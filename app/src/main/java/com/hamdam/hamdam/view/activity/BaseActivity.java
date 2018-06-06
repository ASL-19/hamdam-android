package com.hamdam.hamdam.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.google.android.gms.analytics.Tracker;
import com.hamdam.hamdam.di.BaseApplication;
import com.hamdam.hamdam.util.LocaleUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * BaseActivity class to force language change.
 */
public class BaseActivity extends AppCompatActivity {
    protected Tracker mTracker;

    public BaseActivity() {
        LocaleUtils.updateLocale(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mTracker = ((BaseApplication) getApplication()).getDefaultTracker();
    }

    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public Tracker getTracker() {
        return mTracker;
    }
}
