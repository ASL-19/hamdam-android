package com.hamdam.hamdam.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.github.ebraminio.droidpersiancalendar.Constants;
import com.hamdam.hamdam.util.AlarmUtil;

import static com.hamdam.hamdam.Constants.BROADCAST_REBOOT_ALARM;

/**
 * WakefulBroadcastReceiver to manage reminder notifications/alarms.
 */
public class CustomBroadcastReceiver extends WakefulBroadcastReceiver {
	private static final String TAG = "CustomBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null
                && !TextUtils.isEmpty(intent.getAction())) {

            if (intent.getAction().equals(Constants.BROADCAST_RESTART_APP)
                    || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                    || intent.getAction().equals(Intent.ACTION_DATE_CHANGED)
                    || intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)
                    || (intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED))) {

                // Simple intentService to check/update alarm settings.
                Intent rebootIntent = new Intent(context,
                        CustomWakefulService.class);
                rebootIntent.setAction(CustomWakefulService.ACTION_REBOOT_ALL);
                startWakefulService(context, rebootIntent);

            } else if (intent.getAction().equals(com.hamdam.hamdam.Constants.BROADCAST_START_ALARM)) {
                int code = intent.getIntExtra(AlarmUtil.CODE_ID, 0);

                // Start service to show event notification on user's home screen.
                Intent notifyIntent = new Intent(context,
                        CustomWakefulService.class);
                notifyIntent.setAction(CustomWakefulService.ACTION_NOTIFY);
                notifyIntent.putExtra(AlarmUtil.CODE_ID, code);
                startWakefulService(context, notifyIntent);

            // Rescheduling an alarm for the next cycle/interval has to occur separately from
            // sending the notification, otherwise notification could fire repeatedly for early reminders
            } else if (intent.getAction().equals(BROADCAST_REBOOT_ALARM)) {
                int code = intent.getIntExtra(AlarmUtil.CODE_ID, 0);

                // Schedule new alarm
                Intent updateIntent = new Intent(context,
                        CustomWakefulService.class);
                updateIntent.setAction(CustomWakefulService.ACTION_REBOOT_SINGLE);
                updateIntent.putExtra(AlarmUtil.CODE_ID, code);
                startWakefulService(context, updateIntent);
            }
        }
    }

}
