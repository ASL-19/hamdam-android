package com.hamdam.hamdam.service;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.test.mock.MockContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock context to test notifications and intents.
 * Thanks to Matt Thompson for his implementation.
 */
public class TestContext extends MockContext {

    private List<Intent> mReceivedIntents = new ArrayList<>();
    private Context mContext;

    public TestContext(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public String getPackageName() {
        return "com.hamdam.hamdam.test";
    }

    @Override
    public void startActivity(Intent xiIntent) {
        mReceivedIntents.add(xiIntent);
    }

    public List<Intent> getReceivedIntents() {
        return mReceivedIntents;
    }

    @Override
    public Resources getResources() {
        return mContext.getResources();
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return mContext.registerReceiver(receiver, filter);
    }

    @Override
    public ContentResolver getContentResolver() {
        return mContext.getContentResolver();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return mContext.getSharedPreferences(name, mode);
    }

}
