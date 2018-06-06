package com.hamdam.hamdam.view.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.ebraminio.droidpersiancalendar.adapters.DrawerAdapter;
import com.google.android.gms.analytics.HitBuilders;
import com.hamdam.hamdam.BuildConfig;
import com.hamdam.hamdam.Constants;
import com.hamdam.hamdam.R;
import com.hamdam.hamdam.di.BaseApplication;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.service.update.CustomAmazonReceiver;
import com.hamdam.hamdam.util.AlarmUtil;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.view.fragment.AboutHamdamFragment;
import com.hamdam.hamdam.view.fragment.DomesticRightsInfoFragment;
import com.hamdam.hamdam.view.fragment.DomesticViolenceHotlineFragment;
import com.hamdam.hamdam.view.fragment.HealthInfoFragment;
import com.hamdam.hamdam.view.fragment.HomePageFragment;
import com.hamdam.hamdam.view.fragment.InfographicFragment;
import com.hamdam.hamdam.view.fragment.LicenseFragment;
import com.hamdam.hamdam.view.fragment.NotificationPreferenceFragment;
import com.hamdam.hamdam.view.fragment.PeriodCalendarFragment;
import com.hamdam.hamdam.view.fragment.PrivacyFragment;
import com.hamdam.hamdam.view.fragment.UserPreferenceFragment;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_CLICK;
import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_SKIP;
import static com.hamdam.hamdam.Constants.ANALYTICS_CATEGORY_UPDATE;
import static com.hamdam.hamdam.Constants.UPDATE_NOTIFICATION_ID;


/**
 * Modified MainActivity
 */
public class MainActivity extends BaseActivity implements
        PresenterContracts.NavigationView {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_WRITE_STORAGE = 107;
    private Date mCurrentDate;
    private BroadcastReceiver updateReceiver;
    @Inject
    SharedPreferences mSharedPreferences;

    private DrawerLayout drawerLayout;
    private DrawerAdapter adapter;

    private static final Map<Integer, Class<?>> mFragmentMap
            = Collections.unmodifiableMap(setNavigationClasses());

    private static Map<Integer, Class<?>> setNavigationClasses() {
        HashMap<Integer, Class<?>> navigationClasses = new HashMap<>();
        navigationClasses.put(1, HomePageFragment.class);
        navigationClasses.put(2, UserPreferenceFragment.class);
        navigationClasses.put(3, NotificationPreferenceFragment.class);
        navigationClasses.put(4, PeriodCalendarFragment.class);
        navigationClasses.put(5, InfographicFragment.class);
        navigationClasses.put(6, HealthInfoFragment.class);
        navigationClasses.put(7, DomesticRightsInfoFragment.class);
        navigationClasses.put(8, DomesticViolenceHotlineFragment.class);
        navigationClasses.put(9, AboutHamdamFragment.class);
        navigationClasses.put(10, PrivacyFragment.class);
        navigationClasses.put(11, LicenseFragment.class);

        return navigationClasses;
    }

    // Default selected fragment
    private static final int DEFAULT = 1; // Homepage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(com.hamdam.hamdam.R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        mCurrentDate = DateUtil.clearTimeStamp(new Date());

        ((BaseApplication) getApplication()).getComponent().inject(this); // Dagger 2

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_APP_UPDATE_FAILURE)) {
                    Toast.makeText(MainActivity.this,
                            context.getString(com.hamdam.hamdam.R.string.error_update_failed),
                            Toast.LENGTH_LONG).show();
                } else if (intent.getAction().equals(Constants.BROADCAST_APP_UPDATE_SUCCESS)) {
                    Toast.makeText(MainActivity.this,
                            getString(com.hamdam.hamdam.R.string.download_success),
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_APP_UPDATE_FAILURE);
        filter.addAction(Constants.BROADCAST_APP_UPDATE_SUCCESS);

        registerReceiver(updateReceiver, filter);

        // Check if onboarding_has been completed
        if (!mSharedPreferences.getBoolean(getString(R.string.onboarding_complete_key), false)) {

            // Restart the tooltips
            mSharedPreferences
                    .edit()
                    .putBoolean(getString(R.string.tooltips_complete_calendar_key), false)
                    .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_homepage_key), false)
                    .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_question_key), false)
                    .apply();

            // Start the onboarding Activity
            Intent onboarding = new Intent(this, OnboardingActivity.class);
            startActivity(onboarding);

            // Close the main activity, return
            finish();
            return;
        }

        // Check if app needs update
        if (mSharedPreferences.getBoolean(Constants.APP_NEEDS_UPDATE_KEY, false)) {
            requestAppUpdate();
        }

        // Check if version was just updated. If so, cancel update notification
        int versionCode = mSharedPreferences.getInt(Constants.VERSION_CODE, -1);
        if (versionCode == -1 || BuildConfig.VERSION_CODE > versionCode) { // a new install
            mSharedPreferences.edit()
                    .putInt(Constants.VERSION_CODE, BuildConfig.VERSION_CODE)
                    .apply();
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(UPDATE_NOTIFICATION_ID);
        }

        Toolbar toolbar = (Toolbar) findViewById(com.hamdam.hamdam.R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            ActionBar mActionBar = getSupportActionBar();
            mActionBar.setDefaultDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        RecyclerView navigation = (RecyclerView) findViewById(R.id.navigation_view);
        if (navigation != null) {
            navigation.setHasFixedSize(true);

            adapter = new DrawerAdapter(this);
            navigation.setAdapter(adapter);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            navigation.setLayoutManager(layoutManager);
        }
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        final View appMainView = findViewById(R.id.app_main_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, com.hamdam.hamdam.R.string.openDrawer, com.hamdam.hamdam.R.string.closeDrawer) {
            int slidingDirection = +1;

            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        && isRTL()) {
                    slidingDirection = -1;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    slidingAnimation(drawerView, slideOffset);
                }
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            private void slidingAnimation(View drawerView, float slideOffset) {
                appMainView.setTranslationX(slideOffset * drawerView.getWidth() * slidingDirection);
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // If launching app, show default view
        if (savedInstanceState == null) {
            selectItem(DEFAULT);
        }

        // Check if app launched from a notification
        Intent intent = getIntent();
        checkNotificationIntent(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isRTL() {
        return getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private void requestAppUpdate() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.update_prompt_title))
                .setMessage(getString(R.string.update_prompt))
                .setPositiveButton(getString(com.hamdam.hamdam.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // See if user has granted write permissions. If not,
                        // request permissions to download new apk.
                        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_WRITE_STORAGE);
                        } else {
                            sendUpdateIntent();
                        }

                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory(ANALYTICS_CATEGORY_UPDATE)
                                .setAction(ANALYTICS_ACTION_CLICK)
                                .build());

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(com.hamdam.hamdam.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory(ANALYTICS_CATEGORY_UPDATE)
                                .setAction(ANALYTICS_ACTION_SKIP)
                                .build());
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendUpdateIntent();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Request download of newer version of app
    private void sendUpdateIntent() {
        Intent getUpdateIntent = new Intent(MainActivity.this, CustomAmazonReceiver.class);
        getUpdateIntent.setAction(Constants.INTENT_ACTION_START_UPDATE);
        sendBroadcast(getUpdateIntent);
    }

    // Check if activity has received a notification intent, and display information dialog if needed
    private void checkNotificationIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            if (intent.getExtras().containsKey(getString
                    (com.hamdam.hamdam.R.string.bundle_is_notification_intent))
                    && intent.getExtras().getBoolean(getString
                    (com.hamdam.hamdam.R.string.bundle_is_notification_intent), false)) {

                // Cancel notification, since user clicked it to be there.
                NotificationManager mManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                int requestCode = intent.getExtras().getInt(getString(com.hamdam.hamdam.R.string.bundle_notification_id));
                mManager.cancel(requestCode);

                // Show message corresponding to notification.
                // Originally shown on the home screen, these are shown in the app for
                // increased user privacy.
                String title = null, message = null;
                switch (requestCode) {
                    case AlarmUtil.RequestCodes.BREAST_EXAM_ALARM:
                        title = getString(com.hamdam.hamdam.R.string.dialog_breast_exam_title);
                        message = getString(com.hamdam.hamdam.R.string.dialog_breast_exam_message);
                        break;
                    case AlarmUtil.RequestCodes.MEDICATION_ALARM:
                    case AlarmUtil.RequestCodes.PERIOD_ALARM:
                    case AlarmUtil.RequestCodes.PMS_ALARM:
                        title = AlarmUtil.getTitle(this, requestCode,
                                getString(com.hamdam.hamdam.R.string.default_notification_string));
                        message = AlarmUtil.getBody(this, requestCode,
                                null);
                        break;
                    default:
                        break;
                }
                // Display alert dialog with explanation of reminder.
                if (message != null) { // title is not null if message is not null
                    new AlertDialog.Builder(MainActivity.this, R.style.HamdamTheme_CustomDialogStyle)
                            .setTitle(title)
                            .setMessage(message)
                            .setNeutralButton(getString(com.hamdam.hamdam.R.string.dismiss), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .create()
                            .show();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkNotificationIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View v = findViewById(com.hamdam.hamdam.R.id.drawer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && v != null) {
            v.setLayoutDirection(isRTL() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    @SuppressWarnings("null")
    protected void onResume() {
        super.onResume();
        if (mCurrentDate.getTime()
                != DateUtil.clearTimeStamp(new Date()).getTime()) { // will not be null
            restartActivity();
        }
        if (mTracker != null) {
            mTracker.setScreenName(getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(updateReceiver);
        super.onDestroy();
    }

    public void onClickItem(int position) {
        selectItem(position);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(com.hamdam.hamdam.R.id.fragment_holder);
            if (currentFragment instanceof HomePageFragment) {
                finish();
            } else { // In a fragment; show home screen.
                selectItem(DEFAULT);
            }
        } else {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(com.hamdam.hamdam.R.id.fragment_holder);
            getSupportFragmentManager()
                    .popBackStackImmediate(currentFragment.getClass().getName(),
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Checking for the "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /*
     * Choose appropriate Fragment to display in MainActivity's fragment holder.
     * If no fragments are currently displayed, use the default fragment
     * specified by final int id DEFAULT.
     *
     */
    public void selectItem(int item) {
        boolean isNewActivity = false;
        Class currentClass, targetClass = mFragmentMap.get(item);
        if (getSupportFragmentManager()
                .findFragmentById(com.hamdam.hamdam.R.id.fragment_holder) == null) {
            currentClass = mFragmentMap.get(DEFAULT);
            isNewActivity = true;
        } else {
            currentClass = getSupportFragmentManager()
                    .findFragmentById(com.hamdam.hamdam.R.id.fragment_holder)
                    .getClass();
        }
        if (currentClass != targetClass || isNewActivity) {
            try {
                Fragment targetFragment =
                        (Fragment) targetClass.newInstance();
                getSupportFragmentManager()
                        .popBackStack(null,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(com.hamdam.hamdam.R.id.fragment_holder, targetFragment,
                                mFragmentMap.get(item).getName())
                        .addToBackStack(null)
                        .commit();
            } catch (InstantiationException e) {
                Log.e(TAG, e.getMessage());
            } catch (IllegalAccessException ex) { // Two catch clauses req'd
                Log.e(TAG, ex.getMessage());
            }
        }
        adapter.setSelectedItem(item);
        drawerLayout.closeDrawers();
    }

    @Override
    public void setNavigationPosition(int newPosition) {
        adapter.setSelectedItem(newPosition);
    }

    @Override
    public int getNavigationPosition(String tag) {
        for (int which : mFragmentMap.keySet()) {
            if (mFragmentMap.get(which).getName().equals(tag)) {
                return which;
            }
        }
        return DEFAULT;
    }
}
