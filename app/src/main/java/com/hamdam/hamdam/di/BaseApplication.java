package com.hamdam.hamdam.di;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.hamdam.hamdam.service.update.CustomAmazonReceiver;
import com.hamdam.hamdam.Constants;
import com.hamdam.hamdam.util.AlarmUtil;
import com.hamdam.hamdam.util.LocaleUtils;

import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static com.hamdam.hamdam.Constants.TRACKER;

/**
 * Extend Application to include dependency injection and force Farsi language across app.
 */
public class BaseApplication extends Application {
	private static final String TAG = "BaseApplication",
            GOOGLE_PLAY_MARKET = "com.android.vending";
    private ApplicationComponent mComponent;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/iransans.ttf")
                .setFontAttrId(com.hamdam.hamdam.R.attr.fontPath)
                .build()
        );

        LocaleUtils.setLocale(new Locale("fa"));
        LocaleUtils.updateLocale(this, getBaseContext().getResources().getConfiguration());
        AlarmUtil.rebootAlarms(this); // check if user data has changed prediction-based alarms

        setTheme(com.hamdam.hamdam.R.style.HamdamTheme);

        mComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        mComponent.inject(this);

        // If app not downloaded from google play, check for updates
        String installer = getPackageManager().getInstallerPackageName(getPackageName());
        if (installer == null || !installer.equals(GOOGLE_PLAY_MARKET)) {
            Intent checkUpdateIntent = new Intent(this, CustomAmazonReceiver.class);
            checkUpdateIntent.setAction(Constants.INTENT_ACTION_CHECK_VERSION_CODE);
            sendBroadcast(checkUpdateIntent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateLocale(this, newConfig);
    }

    /**
     * Snippet from developers.google.com:
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    public synchronized Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(TRACKER);
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }

    public ApplicationComponent getComponent() {
        return mComponent;
    }
}
