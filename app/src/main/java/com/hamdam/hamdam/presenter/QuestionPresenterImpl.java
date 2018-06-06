package com.hamdam.hamdam.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.hamdam.hamdam.util.DateUtil;

import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.model.DailyStatus;
import com.hamdam.hamdam.service.eventbus.StatusListEvent;
import com.hamdam.hamdam.util.UtilWrapper;
import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

/**
 * Presenter class to be used with Daily Question activity.
 * Handles logic of providing correct models to database/data persistence impl
 * based on UI activity.
 */
public class QuestionPresenterImpl implements PresenterContracts.QuestionPresenter {
	private static final String TAG = "QuestionPresenterImpl";

    private WeakReference<Context> mWeakContext;

    public QuestionPresenterImpl(Activity activity) {
        mWeakContext = new WeakReference<Context>(activity);
    }

    /*
     * Private AsyncTask classes responsible for querying the database (via DatabaseHelper instance)
     * and performing CRUD operations.
     * Included:
     *   - UpdateStatusTask
     *   - DeleteStatusTask
     *   - UpdatePeriodTask
     *   - LoadDataTask
     *
     */

    private static final class UpdateStatusTask extends AsyncTask<DailyStatus, Void, Boolean> {
        private WeakReference<Context> mWeakContext;

        public UpdateStatusTask(WeakReference<Context> weakContext) {
            this.mWeakContext = weakContext;
        }

        @Override
        protected Boolean doInBackground(DailyStatus... dailyStatuses) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);

                return databaseHelper.updateStatus(dailyStatuses[0]);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.e(TAG, "UpdateStatus unsuccessful");
            }
        }
    }

    private static final class DeleteStatusTask extends AsyncTask<DailyStatus, Void, Boolean> {
        private WeakReference<Context> mWeakContext;

        public DeleteStatusTask(WeakReference<Context> weakContext) {
            this.mWeakContext = weakContext;
        }

        @Override
        protected Boolean doInBackground(DailyStatus... dailyStatuses) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);

                return databaseHelper.deleteDailyStatus(dailyStatuses[0]);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.e(TAG, "Delete unsuccessful");
            }
        }
    }

    @Override
    public void deleteDataById(@NonNull Date date, StatusEnum.Options choice, int pageId) {
        StatusEnum.StatusValue value = UtilWrapper.findStatusValue(choice, pageId);
        if (value != null) {
            new DeleteStatusTask(mWeakContext).execute(new DailyStatus(date, value));
        } else {
            Log.e(TAG, "DeleteById: No status value initialized");
        }
    }

    @Override
    public void addDataById(Date date, StatusEnum.Options choice, int pageId) {
        StatusEnum.StatusValue value = UtilWrapper.findStatusValue(choice, pageId);
        if (value != null) {
            DailyStatus status = new DailyStatus(date, value);
            new UpdateStatusTask(mWeakContext).execute(status);
        } else {
            Log.e(TAG, "SaveById: No status value initialized");
        }
    }

    @Override
    public void loadDailyData(@NonNull final PersianDate date) {
        Context context = mWeakContext.get();
        if (context != null) {
            new LoadDataTask(context, date).execute(DateUtil.persianToGregorianDate(date));
        }
    }

    private static final class LoadDataTask extends AsyncTask<Date, Void, ArrayList<DailyStatus>> {
        private PersianDate date;
        private WeakReference<Context> mWeakContext;

        public LoadDataTask(Context context, PersianDate date) {
            this.date = date;
            this.mWeakContext = new WeakReference<Context>(context);
        }

        @Override
        protected ArrayList<DailyStatus> doInBackground(Date... dates) {
            Context context = mWeakContext.get();
            if (context != null) {
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);

                return databaseHelper.getStatusToday(dates[0]);
            }
            return null;
        }

        @Override
        public void onPostExecute(ArrayList<DailyStatus> statuses) {
            if (statuses != null && !statuses.isEmpty()) {
                EventBus.getDefault().postSticky(new StatusListEvent(date, statuses));
            }
        }
    }

}
