package com.hamdam.hamdam.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.hamdam.hamdam.R;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.view.fragment.SplashFragment;
import com.hamdam.hamdam.view.fragment.onboardfragments.OnboardOptionFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import com.hamdam.hamdam.adapters.NonSwipeViewPager;
import com.hamdam.hamdam.di.BaseApplication;
import com.hamdam.hamdam.view.fragment.onboardfragments.OnboardDateFragment;

import javax.inject.Inject;

import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_SKIP;
import static com.hamdam.hamdam.Constants.ANALYTICS_CATEOGRY_ONBOARDING;


public class OnboardingActivity extends BaseActivity
        implements PresenterContracts.OnboardView {
    private static final String TAG = "OnboardingActivity";

    private CustomPagerAdapter mPagerAdapter;
    private NonSwipeViewPager mViewPager;
    private static final int COUNT = 5;
    private Button next;
    private Button skip;
    private SmartTabLayout mIndicator;
    @Inject
    SharedPreferences mSharedPreferences;

    // Store active Fragments in order to save data. Thanks to stackoverflow user @streetsofboston
    // for this solution.
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();


    @Override
    protected void onResume() {
        super.onResume();

        // Analytics
        if (mTracker != null) {
            mTracker.setScreenName(getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        ((BaseApplication) getApplication()).getComponent().inject(this); // Dagger 2

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up viewpager, tabs and buttons
        mViewPager = (NonSwipeViewPager) findViewById(R.id.viewPager);
        mPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        mIndicator = (SmartTabLayout) findViewById(R.id.indicator);
        skip = (Button) findViewById(R.id.skip);
        next = (Button) findViewById(R.id.next);

        mViewPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mViewPager);

        // Set current item to last item for default right-to-left swipe.
        mViewPager.setCurrentItem(COUNT - 1);
        next.setText(getString(com.hamdam.hamdam.R.string.get_started));

        // Set click handler for 'skip'
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTracker != null) {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(ANALYTICS_CATEOGRY_ONBOARDING)
                            .setAction(ANALYTICS_ACTION_SKIP)
                            .build());
                }
                if (mViewPager.getCurrentItem() != (COUNT - 1)
                        && mViewPager.getCurrentItem() != (COUNT - 2)) { // can't skip first question or title screen; rtl scroll means 'first' is last.
                    saveOnboardingData();
                    finishOnboarding();
                }
            }
        });

        // Set click handler for 'next' (just like skip)
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 0) { // because onboarding scrolls right to left
                    saveOnboardingData();
                    finishOnboarding();
                } else if (mViewPager.getCurrentItem() == COUNT - 1) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                } else {
                    saveOnboardingData();
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                }
            }
        });

        // Set progress indicator tab
        mIndicator.setOnPageChangeListener(new NonSwipeViewPager.SimpleOnPageChangeListener() {

            // When onboarding is finished, display "Done" instead of "Next"
            @Override
            public void onPageSelected(int position) {
                if (position == 0) { // because scrolls right to left
                    skip.setVisibility(View.GONE);
                    next.setText(com.hamdam.hamdam.R.string.done);
                } else if (position == COUNT - 1 ) { // Splash
                    skip.setVisibility(View.GONE);
                    next.setText(getString(com.hamdam.hamdam.R.string.get_started));
                } else if (position == COUNT - 2) {
                    skip.setVisibility(View.GONE); // Can't skip first question
                    next.setText(com.hamdam.hamdam.R.string.next);
                } else {
                    skip.setVisibility(View.VISIBLE); // scroll rtl
                    next.setText(com.hamdam.hamdam.R.string.next);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == (COUNT - 1)) {
            // Go to previous slide unless at the 'end' (recall: RTL scrolling)
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    public void saveOnboardingData() {
        Fragment onboardFragment = mPagerAdapter.getRegisteredFragment
                (mViewPager.getCurrentItem());
        OnboardOptionFragment nFragment;
        OnboardDateFragment dFragment;
        Resources resources = getResources();
        int value;
        try {
            switch (mViewPager.getCurrentItem()) {
                case 0: // Birth control fragment
                    nFragment = (OnboardOptionFragment) onboardFragment;
                    value = nFragment.getValue();
                    String birthControlMethod = resources.getStringArray
                            (com.hamdam.hamdam.R.array.birthControlEntries)[value];
                    mSharedPreferences.edit().putString(resources.getString
                            (com.hamdam.hamdam.R.string.birth_control_key), birthControlMethod).apply();
                    break;
                case 1:
                    nFragment = (OnboardOptionFragment) onboardFragment;
                    value = nFragment.getValue();
                    if (value > 0) {
                        mSharedPreferences.edit().putString
                                (resources.getString(com.hamdam.hamdam.R.string.cycle_length_key),
                                        Integer.toString(value)).apply();
                    }
                    break;
                case 2:
                    nFragment = (OnboardOptionFragment) onboardFragment;
                    value = nFragment.getValue();
                    mSharedPreferences.edit().putString(resources.getString(com.hamdam.hamdam.R.string.period_length_key),
                            Integer.toString(value)).apply();
                    break;
                case 3:
                    dFragment = (OnboardDateFragment) onboardFragment;
                    mSharedPreferences.edit().putString(resources.getString(com.hamdam.hamdam.R.string.last_period_date_key),
                            dFragment.getValue()).apply();
                    break;
                case 4:
                    break; // Splash screen--nothing to save, should never reach here
                default:
                    Log.e(TAG, "Unexpcted Fragment id: " +
                            Integer.toString(mViewPager.getCurrentItem()));
                    break;
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "Invalid cast of fragment " +
                    Integer.toString(mViewPager.getCurrentItem()));
        }
    }

    @Override
    public void setInstructions(TextView textView, int position) {
        switch (position) {
            case 0:
                textView.setText(getResources().getString(com.hamdam.hamdam.R.string.onboarding_question_birth_control));
                break;
            case 1:
                textView.setText(getResources().getString(com.hamdam.hamdam.R.string.onboarding_question_total_cycle_length));
                break;
            case 2:
                textView.setText(getResources().getString(com.hamdam.hamdam.R.string.onboarding_question_period_length));
                break;
            case 3:
                textView.setText(getResources().getString(com.hamdam.hamdam.R.string.onboarding_question_last_period));
                break;
            default:
                break;
        }
    }

    public void finishOnboarding() {
        mSharedPreferences.edit()
                .putBoolean(getString(com.hamdam.hamdam.R.string.onboarding_complete_key), true).apply();

        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }


    private class CustomPagerAdapter extends FragmentPagerAdapter {

        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                // Figure out what order the fragments are coming in and create new ones
                case 0:
                    return OnboardOptionFragment.newInstance(position);
                case 1:
                    return OnboardOptionFragment.newInstance(position);
                case 2:
                    return OnboardOptionFragment.newInstance(position);
                case 3:
                    return OnboardDateFragment.newInstance(position);
                case 4:
                    return SplashFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

}
