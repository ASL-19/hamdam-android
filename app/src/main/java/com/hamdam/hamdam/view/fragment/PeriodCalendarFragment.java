package com.hamdam.hamdam.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ebraminio.droidpersiancalendar.Constants;
import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hamdam.hamdam.R;
import com.hamdam.hamdam.adapters.PeriodCalendarAdapter;
import com.hamdam.hamdam.presenter.FertilityPresenterImpl;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.AnimateUtils;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.view.activity.BaseActivity;
import com.hamdam.hamdam.view.activity.DailyQuestionActivity;

import com.hamdam.hamdam.presenter.DatabaseHelperImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_CLICK;
import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_SCROLL;
import static com.hamdam.hamdam.Constants.ANALYTICS_CATEGORY_CALENDAR;


/**
 * Fragment subclassing the DroidPersianCalendar's CalendarFragment.
 * This fragment overrides click-handling functionality and allows users
 * to navigate to quiz screens and home screen directly from the calendar.
 */
public class PeriodCalendarFragment extends Fragment implements
        View.OnClickListener, ViewPager.OnPageChangeListener {
    private final String TAG = "PeriodCalendarFragmt";
    private ViewPager monthViewPager;
    private static final String DATE = "Date"; // for intent

    private TextView today, startPeriodLegend;
    private AppCompatImageView todayIcon, questionIcon, startPeriod;
    private CardView questionContainer;

    private int viewPagerPosition;
    private PersianDate currentSelectDate;
    private PresenterContracts.FertilityPresenter mFertilityPresenter;
    protected static final int MIDPOINT_INDEX = Constants.MONTHS_LIMIT / 2;
    private Tracker mTracker;

    public PeriodCalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        mFertilityPresenter = FertilityPresenterImpl.getInstance(getActivity());
    }

    @Override
    public void onStop() {
        mFertilityPresenter = null;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentSelectDate != null) {
            new DailyQuestionsCheckedTask().execute
                    (DateUtil.persianToGregorianDate
                            (currentSelectDate));
        }
        if (mTracker != null) {
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        } else {
            Log.e(TAG, "Analytics not updated");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Analytics
        BaseActivity parent = (BaseActivity) getActivity();
        if (parent != null) {
            mTracker = parent.getTracker();
            mTracker.setScreenName(getClass().getSimpleName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }

        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.calendar), true);

        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_calendar, container, false);
        viewPagerPosition = 0;
        currentSelectDate = Utils.getToday();

        // set up Calendar Viewpager
        setViewPager(view);

        today = (TextView) view.findViewById(com.hamdam.hamdam.R.id.today);
        todayIcon = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.today_icon);
        questionIcon = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.daily_questions_icon);
        questionIcon.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.baricon_dailyq, null));

        // Start Period button
        startPeriod = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.period_edit);
        startPeriod.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_startperiod, null));

        startPeriodLegend = (TextView) view.findViewById(com.hamdam.hamdam.R.id.period_edit_legend);
        LinearLayout startPeriodLegendContainer = (LinearLayout)
                view.findViewById(com.hamdam.hamdam.R.id.period_edit_container);
        startPeriodLegendContainer.setOnClickListener(this);

        // Daily Questions bar
        questionContainer = (CardView) view.findViewById(com.hamdam.hamdam.R.id.dailyquestions);

        questionContainer.setOnClickListener(this);
        questionContainer.setVisibility(View.VISIBLE);
        today.setOnClickListener(this);
        todayIcon.setOnClickListener(this);

        Utils.getInstance(getContext()).setFont((TextView) view.findViewById(com.hamdam.hamdam.R.id.today));

        // Legend and disclaimer
        LinearLayout mLegendLayout = (LinearLayout) view.findViewById(com.hamdam.hamdam.R.id.legend_container);
        mLegendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        getString(com.hamdam.hamdam.R.string.fertility_accuracy_disclaimer_text),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Show tooltips on first launch
        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        if (!mSharedPreferences.getBoolean
                (getString(com.hamdam.hamdam.R.string.tooltips_complete_calendar_key), false)) {
            startTooltipSequence();
        }

        return view;
    }

    private void setViewPager(View v) {
        monthViewPager = (ViewPager) v.findViewById(com.hamdam.hamdam.R.id.calendar_pager);
        PeriodCalendarAdapter mAdapter = new PeriodCalendarAdapter(getChildFragmentManager());
        monthViewPager.setAdapter(mAdapter);
        monthViewPager.setCurrentItem(MIDPOINT_INDEX);
        monthViewPager.addOnPageChangeListener(this);

        // Update the UI of the first screen
        monthViewPager.post(new Runnable() {
            @Override
            public void run() {
                PeriodCalendarFragment.this.onPageSelected
                        (monthViewPager.getCurrentItem());
            }
        });
    }

    public void setCurrentSelectDate(PersianDate date) {
        this.currentSelectDate = date;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.hamdam.hamdam.R.id.today:
            case com.hamdam.hamdam.R.id.today_icon:
                bringTodayYearMonth();
                break;
            case com.hamdam.hamdam.R.id.dailyquestions:
                if (!DateUtil.isFutureDate
                        (DateUtil.persianToGregorianDate(currentSelectDate))) {
                    Intent intent = new Intent(getActivity(), DailyQuestionActivity.class);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    intent.putExtra(DATE,
                            df.format(DateUtil.persianToGregorianDate(currentSelectDate)));
                    startActivity(intent);
                } else { // This should not even occur - clicking on future date's quiz is disabled.
                    Toast.makeText(getContext(), getString(com.hamdam.hamdam.R.string.error_edit_future_date),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case com.hamdam.hamdam.R.id.period_edit_container:
                if (DateUtil.isFutureDate
                        (DateUtil.persianToGregorianDate(currentSelectDate))) {
                    Toast.makeText(getContext(), getString(com.hamdam.hamdam.R.string.error_edit_future_date),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setAction(ANALYTICS_ACTION_CLICK)
                            .setCategory(ANALYTICS_CATEGORY_CALENDAR)
                            .setLabel("Add/edit period button")
                            .build());
                    mFertilityPresenter.launchPeriodDialog(currentSelectDate,
                            this.getChildFragmentManager(), viewPagerPosition);
                }
                break;
            default:
                break;
        }
    }

    // Called by PeriodMonthFragment's onClick method for 'next' and 'prev' month display.
    // Change month by given position, and change currently-selected date.
    public void changeMonth(int position) {
        monthViewPager.setCurrentItem(monthViewPager.getCurrentItem() + position, true);
    }

    public void selectDay(PersianDate persianDate) {

        if (Utils.getToday().equals(persianDate)) {
            if (today.getVisibility() == View.VISIBLE) {
                AnimateUtils.medFadeOut(today);
                today.setVisibility(View.INVISIBLE);
            }
            if (todayIcon.getVisibility() == View.VISIBLE) {
                AnimateUtils.medFadeOut(todayIcon);
                todayIcon.setVisibility(View.INVISIBLE);
            }

        } else {
            if (today.getVisibility() != View.VISIBLE) {
                today.setVisibility(View.VISIBLE);
                AnimateUtils.medFadeIn(today);
            }
            if (todayIcon.getVisibility() != View.VISIBLE) {
                todayIcon.setVisibility(View.VISIBLE);
                AnimateUtils.medFadeIn(todayIcon);
            }
        }

        // update current date
        currentSelectDate = persianDate;

        showQuestionButton(persianDate);
    }


    private void showQuestionButton(PersianDate persianDate) {
        Date date = DateUtil.persianToGregorianDate(persianDate);
        if (!DateUtil.isFutureDate(date)) {
            if (questionContainer.getVisibility() != View.VISIBLE) {
                questionContainer.setVisibility(View.VISIBLE);
                AnimateUtils.medFadeIn(questionContainer);
            }
            new DailyQuestionsCheckedTask().execute(date);

        } else {
            if (questionContainer.getVisibility() == View.VISIBLE) {
                AnimateUtils.medFadeOut(questionContainer);
                questionContainer.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void bringTodayYearMonth() {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        if (monthViewPager.getCurrentItem() != MIDPOINT_INDEX) {
            monthViewPager.setCurrentItem(MIDPOINT_INDEX);
        }

        selectDay(Utils.getToday());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        viewPagerPosition = position - MIDPOINT_INDEX;

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        if (viewPagerPosition == 0) {
            intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY,
                    Utils.getToday().getDayOfMonth());
        } else {
            intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);
        }

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        if (viewPagerPosition != 0) {
            today.setVisibility(View.VISIBLE);
            today.setAlpha(1.0f);
            todayIcon.setVisibility(View.VISIBLE);
            todayIcon.setAlpha(1.0f);

            if (mTracker != null) {

                // Track if users are viewing other months
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(ANALYTICS_CATEGORY_CALENDAR)
                        .setAction(ANALYTICS_ACTION_SCROLL)
                        .setValue(viewPagerPosition) // indicate if viewing past, future, or present months
                        .build());
            }
        }

        setPastOrPresentView();
    }

    // Enable question- and period- editing buttons if viewing a current or past calendar month.
    private void setPastOrPresentView() {
        if (monthViewPager.getCurrentItem() < MIDPOINT_INDEX) { // Future or present month
            if (questionContainer.getVisibility() == View.VISIBLE) {
                AnimateUtils.medFadeOut(questionContainer);
                questionContainer.setVisibility(View.INVISIBLE); // Can't do quiz for future dates
            }
            if (startPeriod.getVisibility() == View.VISIBLE) {
                AnimateUtils.medFadeOut(startPeriod);
                startPeriod.setVisibility(View.INVISIBLE); // Can't add future cycles
            }
            if (startPeriodLegend.getVisibility() == View.VISIBLE) {
                AnimateUtils.medFadeOut(startPeriodLegend);
                startPeriodLegend.setVisibility(View.INVISIBLE);
            }
        }

        // Show buttons to edit period data/answer questions
        if (monthViewPager.getCurrentItem() >= MIDPOINT_INDEX) {
            if (questionContainer.getVisibility() != View.VISIBLE) {
                questionContainer.setVisibility(View.VISIBLE);
                AnimateUtils.medFadeIn(questionContainer);
            }
            if (startPeriod.getVisibility() != View.VISIBLE) {
                startPeriod.setVisibility(View.VISIBLE);
                AnimateUtils.medFadeIn(startPeriod);
            }
            if (startPeriodLegend.getVisibility() != View.VISIBLE) {
                startPeriodLegend.setVisibility(View.VISIBLE);
                AnimateUtils.medFadeIn(startPeriodLegend);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private class DailyQuestionsCheckedTask extends AsyncTask<Date, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Date... dates) {
            return DatabaseHelperImpl
                    .getInstance(getContext())
                    .getStatusToday(dates[0])
                    .isEmpty();
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (questionIcon != null &&
                    questionIcon.getVisibility() == View.VISIBLE) {
                questionIcon.setBackgroundResource(
                        (result ? com.hamdam.hamdam.R.drawable.baricon_dailyq
                                : com.hamdam.hamdam.R.drawable.icon_dailyq_finished));
            }
        }
    }


    public int getViewPagerPosition() {
        return viewPagerPosition;
    }

    private void startTooltipSequence() {
        ShowcaseConfig showcaseConfig = new ShowcaseConfig();
        showcaseConfig.setDelay(200); // ms

        MaterialShowcaseSequence sequence =
                new MaterialShowcaseSequence(getActivity());

        // Set up showcase items
        sequence.setConfig(showcaseConfig);

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(
                getActivity())
                .setTarget(questionContainer)
                .withoutShape()
                .setDismissOnTouch(true)
                .setContentText(getString(com.hamdam.hamdam.R.string.tooltips_calendar_blurb))
                .setFadeDuration(500)
                .setMaskColour(ContextCompat.getColor(getContext(),
                        com.hamdam.hamdam.R.color.dim_background))
                .setDismissText(getString(com.hamdam.hamdam.R.string.tooltips_dismiss_casual))
                .build()
        );

        // Set completed to true
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_calendar_key), true)
                .apply();

        sequence.start();
    }

}
