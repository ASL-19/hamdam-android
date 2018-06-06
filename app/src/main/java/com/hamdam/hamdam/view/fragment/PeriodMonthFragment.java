package com.hamdam.hamdam.view.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.Constants;
import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.model.MenstruationDayModel;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.DateUtil;

import com.hamdam.hamdam.adapters.PeriodMonthAdapter;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.service.eventbus.UpdateViewEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A subclass of {@link Fragment} to allow for customized
 * behaviour when interacting with calendar dates, such as launching
 * the quiz screen and sending menstrual calendar data to the adapter for display.
 */
public class PeriodMonthFragment extends Fragment implements View.OnClickListener {

    private PeriodCalendarFragment calendarFragment;
    private int monthOffset;
    private PeriodMonthAdapter adapter;
    private RecyclerView recyclerView;
    private CalendarMonthTask mTask;
    private TextView currentMonth;

    public void onClickItem(PersianDate day) {
        calendarFragment.selectDay(day);
        calendarFragment.setCurrentSelectDate(day);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.hamdam.hamdam.R.id.next:
                calendarFragment.changeMonth(1);
                break;
            case com.hamdam.hamdam.R.id.prev:
                calendarFragment.changeMonth(-1);
                break;
            default:
                break;
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_month, container, false);

        monthOffset = getArguments().getInt(Constants.OFFSET_ARGUMENT);

        mTask = new CalendarMonthTask(getContext(), monthOffset);
        mTask.execute();

        AppCompatImageView prev = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.prev);
        AppCompatImageView next = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.next);
        currentMonth = (TextView) view.findViewById(com.hamdam.hamdam.R.id.current_month);

        prev.setOnClickListener(this);
        next.setOnClickListener(this);

        setRecyclerView(view);

        calendarFragment = (PeriodCalendarFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(PeriodCalendarFragment.class.getName());

        if (monthOffset == 0 && calendarFragment.getViewPagerPosition() == monthOffset) {
            calendarFragment.selectDay(Utils.getToday());
        }

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setCurrentMonthReceiver,
                new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));


        return view;
    }

    public PeriodMonthFragment() {
        // Required empty public constructor
    }

    private void setRecyclerView(View view) {
        recyclerView = (RecyclerView)
                view.findViewById(com.hamdam.hamdam.R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Override
    public void onStop() {
        if (mTask != null) {
            mTask.cancel(false);
            mTask = null;
        }
        super.onStop();
    }

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getExtras().getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == monthOffset) {

                int day = intent.getExtras().getInt(Constants.BROADCAST_FIELD_SELECT_DAY);
                if (day != -1 && adapter != null) {
                    adapter.selectDay(day);
                }
            } else if (value == Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY
                    && adapter != null) {
                adapter.clearSelectedDay();
            }
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance
                (getContext()).unregisterReceiver(setCurrentMonthReceiver);
        super.onDestroy();
    }


    private class CalendarMonthTask extends AsyncTask<Void, Void, List<MenstruationDayModel>> {
        private int offset;
        private WeakReference<Context> mWeakContext;

        public CalendarMonthTask(Context context, int offset) {
            this.offset = offset;
            this.mWeakContext = new WeakReference<>(context);
        }

        @Override
        protected List<MenstruationDayModel> doInBackground(Void... params) {
            List<MenstruationDayModel> cycleDays = DateUtil.getFertilityDays(offset);
            Context context = mWeakContext.get();
            if (context != null) {

                // Get past period and ovulation days, get projected period and ovulation days,
                // then set up calender months to hold this information
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);

                // Pull period records from database, and estimate ovulation windows
                Map<Date, Integer> periodDays = databaseHelper
                        .getRecordsBetween(cycleDays.get(0).getGregorianDate(),
                                cycleDays.get(cycleDays.size() - 1)
                                        .getGregorianDate());
                List<Date> ovulationDays = databaseHelper.getPastOvulationDates();

                // Project periods for future, and estimate ovulation window
                Map<Date, Integer> projectedPeriodDays = databaseHelper.projectRecordsBetween
                        (cycleDays.get(0).getGregorianDate(),
                                cycleDays.get(cycleDays.size() - 1).getGregorianDate(), true);
                Map<Date, Integer> projectedOvDays = databaseHelper.projectRecordsBetween
                        (cycleDays.get(0).getGregorianDate(),
                                cycleDays.get(cycleDays.size() - 1).getGregorianDate(), false);
                cycleDays = DateUtil.setFertilityMonth(cycleDays, periodDays,
                        ovulationDays, projectedPeriodDays, projectedOvDays);
            }
            return cycleDays;
        }


        @Override
        public void onPostExecute(List<MenstruationDayModel> result) {
            adapter = new PeriodMonthAdapter(getContext(), PeriodMonthFragment.this, result);
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
            }
            String monthName = Utils.getMonthName(result.get(1).getPersianDate());
            currentMonth.setText(monthName);
        }
    }


    @Subscribe
    public void onUpdateViewEvent(UpdateViewEvent event) {
        if (isAdded()) {
            mTask = new CalendarMonthTask(getContext(), monthOffset);
            mTask.execute();
        }
    }

}
