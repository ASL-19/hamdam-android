package com.hamdam.hamdam.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.ebraminio.droidpersiancalendar.utils.Utils;

import com.hamdam.hamdam.presenter.DatabaseHelperImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides methods for dependency injection, appliation level.
 */
@Module
public class ApplicationModule {
    private Application mApplication;

    public ApplicationModule(Application app) {
        this.mApplication = app;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

    @Provides
    @Singleton
    Utils provideUtils() {
        return Utils.getInstance(mApplication);
    }


    @Provides
    @Singleton
    DatabaseHelperImpl provideDatabaseHelper() {
        return DatabaseHelperImpl.getInstance(mApplication);
    }

    @Provides
    @Singleton
    Context provideApplicationContext() { // Make sure to use only when app context truly needed
        return mApplication;
    }
}
