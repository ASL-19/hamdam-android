package com.hamdam.hamdam.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.hamdam.hamdam.service.eventbus.PersianDateEvent;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.Constants;

import com.hamdam.hamdam.service.eventbus.UpdateViewEvent;
import com.hamdam.hamdam.view.dialog.PeriodDialogFragment;
import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides async task handling of queries to the database concerning fertility cycle
 * data, including projecting upcoming menstrual cycle and ovulation window.
 */
public class FertilityPresenterImpl implements PresenterContracts.FertilityPresenter {
	private static final String TAG = "FertilityPresenterImpl";
    private Context mContext;

    public static FertilityPresenterImpl getInstance(Activity activity) {
        return new FertilityPresenterImpl(activity);
    }

    public FertilityPresenterImpl(Activity activity) {
        this.mContext = activity;
    }

    /*
     * Used from homepage toggle button to either update a period, if user has period registered, or delete
     * period, if already recorded.
     * @param   persianDate     Today's date as a PersianDate object.
     */
    @Override
    public void togglePeriod(@NonNull PersianDate date) {
        Date gregorianDate = DateUtil.persianToGregorianDate(date);
        new TogglePeriodTask(mContext, gregorianDate, 0).execute();

    }

    @Override
    public void updatePeriodInfo(@NonNull PersianDate date,
                                 Integer duration, Integer tag) {
            Date gregorianDate = DateUtil.persianToGregorianDate(date);
            new UpdatePeriodTask(mContext,
                    gregorianDate, duration, tag).execute();

    }

    @Override
    public void deletePeriodInfo(@NonNull PersianDate date, Integer tag) {
        Date gregorianDate = DateUtil.persianToGregorianDate(date);
        new DeletePeriodTask(mContext, gregorianDate, tag).execute();
    }

    /*
     * UpdatePeriodTask has a constructor which takes one nonnull and two nullable parameters,
     * an End Date (in gregorian) and an integer representing length of period.
     */
    private final static class UpdatePeriodTask extends AsyncTask<Void, Void, Boolean> {
        private Date startDate;
        private Integer periodLength, tag;
        private WeakReference<Context> mWeakContext;

        public UpdatePeriodTask(Context context,
                                @NonNull Date start, @Nullable Integer periodLength,
                                Integer tag) {
            this.startDate = start;
            this.periodLength = periodLength;
            this.tag = tag;
            this.mWeakContext = new WeakReference<Context>(context);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Context context = mWeakContext.get();
            if (context != null) {
                if (android.os.Debug.isDebuggerConnected())
                    android.os.Debug.waitForDebugger();
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);

                if (periodLength == null) {
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    periodLength = Integer.parseInt
                            (mPrefs.getString(context.getString
                                            (com.hamdam.hamdam.R.string.period_length_key),
                                    Integer.toString(Constants.DEFAULT_PERIOD_LENGTH)));
                }
                return databaseHelper.updatePeriodStats(this.startDate,
                        this.periodLength);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                EventBus.getDefault().post(new UpdateViewEvent(tag, true)); // refresh view
            } else {
                Log.e(TAG, "Update unsunccessful");
            }
        }
    }

    /*
     * Toggle period status by either adding a period on given date, if one does not
     * exist, or deleting if one does exist.
     * This is used on the homepage only.
     */
    private static final class TogglePeriodTask extends AsyncTask<Void, Void, Integer> {
        private Date startDate;
        private Integer tag;
        private WeakReference<Context> mWeakContext;

        public TogglePeriodTask(Context weakContext, Date startDate, Integer tag) {
            this.startDate = startDate;
            this.tag = tag;
            this.mWeakContext = new WeakReference<>(weakContext);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper = DatabaseHelperImpl.getInstance(context);
                return (databaseHelper.isActivePeriodDate(startDate) ? 1 : 0);
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Context context = mWeakContext.get();
            if (context != null) {
                if (result == 1) {
                    new EndPeriodEarlyTask(context, 0).execute();
                } else if (result == 0) {
                    new UpdatePeriodTask(context, startDate, null, tag).execute();
                } // else -1; context was null
            }
        }
    }


    /*
     * Special case for ending period early from the homepage.
     * Ends a period on date that task is executed.
     */
    private static final class EndPeriodEarlyTask extends AsyncTask<Void, Void, Map<Date, Integer>> {
        private int tag;
        private WeakReference<Context> mWeakContext;

        public EndPeriodEarlyTask(Context context, int tag) {
            this.tag = tag;
            this.mWeakContext = new WeakReference<Context>(context);
        }

        @Override
        @SuppressWarnings("null")
        // DateConverter won't return null since dates are checked before supplied
        protected Map<Date, Integer> doInBackground(Void... params) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper =
                        DatabaseHelperImpl.getInstance(context);

                Date simpleDate = DateUtil.clearTimeStamp(new Date());
                Date lastRecordDate = databaseHelper.getLastStartDate(simpleDate);
                Map<Date, Integer> deleteMap = new HashMap<>();
                if (lastRecordDate != null) {

                    // If user clicks button on the same day that period was started, delete whole record.
                    if (DateUtil.clearTimeStamp(lastRecordDate).getTime()
                            == simpleDate.getTime()) {
                        databaseHelper.deletePeriod(lastRecordDate);

                        // send info about deleted date
                        deleteMap.put(lastRecordDate, 1);

                    } else { // Period started a few days ago--end it early instead of deleting it.
                        Date endDate = DateUtil.rollCalendar(simpleDate, -1);
                        databaseHelper.updatePeriodStats(lastRecordDate, endDate, null);
                        deleteMap.put(simpleDate, DateUtil.getDaysApart(simpleDate,
                                DateUtil.clearTimeStamp(endDate)));
                    }

                } else {
                    Log.e(TAG, "Error completing EndEarlyTask");
                }
                return deleteMap;
            }
            return null;
        }

        /*
         * Post event to delete records
         */
        @Override
        public void onPostExecute(Map<Date, Integer> result) {
            if (result != null) {
                for (Date d : result.keySet()) {
                    EventBus.getDefault().postSticky
                            (new UpdateViewEvent(tag, false)); // not active period anymore
                }
            }
        }
    }

    private static final class DeletePeriodTask extends AsyncTask<Void, Void, Map<Date, Integer>> {
        int tag;
        Date date;
        WeakReference<Context> mWeakContext;

        public DeletePeriodTask(Context context, Date date, int tag) {
            this.tag = tag;
            this.date = date;
            this.mWeakContext = new WeakReference<Context>(context);
        }

        @Override
        protected Map<Date, Integer> doInBackground(Void... voids) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper = DatabaseHelperImpl.getInstance(context);

                // first, see how long the cycle is if there is one
                Map<Date, Integer> resultsMap = databaseHelper.getRecordsBetween(date, date); // one day

                // then, delete the record
                databaseHelper.deletePeriod(date);
                return resultsMap;
            }
            return null;
        }

        @Override
        public void onPostExecute(Map<Date, Integer> result) {
            if (result != null) {
                EventBus.getDefault().post(new UpdateViewEvent(tag));
            }
        }
    }


    // AsyncTask to check if selected date is part of a period cycle.
    @Override
    public void launchPeriodDialog(@NonNull PersianDate date, FragmentManager manager, int tag) {
        new ActivePeriodDialogTask(mContext, date, manager, tag)
                .execute(DateUtil.persianToGregorianDate(date));
    }

    private static final class ActivePeriodDialogTask extends AsyncTask<Date, Void, Integer> {
        private PeriodDialogFragment mDialog;
        private FragmentManager mFragmentManager;
        private PersianDate mDate;
        private int tag;
        private WeakReference<Context> mWeakContext;

        public ActivePeriodDialogTask(Context context,
                                      @NonNull PersianDate date, FragmentManager manager, int tag) {
            this.mFragmentManager = manager;
            this.mDate = date;
            this.tag = tag;
            this.mWeakContext = new WeakReference<>(context);
        }

        // True if user has selected date that is part of recorded period
        @Override
        protected Integer doInBackground(Date... dates) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);

                return (databaseHelper.isActivePeriodDate(dates[0]) ? 1 : 0);
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result >= 0) {
                mDialog = PeriodDialogFragment.newInstance(mDate, (result == 1), tag);
                mDialog.show(mFragmentManager, PeriodDialogFragment.class.getName());
            }
        }
    }

    @Override
    public void daysTillCycle(@NonNull PersianDate persianDate) {
        new GetCycleDayTask(mContext, persianDate).execute();
    }

    private class GetCycleDayTask extends AsyncTask<Void, Void, Integer> {
        private PersianDate date;
        private WeakReference<Context> mWeakContext;

        public GetCycleDayTask(Context context, PersianDate date) {
            this.date = date;
            this.mWeakContext = new WeakReference<>(context);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);

                return databaseHelper
                        .daysTillStartDate(DateUtil
                                .persianToGregorianDate(date));
            }
            return Constants.OUT_OF_RANGE;
        }

        @Override
        protected void onPostExecute(Integer result) {
            EventBus.getDefault().postSticky
                    (new PersianDateEvent.CountDownEvent(date, result));
        }
    }
}
