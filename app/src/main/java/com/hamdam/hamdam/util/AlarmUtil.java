package com.hamdam.hamdam.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.hamdam.hamdam.Constants;
import com.hamdam.hamdam.R;
import com.hamdam.hamdam.view.activity.MainActivity;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.service.CustomBroadcastReceiver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.hamdam.hamdam.Constants.BROADCAST_REBOOT_ALARM;
import static com.hamdam.hamdam.Constants.BROADCAST_START_ALARM;

/**
 * Utility class for building alarm/notification schedules.
 */
public final class AlarmUtil {
    private static final String TAG = "AlarmUtil";
    public static final int DEFAULT_HOUR_ALARM = 9;
    private static final String GROUP_REMINDERS = "ReminderGroup"; // Notifications

    public static final String ALARM_TIME = "AlarmTime",
            CODE_ID = "CodeId",
            REBOOT_ALARM_TIME = "RebootAlarmTime",
            STORED_VALUE_KEY = "StoredValue",
            ALARM_HOUR_KEY = "AlarmHourKey", // Shared preference; don't change key.
            HAS_FIRED = "HasFired"; // Set/get shared preference; don't change key.


    public static final class RequestCodes {
        public static final int PERIOD_ALARM = 999;
        public static final int PMS_ALARM = 998;
        public static final int MEDICATION_ALARM = 997;
        public static final int BREAST_EXAM_ALARM = 996;
    }

    /*
     * Create new repeating alarm.
     * Sets a date for alarm to go off, and a 'reboot date' to reschedule alarm.
     * These dates are the same unless user has requested to be reminded early for an event,
     * in which case the reboot date is after the alarm schedule date.
     * If alarm reboot date has passed, do not set alarm.
     *
     * @param   context     Context
     * @param   setDate     target date om which alarm should fire
     * @param   rebootDate  target date on which to reschedule alarm. Used to prevent repeated
     *                      firings of same alarm if user asks to be reminded of event before
     *                      it occurs.
     * @param   requestCode id of alarm (there are multiple types of alarms in app) to facilitate
     *                      canceling/updating alarm.
     */
    @NonNull
    public static Date setAlarm(Context context, final Date setDate, final Date rebootDate,
                                final int requestCode) {

        // Create pendingIntents for alarm broadcasts, assign action strings.
        Date realAlarmDate = setAlarmIntent(context, setDate, BROADCAST_START_ALARM, requestCode);

        setAlarmIntent(context, rebootDate, BROADCAST_REBOOT_ALARM, requestCode);

        // Return day that notification (alarm) is scheduled
        return realAlarmDate;
    }

    /*
     * Return the next date an alarm should go off given an identifier (request code)
     * indicating alarm type.
     * Breast exam and medication alarms return dates depending on user's specified preferences.
     * Period and PMS alarms return dates in future depending on user's cycle data.
     *
     * @param   context     Current context
     * @param   requestCode integer identifier of alarm type
     * @returns Date    Date to set next alarm, excluding user offset preference.
     *                  Default date + 1 day in future.
     *                  Note that if user specifies an offset ("Remind me 3 days early"),
     *                  it is not included in this calculation and is calculated afterwards.
     *                  This option is only available for menstrual calendar reminders
     *                  (Period/PMS).
     */
    @Nullable
    public static Date getAlarmDateNoOffset(final Context context,
                                            final int requestCode) {
        Calendar calendar = new GregorianCalendar();
        Date nextDate = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        switch (requestCode) {
            case RequestCodes.BREAST_EXAM_ALARM:
                int repeatInterval = Integer.parseInt(preferences.getString
                        (context.getString(com.hamdam.hamdam.R.string.breast_exam_reminder_key)
                                + STORED_VALUE_KEY, "-1"));
                if (repeatInterval < 0) {
                    repeatInterval = context.getResources().getInteger(com.hamdam.hamdam.R.integer.monthly);
                } else {
                    repeatInterval--;
                }
                calendar.add(Calendar.DATE, repeatInterval);
                nextDate = calendar.getTime();
                break;

            case RequestCodes.MEDICATION_ALARM:
                int interval = Integer.parseInt(preferences.getString
                        (context.getString(com.hamdam.hamdam.R.string.birth_control_reminder_key)
                                + STORED_VALUE_KEY, "-1"));
                if (interval < 0) {
                    interval = context.getResources().getInteger(com.hamdam.hamdam.R.integer.daily);
                } else {
                    interval--;
                }
                calendar.add(Calendar.DATE, interval);
                nextDate = calendar.getTime();
                break;

            case RequestCodes.PERIOD_ALARM:
                nextDate = getNextPeriodDate(context);
                break;

            case RequestCodes.PMS_ALARM:
                nextDate = getNextPmsDate(context, calendar);
                break;

            default:
                Log.e(TAG, "Unexpected request code " + requestCode);
                break;
        }

        if (nextDate != null) {
            return assignNextAlarmDate(context, nextDate);
        } else {
            return null;
        }
    }

    /*
     * For predictive alarms (menstrual calendar):
     * Check target menstrual alarm date to see if it has already passed. If so, update
     * date by an increment corresponding to the length of the user's menstrual calendar,
     * or by the number of days they have specified in their user settings if they have disabled
     * prediction mode.
     *
     * @oaram   foundDate   Date representing next predicted alarm
     * @returns             foundDate, if foundDate is present or future date, or Date
     *                      representing foundDate incremented forward into user's current
     *                      menstrual cycle.
     */
    @SuppressWarnings("null") // DateConverter passed a valid date won't return null
    private static Date assignNextAlarmDate(Context context, @NonNull Date foundDate) {
        Date current = DateUtil.clearTimeStamp(new Date());
        if (foundDate.getTime() >= current.getTime()) { // will not be null
            return foundDate;
        } else {
            int increment;
            boolean usePredictionMode = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getBoolean(context
                            .getString(com.hamdam.hamdam.R.string.enable_prediction_mode_key), true);
            if (usePredictionMode) {
                increment = DatabaseHelperImpl.getInstance(context).getAverageCycleLength();
            } else {
                increment = Integer.parseInt(PreferenceManager
                        .getDefaultSharedPreferences(context).getString
                                (context.getString(com.hamdam.hamdam.R.string.cycle_length_key),
                                        Integer.toString(Constants.DEFAULT_CYCLE_LENGTH)));
            }
            Date tempDate = foundDate;

            // Advance until in current range
            while (tempDate != null && tempDate.getTime() < current.getTime()) {
                tempDate = DateUtil.rollCalendar(tempDate, increment);
            }
            return tempDate;
        }
    }

    @Nullable
    private static Date getNextPmsDate(Context context, Calendar calendar) {
        Date nextPmsDate = DatabaseHelperImpl
                .getInstance(context)
                .projectNextStartDate(new Date());
        String defaultPmsString = Integer
                .toString(context.getResources()
                        .getInteger(com.hamdam.hamdam.R.integer.default_pms_length));
        Integer pmsInt = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(context.getString(com.hamdam.hamdam.R.string.pms_length_key),
                        defaultPmsString));
        if (nextPmsDate != null) {
            calendar.setTime(nextPmsDate);
            calendar.add(Calendar.DATE, -pmsInt); // Pms is earlier; 'add' negative
            return calendar.getTime();
        }
        return null;
    }

    @Nullable
    private static Date getNextPeriodDate(Context context) {
        Date nextPeriodDate = DatabaseHelperImpl
                .getInstance(context)
                .projectNextStartDate(new Date());
        return (nextPeriodDate == null ? null : nextPeriodDate);
    }


    /*
     * Cancel a given alarm by request code.
     */
    public static void cancelAlarm(Context context, final int requestCode) {

        // Cancel alarm
        Intent intent = new Intent(context, CustomBroadcastReceiver.class);
        intent.putExtra(CODE_ID, requestCode);
        intent.setAction(BROADCAST_START_ALARM);

        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(context, requestCode, intent,
                        PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            pendingIntent.cancel();
            alarmManager.cancel(pendingIntent);
        }
    }
    /*
         * Cancel a given alarm by request code.
         */
    public static void cancelReboot(Context context, final int requestCode) {

        // Cancel alarm
        Intent intent = new Intent(context, CustomBroadcastReceiver.class);
        intent.putExtra(CODE_ID, requestCode);
        intent.setAction(BROADCAST_REBOOT_ALARM);

        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(context, requestCode, intent,
                        PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            pendingIntent.cancel();
            alarmManager.cancel(pendingIntent);
        }
    }

    /*
     * Display a notification to the user.
     * @param   context context
     * @param   body    String message content
     * @param   title   String title content
     */
    public static void sendNotification(Context context,
                                        final String title, final String body,
                                        final int notificationType) {
        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.putExtra(context.getString(com.hamdam.hamdam.R.string.bundle_is_notification_intent), true);
        launchIntent.putExtra(context.getString(com.hamdam.hamdam.R.string.bundle_notification_id), notificationType);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // make sure to use this intent

        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(context, notificationType,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setWhen(0)
                .setDefaults(Notification.DEFAULT_ALL) // Lights, sound, vibrate, priority
                .setContentIntent(launchAppPendingIntent)
                .setContentText(body)
                .setGroup(GROUP_REMINDERS)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.hamdam_statusbar_white); // this must be a PNG for older phone compat
        NotificationManager mManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mManager.notify(notificationType, mBuilder.build());
    }

    @Nullable
    public static String getKey(Context context, int code) {
        switch (code) {
            case RequestCodes.BREAST_EXAM_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.breast_exam_reminder_key);
            case RequestCodes.MEDICATION_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.birth_control_reminder_key);
            case RequestCodes.PERIOD_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.period_reminder_key);
            case RequestCodes.PMS_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.pms_reminder_key);
            default:
                return null;
        }
    }

    /*
     * Check offset value and adjust alarm date if valid offset specified.
     * "Offset" corresponds to a user-selected choice of reminder date:
     * "Never", "Same day," "One day early", "Two days early."
     * If user specifies "Never", the alarm date returned is null and the alarm is cancelled.
     */
    @Nullable
    public static Date adjustAlarmOffset(Context context, @Nullable Date date,
                                         final int requestCode) {
        String key = getKey(context, requestCode);
        if (date != null && key != null) {
            String offsetKey = key + STORED_VALUE_KEY;
            Integer offset = Integer
                    .parseInt(PreferenceManager
                            .getDefaultSharedPreferences(context)
                            .getString(offsetKey, "-1"));
            if (offset >= 0) {
                date = DateUtil.rollCalendar(date, -offset);
            } else { // Error, or user specified no alarm
                return null;
            }
        }
        return date;
    }

    /*
     * Once a push notification has been sent for an alarm, update scheduled alarm
     * to fire after scheduled interval and update Shared Preferences to store new
     * scheduled start time.
     * @param   context     context
     * @param   alarmRequestCode    int Id determining type of alarm
     *                              (medication, menstruation, etc)
     * @returns alarmSetDate        Date alarm is set to display notification (nextAlarmDate), or
     *                              null if alarm could not be successfully set.
     */
    @SuppressWarnings("null") // DateUtil.cleartimeStamp will not be null with non-null date
    public static Date updateAlarm(Context context, final int alarmRequestCode) {
        Date alarmSetDate = null; // Return as null if alarm not set, or return scheduled date if set
        Date nextAlarmDate = getAlarmDateNoOffset(context, alarmRequestCode); // returns a future date

        // If null, cancel the alarm.
        if (nextAlarmDate != null) {
            Date nextRebootDate = nextAlarmDate;

            // Check if user has specified "Remind me one day early", "two days early" etc
            // (only available on select alarms)
            if (alarmRequestCode == RequestCodes.PERIOD_ALARM ||
                    alarmRequestCode == RequestCodes.PMS_ALARM) {
                nextAlarmDate = adjustAlarmOffset(context, nextAlarmDate, alarmRequestCode);
            }

            // If user has disabled an alarm, nextDate is null; don't set alarms then or for past dates
            if (nextAlarmDate != null && nextRebootDate.getTime()
                    >= DateUtil.clearTimeStamp(new Date()).getTime()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String key = getKey(context, alarmRequestCode), rebootKey, alarmTimeKey;
                if (key != null) {
                    rebootKey = key + REBOOT_ALARM_TIME;
                    alarmTimeKey = key + ALARM_TIME;

                    // Update alarm start date in shared preferences
                    // (used to reschedule alarms on device reboot)
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(context);
                    prefs.edit()
                            .putString(alarmTimeKey, dateFormat.format(nextAlarmDate))
                            .putString(rebootKey, dateFormat.format(nextRebootDate))
                            .putBoolean(key + HAS_FIRED, false) // setting new alarm
                            .apply();

                    // Update alarm to new date (persists until device reboot)
                    alarmSetDate = setAlarm(context, nextAlarmDate, nextRebootDate, alarmRequestCode);
                }
            }
        } else { // Alarm set date was null, or alarm date has been missed; cancel alarm
            cancelAlarm(context, alarmRequestCode);
        }
        return alarmSetDate;
    }

    /*
     * Reset scheduled alarms on phone reboot by retrieving
     * active alarms from shared preferences. This is called only on device reboot.
     * @param context   Application Context
     */
    public static void rebootAlarms(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        final Resources resources = context.getResources();

        // Check if notifications are enabled before setting alarms
        if (prefs.getBoolean(resources
                .getString(com.hamdam.hamdam.R.string.notifications_key), false)) {

            final String breast_exam_key = resources
                    .getString(com.hamdam.hamdam.R.string.breast_exam_reminder_key),
                    birth_control_key = resources
                            .getString(com.hamdam.hamdam.R.string.birth_control_reminder_key),
                    period_key = resources
                            .getString(com.hamdam.hamdam.R.string.period_reminder_key),
                    pms_key = resources
                            .getString(com.hamdam.hamdam.R.string.pms_reminder_key);

            if (prefs.getBoolean(breast_exam_key, false)) {
                rebootSingleAlarm(context,
                        breast_exam_key,
                        RequestCodes.BREAST_EXAM_ALARM);
            }

            if (prefs.getBoolean(birth_control_key, false)) {
                rebootSingleAlarm(context,
                        birth_control_key,
                        RequestCodes.MEDICATION_ALARM);
            }

            if (prefs.getBoolean(period_key, false)) {
                rebootSingleAlarm(context,
                        period_key,
                        RequestCodes.PERIOD_ALARM);
            }

            if (prefs.getBoolean(pms_key, false)) {
                rebootSingleAlarm(context,
                        pms_key,
                        RequestCodes.PMS_ALARM);
            }
        }
    }

    /*
     * Set title message for push notfications based on notification type.
     * @param   context         context
     * @param   codeId          integer identifier indicating notification type
     * @param   defaultTitle     default title
     */
    @NonNull
    public static String getTitle(Context context, int codeId, String defaultTitle) {
        switch (codeId) {
            case RequestCodes.BREAST_EXAM_ALARM:
            case RequestCodes.MEDICATION_ALARM:
            case RequestCodes.PERIOD_ALARM:
            case RequestCodes.PMS_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.default_notification_title); // "New Reminder"
            default:
                return defaultTitle;
        }
    }

    /*
     * Set body message for push notfications based on notification type.
     * @param   context         context
     * @param   codeId          integer identifier indicating notification type
     * @param   defaultBody     default body if no other resources found
     */
    @NonNull
    public static String getBody(Context context, int codeId, String defaultBody) {
        switch (codeId) {
            case RequestCodes.BREAST_EXAM_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.notification_body_breast_exam);
            case RequestCodes.MEDICATION_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.notification_body_medication);
            case RequestCodes.PERIOD_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.notification_body_period);
            case RequestCodes.PMS_ALARM:
                return context.getString(com.hamdam.hamdam.R.string.notification_body_pms);
            default:
                return defaultBody;
        }
    }

    /*
     * Check if alarms are enabled and update menstruation alarms in the
     * event of new data being added that changes the user's menstrual cycle data.
     *
     * (Adding new data potentially changes the 'remind me of my next period/pms'
     * alarm schedule).
     *
     * This is different from rebooting alarms, which sets alarms based on
     * shared preferences (but does not update the shared preferences data),
     * or from updating alarms, which is called only after alarm enable state has been checked.
     *
     */

    /*
    * Reschedule an alarm based on its code (indicates scheduling type).
    * Next Alarm date is stored in shared preferences.
    * If Date cannot be parsed from shared preferences, or if alarm has already fired,
    * an alarm is not set.
    */
    @SuppressWarnings("null") // dateconverter will not return null checking existing dates.
    public static Date rebootSingleAlarm(Context context,
                                          String key,
                                          int requestCode) {
        Date date = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Date storedAlarmDate, rebootDate, alarmDate;

        String setDateString = prefs.getString
                (key + AlarmUtil.ALARM_TIME,
                        null);
        String rebootDateString = prefs.getString
                (key + AlarmUtil.REBOOT_ALARM_TIME,
                        null);
        if (setDateString == null || rebootDateString == null) {

            // First time setting alarm; no preferences found
            return updateAlarm(context, requestCode); // sets alarm, also updating shared prefs with new time
        }

        try {
            storedAlarmDate = df.parse(setDateString);
            rebootDate = df.parse(rebootDateString);
            alarmDate = storedAlarmDate;

            // AlarmDate can be directly loaded from storedDate for alarms on a constant interval.
            // But correct time for alarms that predict based on user data can change between
            // being set and being sent, and have to be rechecked.
            if (requestCode == RequestCodes.PERIOD_ALARM || requestCode == RequestCodes.PMS_ALARM) {
                Date nextAlarmDate = getAlarmDateNoOffset(context, requestCode); // returns a future date
                alarmDate = adjustAlarmOffset(context, nextAlarmDate, requestCode);
            }

            // Check if alarm date is same as stored date (it's still correct)
            if (alarmDate != null
                    && (DateUtil.clearTimeStamp(alarmDate).getTime()
                    == DateUtil.clearTimeStamp(storedAlarmDate).getTime())) {

                // Check if alarms have been sent already.
                boolean hasFired = prefs.getBoolean(key + HAS_FIRED, false);

                // Check if reset date has passed
                if (rebootDate != null
                        && rebootDate.getTime() >=
                        DateUtil.clearTimeStamp(new Date()).getTime()) {

                    // If notification has not already been sent, set alarm.
                    if (!hasFired) {
                        date = setAlarm(context, storedAlarmDate, rebootDate,
                                requestCode);

                    } else { // Alarm has fired already--rebroadcast again in a day.
                        Calendar newRebootDate = new GregorianCalendar();
                        newRebootDate.setTime(rebootDate);
                        newRebootDate.add(Calendar.DATE, 1);
                        setAlarmIntent(context, newRebootDate.getTime(), BROADCAST_REBOOT_ALARM, requestCode);
                    }
                } else {
                    date = updateAlarm(context, requestCode);
                }
            } else { // Difference between correct alarm date and stored date; update alarm
                date = updateAlarm(context, requestCode); // sets alarm and updates preferences with new time
            }
        } catch (ParseException ex) {
            Log.e(TAG, "RebootAlarms: failed to " +
                    "set alarm" + requestCode + ". Reason: " + ex.getMessage());
        }
        return date;
    }

    /*
     * Check if alarm is already scheduled.
     */
    public static boolean isScheduledAlarm(Context context, int code) {
        Intent alarmIntent = new Intent(context, CustomBroadcastReceiver.class);
        alarmIntent.putExtra(CODE_ID, code);
        alarmIntent.setAction(BROADCAST_START_ALARM);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, code, alarmIntent,
                        PendingIntent.FLAG_NO_CREATE);
        return (pendingIntent != null);
    }

    /*
    * Check if reboot is already scheduled.
    */
    public static boolean isScheduledReboot(Context context, int code) {
        Intent alarmIntent = new Intent(context, CustomBroadcastReceiver.class);
        alarmIntent.putExtra(CODE_ID, code);
        alarmIntent.setAction(BROADCAST_REBOOT_ALARM);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, code, alarmIntent,
                        PendingIntent.FLAG_NO_CREATE);
        return (pendingIntent != null);
    }


    private static Date setAlarmIntent(Context context, Date setDate, String action, int requestCode) {
        Intent intent = new Intent(context, CustomBroadcastReceiver.class);
        intent.putExtra(CODE_ID, requestCode);
        intent.setAction(action);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(setDate);

        int timeOfDay = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(ALARM_HOUR_KEY, DEFAULT_HOUR_ALARM);
        calendar.set(Calendar.HOUR_OF_DAY, timeOfDay);
        calendar.set(Calendar.MINUTE, 1);

        long timeInMillis = calendar.getTimeInMillis();

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, requestCode, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent);

        return calendar.getTime();
    }

    /*
     * Set alarm's shared preference hasFired to booleanHasFired
     * (true once push notification has been sent).  Is reset to false when new alarm is scheduled.
     * Applies to menstruation alarms, which could have variable notification schedule.
     */
    public static void setHasFired(Context context, int alarmCode, boolean hasFired) {
        String key = getKey(context, alarmCode);
        if (key != null) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(key + HAS_FIRED, hasFired)
                    .apply();
        }
    }

}
