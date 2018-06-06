package com.hamdam.hamdam.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LinkagePager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.presenter.QuestionPresenterImpl;
import com.hamdam.hamdam.util.AnimateUtils;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.util.UtilWrapper;

import com.hamdam.hamdam.util.LocaleUtils;
import com.hamdam.hamdam.view.fragment.statusfragments.QuizIconFragment;
import com.hamdam.hamdam.view.fragment.statusfragments.QuizStatusFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.crosswall.lib.coverflow.core.LinkagePagerContainer;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_DONE;
import static com.hamdam.hamdam.Constants.ANALYTICS_CATEGORY_QUIZ;


public class DailyQuestionActivity extends BaseActivity
        implements LinkagePager.OnPageChangeListener, PresenterContracts.QuestionView {
    private static final String TAG = "DailyQuestionActivity";

    private LinkagePagerContainer mLinkagePagerContainer;
    private LinkagePager quizIconPager;
    private CustomTitlePagerAdapter adapter;
    private CustomPagerAdapter iconAdapter;
    private LinkagePager titleIconPager;
    private int currentIndex = COUNT - 1, previousIndex = COUNT - 1;
    private Button done;
    TabLayout mTabs;

    private static final int COUNT = 7;
    private PersianDate date;

    private PresenterContracts.QuestionPresenter mPresenter;
    private SparseArray<QuizIconFragment> registeredTitleFragments = new SparseArray<>(); // change drawables of unselected fragments.
    private SparseArray<QuizStatusFragment> registeredAnswerFragments = new SparseArray<>(); // change drawables of unselected fragments.

    private static final String POSITION = "Position", DATE = "Date"; // For savedInstanceState

    // buttons
    AppCompatImageButton buttonOne, buttonTwo, buttonThree, buttonFour;

    // labels
    TextView labelOne, labelTwo, labelThree, labelFour;

    // In order to use VectorDrawables on < Android 5 devices.
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public PersianDate getSelectedDate() {
        return this.date;
    }

    @Override
    protected void onStop() {
        mPresenter = null;
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter = new QuestionPresenterImpl(this);
        if (date != null) {
            mPresenter.loadDailyData(date);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTracker != null) {
            mTracker.setScreenName(getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    private static final Map<Integer, StatusEnum.Options> buttonLookup =
            Collections.unmodifiableMap(setButtonLookup());

    private static final Map<Integer, StatusEnum.Options> setButtonLookup() {
        HashMap<Integer, StatusEnum.Options> buttonOptions = new HashMap<>();
        buttonOptions.put(com.hamdam.hamdam.R.id.optionOne, StatusEnum.Options.ONE);
        buttonOptions.put(com.hamdam.hamdam.R.id.optionTwo, StatusEnum.Options.TWO);
        buttonOptions.put(com.hamdam.hamdam.R.id.optionThree, StatusEnum.Options.THREE);
        buttonOptions.put(com.hamdam.hamdam.R.id.optionFour, StatusEnum.Options.FOUR);
        return buttonOptions;
    }

    public DailyQuestionActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.hamdam.hamdam.R.layout.activity_daily_questions);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        // Analytics
        if (mTracker != null) {
            mTracker.setScreenName(getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        Intent intent = getIntent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor
                    (ContextCompat.getColor(this, com.hamdam.hamdam.R.color.primary_dark));
        }

        // Get date from intent
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final String dateString = intent.getStringExtra(DATE);
        if (dateString != null) {
            try {
                date = DateUtil.gregorianDateToPersian(sdf.parse(dateString));
            } catch (ParseException ex) {
                Log.e(TAG, "Failed to parse date from intent extras: " + ex.getMessage());
            }
        }

        // Set current page and retrieve date, if stored
        if (savedInstanceState != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            currentIndex = savedInstanceState.getInt(POSITION);
            try {
                date = DateUtil.gregorianDateToPersian(
                        df.parse(savedInstanceState.getString(DATE)));
            } catch (ParseException ex) {
                Log.e(TAG, "Could not restore date from savedInstanceState: "
                        + ex.getMessage());
            }
        }

        if (date == null) {
            date = Utils.getToday();
        }

        String buttonTitle = setQuestionString(date);
        TextView tv = (TextView) findViewById(com.hamdam.hamdam.R.id.dsTextView);
        if (tv != null) {
            tv.setText(buttonTitle);
        }

        mLinkagePagerContainer = (LinkagePagerContainer) findViewById(com.hamdam.hamdam.R.id.pager_container);
        titleIconPager = mLinkagePagerContainer.getViewPager(); // R.id.linkageTitlePager
        adapter = new CustomTitlePagerAdapter(getSupportFragmentManager());
        titleIconPager.setAdapter(adapter);
        titleIconPager.setOffscreenPageLimit(adapter.getCount());
        titleIconPager.setClipToPadding(false);
        titleIconPager.setClipChildren(false);

        quizIconPager = (LinkagePager) findViewById(com.hamdam.hamdam.R.id.linkageBodyPager);
        iconAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        quizIconPager.setAdapter(iconAdapter);
        quizIconPager.setOffscreenPageLimit(iconAdapter.getCount());
        quizIconPager.setClipToPadding(false);
        quizIconPager.setClipChildren(false);

        // bind pagers together; bind tabs
        quizIconPager.setLinkagePager(titleIconPager);
        titleIconPager.setLinkagePager(quizIconPager);
        mTabs = (TabLayout) findViewById(com.hamdam.hamdam.R.id.tabs);
        mTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                quizIconPager.setCurrentItem(tab.getPosition(), true);
                titleIconPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        titleIconPager.setCurrentItem(currentIndex);
        quizIconPager.setCurrentItem(currentIndex); // may not need.

        quizIconPager.addOnPageChangeListener(this);

        mLinkagePagerContainer.setOverlapEnabled(false); // Should be no overlap of icons

        // prev, next buttons
        AppCompatImageView prev = (AppCompatImageView) findViewById(com.hamdam.hamdam.R.id.prev);
        AppCompatImageView next = (AppCompatImageView) findViewById(com.hamdam.hamdam.R.id.next);
        if (prev != null) {
            prev.setFilterTouchesWhenObscured(true);
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int curr = quizIconPager.getCurrentItem();
                    if (curr > 0) {
                        quizIconPager.setCurrentItem(curr - 1, true);
                        titleIconPager.setCurrentItem(curr - 1, true);
                    }
                }
            });
        }
        if (next != null) {
            next.setFilterTouchesWhenObscured(true);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int curr = quizIconPager.getCurrentItem();
                    if (curr < quizIconPager.getChildCount()) {
                        quizIconPager.setCurrentItem(curr + 1, true);
                        titleIconPager.setCurrentItem(curr + 1, true);
                    }
                }
            });
        }

        // set 'done' button
        done = (Button) findViewById(com.hamdam.hamdam.R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Track : user is leaving quiz
                if (mTracker != null) {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(ANALYTICS_CATEGORY_QUIZ)
                            .setAction(ANALYTICS_ACTION_DONE)
                            .setValue(quizIconPager.getCurrentItem()) // which page was user on when they left the quiz
                            .build());
                }

                Intent intent = new Intent(DailyQuestionActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity task stack, resume main
                startActivity(intent);
                finish();
            }
        });

        // Show tooltips on first launch
        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (!mSharedPreferences.getBoolean
                (getString(com.hamdam.hamdam.R.string.tooltips_complete_question_key), false)) {
            startTooltipSequence();
        }
    }

    private String setQuestionString(PersianDate currentDate) {
        String text;
        if (currentDate != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                text = LocaleUtils.buildLtr(getString(com.hamdam.hamdam.R.string.daily_questions))
                        + " " + LocaleUtils.buildLtr
                        (Utils.dateToString(currentDate));
            } else {
                text = Utils.dateToString(currentDate)
                        + " " + getString(com.hamdam.hamdam.R.string.daily_questions);
            }
        } else {
            text = getString(com.hamdam.hamdam.R.string.daily_questions);
        }
        return text;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        outState.putInt(POSITION, quizIconPager.getCurrentItem());
        outState.putString(DATE, df.format(DateUtil.persianToGregorianDate(date)));
        super.onSaveInstanceState(outState);
    }

    @Override
    public StatusEnum.Options getOptionChoice(int clickId) {
        switch (clickId) {
            case com.hamdam.hamdam.R.id.optionOne:
                return StatusEnum.Options.ONE;
            case com.hamdam.hamdam.R.id.optionTwo:
                return StatusEnum.Options.TWO;
            case com.hamdam.hamdam.R.id.optionThree:
                return StatusEnum.Options.THREE;
            case com.hamdam.hamdam.R.id.optionFour:
                return StatusEnum.Options.FOUR;
            default:
                return null;
        }
    }

    @Override
    public int getCurrentPosition() {
        return titleIconPager.getCurrentItem();
    }

    // Get a list of all the page's button Ids.
    @Override
    public Set<Integer> getQuizButtons() {
        return buttonLookup.keySet();
    }

    @Override
    public void setButtonLabelsById(View view, int pageId) {
        labelOne = (TextView) view.findViewById(com.hamdam.hamdam.R.id.optionTextOne);
        labelTwo = (TextView) view.findViewById(com.hamdam.hamdam.R.id.optionTextTwo);
        labelThree = (TextView) view.findViewById(com.hamdam.hamdam.R.id.optionTextThree);
        labelFour = (TextView) view.findViewById(com.hamdam.hamdam.R.id.optionTextFour);

        switch (pageId) {
            case StatusEnum.Topics.SEX:
                labelOne.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SEX,
                                StatusEnum.Options.ONE)));
                labelTwo.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SEX,
                                StatusEnum.Options.TWO)));
                labelThree.setVisibility(View.GONE);
                labelFour.setVisibility(View.GONE);
                break;

            case StatusEnum.Topics.EXERCISE:
                labelOne.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.EXERCISE,
                                StatusEnum.Options.ONE)));
                labelTwo.setVisibility(View.GONE);
                labelThree.setVisibility(View.GONE);
                labelFour.setVisibility(View.GONE);

                break;

            case StatusEnum.Topics.SLEEP:
                labelOne.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SLEEP,
                                StatusEnum.Options.ONE)));
                labelTwo.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SLEEP,
                                StatusEnum.Options.TWO)));
                labelThree.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SLEEP,
                                StatusEnum.Options.THREE)));
                labelFour.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SLEEP,
                                StatusEnum.Options.FOUR)));

                break;


            case StatusEnum.Topics.MOOD:
                labelOne.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.MOOD,
                                StatusEnum.Options.ONE)));
                labelTwo.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.MOOD,
                                StatusEnum.Options.TWO)));
                labelThree.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.MOOD,
                                StatusEnum.Options.THREE)));
                labelFour.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.MOOD,
                                StatusEnum.Options.FOUR)));

                break;

            case StatusEnum.Topics.PAIN: // Note: These are specifically ordered and order matters!
                // 1: headache; 2: cramps, 3: back pain, 4: breast tenderness.
                labelOne.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.PAIN,
                                StatusEnum.Options.ONE)));
                labelTwo.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.PAIN,
                                StatusEnum.Options.TWO)));
                labelThree.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.PAIN,
                                StatusEnum.Options.THREE)));
                labelFour.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.PAIN,
                                StatusEnum.Options.FOUR)));

                break;

            case StatusEnum.Topics.FLUIDS:
                // Note: These are specifically ordered and order matters!
                // 1: sticky, 2: creamy, 3: egg white, 4: atypical.
                labelOne.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.FLUIDS,
                                StatusEnum.Options.ONE)));
                labelTwo.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.FLUIDS,
                                StatusEnum.Options.TWO)));
                labelThree.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.FLUIDS,
                                StatusEnum.Options.THREE)));
                labelFour.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.FLUIDS,
                                StatusEnum.Options.FOUR)));

                break;

            case StatusEnum.Topics.BLEEDING:
                labelOne.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.BLEEDING,
                                StatusEnum.Options.ONE)));
                labelTwo.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.BLEEDING,
                                StatusEnum.Options.TWO)));
                labelThree.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.BLEEDING,
                                StatusEnum.Options.THREE)));
                labelFour.setText(UtilWrapper.getValueLabel(this,
                        StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.BLEEDING,
                                StatusEnum.Options.FOUR)));

                break;

            default:
                Log.e(TAG, "default pageID reached when all options " +
                        "should be specified: pageID " + Integer.toString(pageId));
                break;

        }
    }

    // Interface methods for status fragments to save/set buttons and handle button clicks.
    @Override
    public void setButtonsById(View view, int pageId, View.OnClickListener listener) {
        // Get references to button icons
        buttonOne = (AppCompatImageButton) view.findViewById(com.hamdam.hamdam.R.id.optionOne);
        buttonTwo = (AppCompatImageButton) view.findViewById(com.hamdam.hamdam.R.id.optionTwo);
        buttonThree = (AppCompatImageButton) view.findViewById(com.hamdam.hamdam.R.id.optionThree);
        buttonFour = (AppCompatImageButton) view.findViewById(com.hamdam.hamdam.R.id.optionFour);

        buttonOne.setBackgroundResource(com.hamdam.hamdam.R.drawable.button_selector);
        buttonTwo.setBackgroundResource(com.hamdam.hamdam.R.drawable.button_selector);
        buttonThree.setBackgroundResource(com.hamdam.hamdam.R.drawable.button_selector);
        buttonFour.setBackgroundResource(com.hamdam.hamdam.R.drawable.button_selector);

        // Set onClick listeners
        buttonOne.setOnClickListener(listener);
        buttonTwo.setOnClickListener(listener);
        buttonThree.setOnClickListener(listener);
        buttonFour.setOnClickListener(listener);

        switch (pageId) {
            // If a two option question, set visibility to gone.
            case StatusEnum.Topics.SEX:
                buttonOne.setImageResource(com.hamdam.hamdam.R.drawable.icon_protected_selector);
                buttonTwo.setImageResource(com.hamdam.hamdam.R.drawable.icon_unprotected_selector);
                buttonThree.setVisibility(View.GONE);
                buttonFour.setVisibility(View.GONE);
                break;

            case StatusEnum.Topics.EXERCISE:
                buttonOne.setImageResource(com.hamdam.hamdam.R.drawable.icon_exercise_selector);
                buttonTwo.setVisibility(View.GONE);
                buttonThree.setVisibility(View.GONE);
                buttonFour.setVisibility(View.GONE);

                break;

            case StatusEnum.Topics.SLEEP:
                buttonOne.setImageResource(com.hamdam.hamdam.R.drawable.icon_3sleep_selector);
                buttonTwo.setImageResource(com.hamdam.hamdam.R.drawable.icon_6sleep_selector);
                buttonThree.setImageResource(com.hamdam.hamdam.R.drawable.icon_9sleep_selector);
                buttonFour.setImageResource(com.hamdam.hamdam.R.drawable.icon_9plus_sleep_selector);

                break;


            case StatusEnum.Topics.MOOD:
                buttonOne.setImageResource(com.hamdam.hamdam.R.drawable.icon_positivemood_selector);
                buttonTwo.setImageResource(com.hamdam.hamdam.R.drawable.icon_neutralmood_selector);
                buttonThree.setImageResource(com.hamdam.hamdam.R.drawable.icon_negativemood_selector);
                buttonFour.setImageResource(com.hamdam.hamdam.R.drawable.icon_pmsmood_selector);

                break;

            case StatusEnum.Topics.PAIN: // Note: These are specifically ordered and order matters!
                // 1: headache; 2: cramps, 3: back pain, 4: breast tenderness.
                buttonOne.setImageResource(com.hamdam.hamdam.R.drawable.icon_headache_selector);
                buttonTwo.setImageResource(com.hamdam.hamdam.R.drawable.icon_cramps_selector);
                buttonThree.setImageResource(com.hamdam.hamdam.R.drawable.icon_backpain_selector);
                buttonFour.setImageResource(com.hamdam.hamdam.R.drawable.icon_tenderbreasts_selector);

                break;

            case StatusEnum.Topics.FLUIDS:
                // Note: These are specifically ordered and order matters!
                // 1: sticky, 2: creamy, 3: egg white, 4: atypical.
                buttonOne.setImageResource(com.hamdam.hamdam.R.drawable.icon_stickyfluids_selector);
                buttonTwo.setImageResource(com.hamdam.hamdam.R.drawable.icon_creamyfluids_selector);
                buttonThree.setImageResource(com.hamdam.hamdam.R.drawable.icon_eggwhitefluids_selector);
                buttonFour.setImageResource(com.hamdam.hamdam.R.drawable.icon_atypical_selector);

                break;

            case StatusEnum.Topics.BLEEDING:
                buttonOne.setImageResource(com.hamdam.hamdam.R.drawable.icon_bleeding1_selector);
                buttonTwo.setImageResource(com.hamdam.hamdam.R.drawable.icon_bleeding2_selector);
                buttonThree.setImageResource(com.hamdam.hamdam.R.drawable.icon_bleeding3_selector);
                buttonFour.setImageResource(com.hamdam.hamdam.R.drawable.icon_bleeding4_selector);

                break;

            default:
                Log.e(TAG, "default pageID reached when all options " +
                        "should be specified: pageID " + Integer.toString(pageId));
                break;
        }
    }

    public PresenterContracts.QuestionPresenter getPresenter() {
        return this.mPresenter;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int newPosition) {
        QuizIconFragment mTargetFragment = adapter.getRegisteredTitleFragment(newPosition);
        QuizIconFragment mPrevFragment = adapter.getRegisteredTitleFragment(previousIndex);

        AnimateUtils.scaleUp(mTargetFragment.getIcon());
        AnimateUtils.scaleDown(mPrevFragment.getIcon());
        AnimateUtils.quickBrighten(mTargetFragment.getTopicText());
        AnimateUtils.quickDim(mPrevFragment.getTopicText());

        previousIndex = newPosition;
        TabLayout.Tab tab = mTabs.getTabAt(newPosition);
        if (tab != null) {
            tab.select();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /*
     * Adapter for the daily status questions, contained in a swipeable Viewpager.
     */
    private class CustomPagerAdapter extends FragmentPagerAdapter {

        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredAnswerFragments.put(position, (QuizStatusFragment) fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            // Buttons etc are determined based on position Id.
            boolean multiAnswer = StatusEnum.isMultiAnswer
                    (StatusEnum.StatusType.getByTag(position));
            return QuizStatusFragment.newInstance(position, multiAnswer);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredAnswerFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public QuizStatusFragment getRegisteredAnswerFragment(int position) {
            return registeredAnswerFragments.get(position);
        }

    }

    /*
     * Adapter for the daily status title icons, contained in a swipeable Viewpager.
     */
    private class CustomTitlePagerAdapter extends FragmentPagerAdapter {

        public CustomTitlePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public float getPageWidth(int position) {
            return (1.0f);
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            // Set only active icon's properties.
            return QuizIconFragment.newInstance(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredTitleFragments.put(position, (QuizIconFragment) fragment);
            boolean selected = (position == 0);
            mTabs.addTab(mTabs.newTab(), selected);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredTitleFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public QuizIconFragment getRegisteredTitleFragment(int position) {
            return registeredTitleFragments.get(position);
        }

    }

    private void startTooltipSequence() {
        ShowcaseConfig showcaseConfig = new ShowcaseConfig();
        showcaseConfig.setDelay(200); // ms

        MaterialShowcaseSequence sequence =
                new MaterialShowcaseSequence(this);

        // Set up showcase items
        sequence.setConfig(showcaseConfig);

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(
                this)
                .setTarget(titleIconPager)
                .setDelay(0)
                .setDismissOnTouch(true)
                .setContentText(getString(com.hamdam.hamdam.R.string.tooltips_questions_howto_blurb))
                .setFadeDuration(500)
                .setMaskColour(ContextCompat.getColor(this,
                        com.hamdam.hamdam.R.color.dim_background))
                .setDismissText(getString(com.hamdam.hamdam.R.string.tooltips_dismiss_casual))
                .withoutShape()
                .build()
        );

        // Set completed to true
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_question_key), true)
                .apply();

        sequence.start();
    }

}
