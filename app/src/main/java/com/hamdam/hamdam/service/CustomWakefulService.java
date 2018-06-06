package com.hamdam.hamdam.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hamdam.hamdam.util.AlarmUtil;

import java.lang.ref.WeakReference;

/**
 * Application service to handle calendar events and system broadcasts.
 */
public class CustomWakefulService extends IntentService {
    private static final String TAG = "WakefulService";
    public static final String ACTION_REBOOT_ALL = "ActionReboot",
            ACTION_NOTIFY = "ACTION_NOTIFY",
            ACTION_REBOOT_SINGLE = "ACTION_REBOOT_SINGLE";

    public CustomWakefulService() {
        super(CustomWakefulService.class.getName());
    }

    private static WeakReference<CustomWakefulService> instance;

    @Nullable
    public static CustomWakefulService getInstance() {
        return instance == null ? null : instance.get();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        instance = new WeakReference<>(this);
        String action = intent.getAction();
        switch (action) {
            case ACTION_NOTIFY:
                int code = intent.getIntExtra(AlarmUtil.CODE_ID, 0);
                Context context = getApplicationContext();

                // As per security recommendations, the message displayed is
                // just a generic "New Reminder" message.
                // Using Alarm.getBody(context, code, defaultString) would produce
                // specific messages depending on the type of each reminder.
                // Future feature: offer the user the choice in preferences menu
                // whether to display specific message or generic reminder.

                // Note: due to the resource identifier (int) potentially across app updates,
                // look up int at time of composing notification.
                int defaultTitle = getResources().getIdentifier("app_name", "string", getPackageName());
                int defaultBody = getResources().getIdentifier("default_notification_string", "string", getPackageName());

                AlarmUtil.sendNotification(getApplicationContext(),
                        AlarmUtil.getTitle(context,
                                code,
                                getString(defaultTitle)),
                        getString(defaultBody),
                        code);

                AlarmUtil.setHasFired(context, code, true);
                CustomBroadcastReceiver.completeWakefulIntent(intent);
                break;
            case ACTION_REBOOT_ALL:

                // Check to make sure correct alarm times are set, and reboot alarms.
                AlarmUtil.rebootAlarms(getApplicationContext());
                CustomBroadcastReceiver.completeWakefulIntent(intent);
                break;
            case ACTION_REBOOT_SINGLE:

                // Update single alarm
                Context cxt = getApplicationContext();
                int alarmCode = intent.getIntExtra(AlarmUtil.CODE_ID, 0);
                AlarmUtil.rebootSingleAlarm(cxt,
                        AlarmUtil.getKey(cxt, alarmCode), alarmCode);
                CustomBroadcastReceiver.completeWakefulIntent(intent);
                break;
            default:
                Log.e(TAG, "Unexpected intent action " + action);
                break;
        }
    }
}
