package com.hamdam.hamdam.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.Constants;
import com.hamdam.hamdam.R;
import com.hamdam.hamdam.adapters.HomePageMonthAdapter;
import com.hamdam.hamdam.model.MenstruationDayModel;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.presenter.FertilityPresenterImpl;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.service.eventbus.PersianDateEvent;
import com.hamdam.hamdam.service.eventbus.UpdateViewEvent;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.util.LocaleUtils;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.view.activity.DailyQuestionActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * HomePage view of Hamdam app extending {@link Fragment}.
 * Displays a timeline centered at current day and showing past menstruation calendar history
 * and future approximate projections, if available, as well as links to the
 * {@link DailyQuestionActivity}, where users can fill in optional daily status information,
 * {@link InfographicFragment}, where users can view a summarized version of their data,
 * {@link HealthInfoFragment} and {@link DomesticRightsInfoFragment}, for information on
 * physical/sexual health and domestic/marriage rights, and
 * {@link PeriodCalendarFragment}, where users can view and edit their menstrual calendar.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "HomePageFragment";
    private PersianDate persianDate;
    private HomePageMonthAdapter mAdapter;
    private PresenterContracts.FertilityPresenter mFertilityPresenter;
    private List<MenstruationDayModel> cycleDays;
    private int daysTilPeriod;
    private boolean isQuizFilled, isPeriod;
    private static final int TIMELINE_LENGTH = 29;
    private static final String COUNTDOWN_KEY = "CountdownKey",
            IS_PERIOD = "IsPeriod",
            IS_QUESTION_FILLED = "IsQuestionFilled",
            DATE = "Date"; // for savedInstanceState
    private HomePageMonthTask mTimelineTask;
    private PeriodButtonCheckedTask mPeriodCheckTask;
    private DailyQuestionsCheckedTask mQuestionsTask;
    RecyclerView recyclerView;

    // UI Elements
    TextView daysTilPeriodView, periodButtonText;
    AppCompatImageView startPeriod, questionIcon, dailyQuestionSmallWidget;
    RelativeLayout dailyQuestions, infographic;
    LinearLayout indicator;

    public HomePageFragment() {
        // Required empty public constructor
    }

    public static HomePageFragment newInstance() {
        return new HomePageFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize with empty list for adapter, then populate and notify of data change.
        mAdapter = new HomePageMonthAdapter(getContext(), new ArrayList<MenstruationDayModel>());

        // Set current date
        persianDate = Utils.getToday();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_home, container, false);
        Drawable startPeriodDrawable = ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_startperiod, null);
        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.home), true);

        TextView dateToday = (TextView) view.findViewById(com.hamdam.hamdam.R.id.date_today);
        dateToday.setText(Utils.dateToString(persianDate));

        // Set up buttons and click handlers
        dailyQuestions = (RelativeLayout)
                view.findViewById(com.hamdam.hamdam.R.id.daily_questions_container);
        infographic = (RelativeLayout)
                view.findViewById(com.hamdam.hamdam.R.id.infographic_container);

        // Set icon drawables
        AppCompatImageView infographicIcon = (AppCompatImageView) view.findViewById(R.id.infographic_icon);
        AppCompatImageView healthIcon = (AppCompatImageView) view.findViewById(R.id.health_icon);
        AppCompatImageView domesticRightsIcon = (AppCompatImageView) view.findViewById(R.id.marriage_rights_icon);

        Drawable infographicDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_inforgraphic, null);
        Drawable healthDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_health, null);
        Drawable domesticRightsDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_marriage_rights, null);

        infographicIcon.setBackground(infographicDrawable);
        healthIcon.setBackground(healthDrawable);
        domesticRightsIcon.setBackground(domesticRightsDrawable);

        RelativeLayout marriageRights = (RelativeLayout)
                view.findViewById(com.hamdam.hamdam.R.id.marriage_rights_title_container);
        RelativeLayout healthInfo = (RelativeLayout)
                view.findViewById(com.hamdam.hamdam.R.id.health_info_title_container);
        LinearLayout periodEditContainer = (LinearLayout)
                view.findViewById(com.hamdam.hamdam.R.id.edit_period_button_container);
        startPeriod = (AppCompatImageView)
                view.findViewById(com.hamdam.hamdam.R.id.period_edit);
        startPeriod.setBackground(startPeriodDrawable);

        // "Days till Period" message, period timeline
        daysTilPeriodView = (TextView) view.findViewById(com.hamdam.hamdam.R.id.days_til_period);
        periodButtonText = (TextView) view.findViewById(com.hamdam.hamdam.R.id.period_edit_legend);
        indicator = (LinearLayout) view.findViewById(com.hamdam.hamdam.R.id.today_indicator);
        indicator.setOnClickListener(this);

        // Questions, Infographic, and education views
        questionIcon = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.daily_questions_icon);
        Drawable questionDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.baricon_dailyq, null);
        questionIcon.setBackground(questionDrawable);

        dailyQuestionSmallWidget = (AppCompatImageView)
                view.findViewById(com.hamdam.hamdam.R.id.daily_questions_check_widget);

        dailyQuestions.setOnClickListener(this);
        infographic.setOnClickListener(this);
        healthInfo.setOnClickListener(this);
        marriageRights.setOnClickListener(this);
        periodEditContainer.setOnClickListener(this);

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

        // Homepage month-at-a-glance graphic
        setRecyclerView(view);

        // Menstruation cycle info presenter
        mFertilityPresenter = FertilityPresenterImpl.getInstance(getActivity());

        // Build timeline, also determines days till next cycle.
        mTimelineTask = new HomePageMonthTask();
        mTimelineTask.execute();

        if (savedInstanceState != null) {
            Log.d(TAG, "SavedInstanceState for homepage fragment--recreating....");

            daysTilPeriod = savedInstanceState.getInt(COUNTDOWN_KEY);
            isPeriod = savedInstanceState.getBoolean(IS_PERIOD);
            isQuizFilled = savedInstanceState.getBoolean(IS_QUESTION_FILLED);

            // update UI
            setDaysTilPeriod(daysTilPeriod);
            setPeriodButtonChecked(isPeriod);
            setDailyQuestionsChecked(isQuizFilled);
            Log.d(TAG, "Quiz Filled: " + (isQuizFilled ? "true" : "false"));


        } else { // No saved data; launch tasks to find current cycle day

            if (persianDate == null) {
                persianDate = Utils.getToday();
            }

            // See if in middle of period, update button
            mPeriodCheckTask = new PeriodButtonCheckedTask(getContext());
            mPeriodCheckTask.execute();

            // Estimate days till next anticipated period
            mFertilityPresenter.daysTillCycle(persianDate);
        }

        // Show tooltips on first launch (or if tooltips re-enabled)
        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        if (!mSharedPreferences.getBoolean
                (getString(com.hamdam.hamdam.R.string.tooltips_complete_homepage_key), false)) {
            startTooltipSequence();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // See if daily status quiz has been updated, update button
        mQuestionsTask = new DailyQuestionsCheckedTask();
        mQuestionsTask.execute(DateUtil.persianToGregorianDate(persianDate));
    }

    // Set state of "Start period!" button (checked or unchecked)
    private void setPeriodButtonChecked(boolean isActivePeriod) {
        startPeriod.setBackgroundResource(
                (isActivePeriod ? com.hamdam.hamdam.R.drawable.icon_activeperiod
                        : com.hamdam.hamdam.R.drawable.icon_startperiod));
        periodButtonText.setText(
                isActivePeriod ? getString(com.hamdam.hamdam.R.string.end_period_legend_text) // The button will offer to end a current cycle
                        : getString(com.hamdam.hamdam.R.string.start_period_legend_text));  // The button will allow you to create a new cycle

        this.isPeriod = isActivePeriod;
    }

    // Set state of "Daily Questions" button.
    // Note that if a user has answered at minimum one the questions, the checkmark will appear
    // filled in. This does not mean that they have answered *all* of the questions!
    private void setDailyQuestionsChecked(boolean isQuizFilled) {
        questionIcon.setBackgroundResource(
                (isQuizFilled
                        ? com.hamdam.hamdam.R.drawable.icon_dailyq_finished
                        : com.hamdam.hamdam.R.drawable.baricon_dailyq));
        dailyQuestionSmallWidget.setBackgroundResource(
                (isQuizFilled
                        ? com.hamdam.hamdam.R.drawable.icon_dailyq_finished
                        : com.hamdam.hamdam.R.drawable.baricon_dailyq));
        this.isQuizFilled = isQuizFilled;
    }

    private void setRecyclerView(View view) {
        recyclerView = (RecyclerView)
                view.findViewById(com.hamdam.hamdam.R.id.RecyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setOnClickListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        PresenterContracts.NavigationView mNavigator =
                (PresenterContracts.NavigationView) getActivity();

        switch (view.getId()) {
            case (com.hamdam.hamdam.R.id.daily_questions_container):

                // hide this fragment, show dailyStatus; don't change menu position.
                Intent intent = new Intent(getActivity(), DailyQuestionActivity.class);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                intent.putExtra(DATE,
                        df.format(new Date()));
                startActivity(intent);
                break;
            case (com.hamdam.hamdam.R.id.infographic_container):
                mNavigator.setNavigationPosition
                        (mNavigator.getNavigationPosition
                                (InfographicFragment.class.getName()));
                getActivity().getSupportFragmentManager()
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(com.hamdam.hamdam.R.id.fragment_holder, InfographicFragment.newInstance(),
                                InfographicFragment.class.getName())
                        .addToBackStack(null)
                        .commit();
                break;
            case (com.hamdam.hamdam.R.id.health_info_title_container):
                mNavigator.setNavigationPosition
                        (mNavigator.getNavigationPosition
                                (HealthInfoFragment.class.getName()));
                getActivity().getSupportFragmentManager()
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(com.hamdam.hamdam.R.id.fragment_holder, HealthInfoFragment.newInstance(),
                                HealthInfoFragment.class.getName())
                        .addToBackStack(null)
                        .commit();
                break;
            case (com.hamdam.hamdam.R.id.marriage_rights_title_container):
                mNavigator.setNavigationPosition
                        (mNavigator.getNavigationPosition
                                (DomesticRightsInfoFragment.class.getName()));
                getActivity().getSupportFragmentManager()
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(com.hamdam.hamdam.R.id.fragment_holder, DomesticRightsInfoFragment.newInstance(),
                                DomesticRightsInfoFragment.class.getName())
                        .addToBackStack(null)
                        .commit();
                break;
            case (com.hamdam.hamdam.R.id.edit_period_button_container):
                mFertilityPresenter.togglePeriod(persianDate);
                break;
            case (com.hamdam.hamdam.R.id.RecyclerView):
            case (com.hamdam.hamdam.R.id.today_indicator):
                try {
                    mNavigator.setNavigationPosition
                            (mNavigator.getNavigationPosition
                                    (PeriodCalendarFragment.class.getName()));
                    getActivity().getSupportFragmentManager()
                            .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(com.hamdam.hamdam.R.id.fragment_holder,
                                    PeriodCalendarFragment.class.newInstance(),
                                    PeriodCalendarFragment.class.getName())
                            .addToBackStack(null)
                            .commit();
                } catch (java.lang.InstantiationException e) {
                    Log.e(TAG, e.getMessage());
                } catch (IllegalAccessException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        outstate.putInt(COUNTDOWN_KEY, daysTilPeriod);
        outstate.putBoolean(IS_QUESTION_FILLED, isQuizFilled);
        outstate.putBoolean(IS_PERIOD, isPeriod);
        super.onSaveInstanceState(outstate);
    }

    private class DailyQuestionsCheckedTask extends AsyncTask<Date, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Date... dates) {
            Context context = getContext();

            // default == true (isEmpty)
            return (context == null ||
                    DatabaseHelperImpl
                        .getInstance(context)
                        .getStatusToday(new Date())
                        .isEmpty());
        }

        // onPostExecute returns isEmpty (rather than isFilled) by design
        @Override
        public void onPostExecute(Boolean isEmptyQuiz) {
            setDailyQuestionsChecked(!isEmptyQuiz);
        }
    }

    private class PeriodButtonCheckedTask extends AsyncTask<Date, Void, Boolean> {
        private WeakReference<Context> mWeakContext;

        public PeriodButtonCheckedTask(Context context) {
            this.mWeakContext = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Date... dates) {
            Context context = mWeakContext.get();
            if (context != null) {
                return DatabaseHelperImpl
                        .getInstance(context)
                        .isActivePeriodDate(new Date());
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean isActivePeriod) {
            setPeriodButtonChecked(isActivePeriod);
        }
    }

    // Calculate number of days till period and set textview to display the appropriate message.
    // If calculation indicates that user's cycle is more than two weeks late, do not display the prediction
    // message, since it may be inaccurate (user has not entered data, has entered erroneous data,
    // has irregular cycles or is possibly pregnant) and more data is required to improve the prediction.
    public void setDaysTilPeriod(Integer daysTil) {
        String message = null;
        if (daysTil != null) {
            if (daysTil > 0 && daysTil < Constants.DEFAULT_LONG_CYCLE_LENGTH) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) { // RTL is not supported
                    message = LocaleUtils.buildLtr(getString(com.hamdam.hamdam.R.string.days_till_period)) + " "
                            + LocaleUtils.buildLtr(Utils.formatNumber(Math.abs(daysTil)));
                } else {
                    message = (Utils.formatNumber(daysTil))
                            + " " + getString(com.hamdam.hamdam.R.string.days_till_period);
                }
            } else if (daysTil == 0) {
                // set daysTillPeriod to "period due"
                message = getString(com.hamdam.hamdam.R.string.period_due);

            } else if (daysTil < 0 && daysTil > (Constants.DEFAULT_CYCLE_LENGTH * -1)) { // Less than two weeks late
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) { // RTL is not supported
                    message = LocaleUtils.buildLtr(getString(com.hamdam.hamdam.R.string.days_period_late)) + " "
                            + LocaleUtils.buildLtr(Utils.formatNumber(Math.abs(daysTil)));
                } else {
                    message = Utils.formatNumber(Math.abs(daysTil))
                            + " " + getString(com.hamdam.hamdam.R.string.days_period_late);
                }
            } else if (daysTil == Constants.FLAG_TODAY) {
                message = getString(com.hamdam.hamdam.R.string.period_today);
            }

            // Other cases: daysTil == Constants.OUT_OF_RANGE (no data), or daysTil is
            // more than 60 days in advance or more than 28 days late.
            // in these cases, showing a 'prediction' message is too inaccurate.
        }

        // If can't show predictive message, just show generic message
        if (message == null) {
            message = getString(com.hamdam.hamdam.R.string.cycle_at_a_glance); // generic title
        }
        daysTilPeriodView.setText(message);
    }

    @Subscribe(sticky = true)
    public void onCountDownEvent(PersianDateEvent.CountDownEvent event) {
        if (event.getDate().equals(Utils.getToday())) {
            daysTilPeriod = event.getCountdown();
            setDaysTilPeriod(daysTilPeriod);
            EventBus.getDefault().removeStickyEvent(event);
        }
    }

    /*
     * Create a timeline (chronological List<MenstruationDayModel> of days centered at the current day),
     * populate with fertility information from the database.
     */
    private class HomePageMonthTask extends AsyncTask<Void, Void, List<MenstruationDayModel>> {
        private WeakReference<Context> mWeakContext;

        public HomePageMonthTask() {
            this.mWeakContext = new WeakReference<>(getContext());
        }

        @Override
        protected List<MenstruationDayModel> doInBackground(Void... params) {
            List<MenstruationDayModel> days = DateUtil.getFertilityDays(0);
            cycleDays = UtilWrapper
                    .fillDaysToCenter(days, persianDate, TIMELINE_LENGTH, 0);
            Context context = mWeakContext.get();
            if (context != null) {

                // Get past period and ovulation days, get projected period and ovulation days,
                // then set up calender months to hold this information
                PresenterContracts.DatabasePresenter databaseHelper
                        = DatabaseHelperImpl.getInstance(context);
                List<Date> ovulationDates = databaseHelper.getPastOvulationDates();
                Map<Date, Integer> periodRecords = databaseHelper
                        .getRecordsBetween(cycleDays.get(0).getGregorianDate(),
                                cycleDays.get(cycleDays.size() - 1)
                                        .getGregorianDate());
                // Get projections
                Map<Date, Integer> periodProjections = databaseHelper
                        .projectRecordsBetween(days.get(0).getGregorianDate(),
                                days.get(days.size() - 1).getGregorianDate(), true);
                Map<Date, Integer> ovulationProjections = databaseHelper
                        .projectRecordsBetween(days.get(0).getGregorianDate(),
                                days.get(days.size() - 1).getGregorianDate(), false);

                // Build records and projections into a "timeline" (a List<Day>).
                cycleDays = DateUtil.setFertilityMonth(cycleDays, periodRecords,
                        ovulationDates, periodProjections, ovulationProjections);
            }
            return cycleDays;
        }

        @Override
        public void onPostExecute(List<MenstruationDayModel> result) {
            if (mAdapter != null) {
                mAdapter.addAllItems(result);
                mAdapter.notifyDataSetChanged();
            }

            cycleDays = result;
        }
    }

    @Subscribe
    public void onUpdateViewEvent(UpdateViewEvent event) {
        if (isAdded() && event.getTag() == 0) {
            mTimelineTask = new HomePageMonthTask();
            mTimelineTask.execute();
            mFertilityPresenter.daysTillCycle(persianDate);
            isPeriod = event.isPeriod();
            setPeriodButtonChecked(isPeriod);
        }
    }

    /*
     * Show tooltips overlays.
     */
    private void startTooltipSequence() {
        ShowcaseConfig showcaseConfig = new ShowcaseConfig();
        showcaseConfig.setDelay(300); // ms

        MaterialShowcaseSequence sequence =
                new MaterialShowcaseSequence(getActivity());

        // Set up showcase items
        sequence.setConfig(showcaseConfig);

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(
                getActivity())
                .setTarget(startPeriod)
                .setDismissOnTouch(true)
                .setContentText(getString(com.hamdam.hamdam.R.string.tooltips_start_period_blurb))
                .setFadeDuration(500)
                .setMaskColour(ContextCompat.getColor(getContext(),
                        com.hamdam.hamdam.R.color.dim_background))
                .setDismissText(getString(com.hamdam.hamdam.R.string.tooltips_dismiss_casual))
                .setShapePadding(40)
                .build()
        );

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(
                getActivity())
                .setTarget(recyclerView)
                .setDismissOnTouch(true)
                .setContentText(getString(com.hamdam.hamdam.R.string.tooltips_timeline_blurb))
                .setFadeDuration(500)
                .setMaskColour(ContextCompat.getColor(getContext(),
                        com.hamdam.hamdam.R.color.dim_background))
                .setDismissText(getString(com.hamdam.hamdam.R.string.tooltips_dismiss_casual))
                .withRectangleShape(false)
                .setShapePadding(24)
                .build()
        );

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(
                getActivity())
                .setTarget(dailyQuestions)
                .setDismissOnTouch(true)
                .setContentText(getString(com.hamdam.hamdam.R.string.tooltips_daily_questions_blurb))
                .setFadeDuration(500)
                .withRectangleShape()
                .setMaskColour(ContextCompat.getColor(getContext(),
                        com.hamdam.hamdam.R.color.dim_background))
                .setDismissText(getString(com.hamdam.hamdam.R.string.tooltips_dismiss_casual))
                .setShapePadding(20)
                .build()
        );

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(
                getActivity())
                .setTarget(indicator)
                .withoutShape()
                .setTitleText(getString(com.hamdam.hamdam.R.string.tooltips_finished_title))
                .setDismissOnTouch(true)
                .setContentText(getString(com.hamdam.hamdam.R.string.tooltips_finished))
                .setDismissText(com.hamdam.hamdam.R.string.get_started)
                .setFadeDuration(500)
                .setMaskColour(ContextCompat.getColor(getContext(),
                        com.hamdam.hamdam.R.color.dim_background))
                .build()
        );

        // Set completed to true
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_homepage_key), true)
                .apply();

        sequence.start();
    }

    /*
     * Cancel DailyQuestionsCheckedTask, which is dispatched in onStart, if still running.
     */
    @Override
    public void onStop() {
        if (mQuestionsTask != null) {
            mQuestionsTask.cancel(true);
            mQuestionsTask = null;
        }
        super.onStop();
    }

    /*
         * Cancel running tasks that were started in {@link #onCreateView()}, if any, and destroy view.
         */
    @Override
    public void onDestroyView() {
        if (mTimelineTask != null) {
            mTimelineTask.cancel(true);
            mTimelineTask = null;
        }
        if (mPeriodCheckTask != null) {
            mPeriodCheckTask.cancel(true);
            mPeriodCheckTask = null;
        }
        super.onDestroyView();
    }

}
