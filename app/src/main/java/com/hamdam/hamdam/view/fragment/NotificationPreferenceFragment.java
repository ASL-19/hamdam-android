package com.hamdam.hamdam.view.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.preferences.ShapedListDialog;
import com.github.ebraminio.droidpersiancalendar.preferences.ShapedListPreference;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.util.DateUtil;

import com.hamdam.hamdam.R;
import com.hamdam.hamdam.service.CustomBroadcastReceiver;
import com.hamdam.hamdam.util.AlarmUtil;
import com.hamdam.hamdam.util.LocaleUtils;
import com.hamdam.hamdam.util.NumberPickerWrapper;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.Constants;

import java.util.ArrayList;
import java.util.Date;


/**
 * Preference Fragment to handle notification-related preferences
 * (enabling and disabling reminders).
 */
public class NotificationPreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        DialogInterface.OnClickListener {

    private static final String TAG = "NotificationPreference";

    private SharedPreferences prefs;
    NumberPickerWrapper picker; // in dialogs
    private String currentKey;

    public NotificationPreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UtilWrapper.setActionBar(getActivity(), getString(R.string.notifications), false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), R.color.primary_dark));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.notification_preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        for (final String key : prefs.getAll().keySet()) {
            if (findPreference(key) instanceof SwitchPreferenceCompat) {
                setSwitchPreferenceSummary(prefs, findPreference(key), key);
            }
        }

        // Set click listener for hour of day preference
        findPreference(AlarmUtil.ALARM_HOUR_KEY).setOnPreferenceClickListener
                (new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        launchHourWheelPicker(AlarmUtil.ALARM_HOUR_KEY);
                        return true;
                    }
                });
        int hour = prefs.getInt(AlarmUtil.ALARM_HOUR_KEY, AlarmUtil.DEFAULT_HOUR_ALARM);
        findPreference(AlarmUtil.ALARM_HOUR_KEY).setSummary(formatHour(hour));
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment = null;
        if (preference instanceof ShapedListPreference) {
            fragment = new ShapedListDialog();
        } else {
            super.onDisplayPreferenceDialog(preference);
        }

        if (fragment != null) {
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            fragment.setArguments(bundle);
            fragment.setTargetFragment(this, 0);
            fragment.show(getChildFragmentManager(), fragment.getClass().getName());
        }
    }

    /*
         * Check for preference change and either set, update or delete a scheduled alert/alarm
         * for user depending on their choice.
         */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        Context context = getActivity().getApplicationContext();
        if (context == null) {
            Log.e(TAG, "Tried to " +
                    "change shared preferences but context was null");
            return;
        }

        // Enable/disable all notifications
        if (key.equals(getString(R.string.notifications_key))) {
            checkGlobalNotifications(context, sharedPreferences, key);
            setSwitchPreferenceSummary(sharedPreferences, preference, key);

        // Set or cancel individual reminders
        } else if (key.equals(getString(R.string.period_reminder_key)) ||
                key.equals(getString(R.string.pms_reminder_key))) {
            if (prefs.getBoolean(key, false)) {
                showFertilityPreferenceDialog(key);
            } else {
                AlarmUtil.cancelAlarm(getContext(), getRequestCode(key));
                AlarmUtil.cancelReboot(getContext(), getRequestCode(key));
            }
            setSwitchPreferenceSummary(sharedPreferences, findPreference(key), key);
        } else if (key.equals(getString(R.string.birth_control_reminder_key)) ||
                key.equals(getString(R.string.breast_exam_reminder_key))) {
            if (prefs.getBoolean(key, false)) {
                showReminderPreferenceDialog(key);
            } else {
                AlarmUtil.cancelAlarm(getContext(), getRequestCode(key));
                AlarmUtil.cancelReboot(getContext(), getRequestCode(key));
            }
            setSwitchPreferenceSummary(sharedPreferences, findPreference(key), key);

            // Alarm times have been changed
        } else if (key.equals(getString(R.string.period_reminder_key) + AlarmUtil.STORED_VALUE_KEY) ||
                key.equals(getString(R.string.pms_reminder_key) + AlarmUtil.STORED_VALUE_KEY)) {

            // remove stored value tag and check alarm
            String alarmKey = key.replaceAll(AlarmUtil.STORED_VALUE_KEY, "");
            checkAlarms(context, alarmKey);

            // Set summary of containing switch
            setSwitchPreferenceSummary(prefs, findPreference(alarmKey), alarmKey);
        } else if (key.equals(getString(R.string.breast_exam_reminder_key)
                + AlarmUtil.STORED_VALUE_KEY) ||
                key.equals(getString(R.string.birth_control_reminder_key)
                        + AlarmUtil.STORED_VALUE_KEY)) {
            String alarmKey = key.replaceAll(AlarmUtil.STORED_VALUE_KEY, "");
            checkAlarms(context, alarmKey);
            setSwitchPreferenceSummary(prefs, findPreference(alarmKey), alarmKey);
        } else if (key.equals(AlarmUtil.ALARM_HOUR_KEY)) { // Alarms need to be set with new time.
            AlarmUtil.rebootAlarms(context);
            findPreference(key).setSummary(formatHour(
                    (prefs.getInt(key, AlarmUtil.DEFAULT_HOUR_ALARM))));
        }
    }


    /*
     * Launch alert dialog with wheel picker when user wants to set alarm at
     * custom date interval.
     */
    private void launchCustomWheelPicker(final String key) {
        currentKey = key;
        AlertDialog dialog = new AlertDialog.Builder(getContext(),
                R.style.HamdamTheme_CustomDialogStyle)
                .setTitle(getString(R.string.select_reminder_interval))
                .setPositiveButton(getString(R.string.select), this)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        // Set custom dialog layout with numberpicker
        FrameLayout layout = (FrameLayout) dialog.findViewById(android.R.id.custom);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_wheel_select, layout);
        picker = (NumberPickerWrapper) view.findViewById(R.id.number_picker);

        // Set formatted numbers in picker
        String[] options = UtilWrapper.getFormattedStringArray(1, Constants.REMINDER_DAY_LIMIT); // 60

        // Stored value's index is equal to its value - 1
        int which = Integer.parseInt(prefs.getString(key + AlarmUtil.STORED_VALUE_KEY, "1")) - 1;
        UtilWrapper.setPickerArray(picker, options, which);
        dialog.setView(view);

        dialog.show();
    }


    /*
     * Launch alert dialog with wheel picker when user wants to set alarm at
     * custom date interval.
     */
    private void launchHourWheelPicker(final String key) {
        currentKey = key;
        AlertDialog dialog = new AlertDialog.Builder(getContext(),
                R.style.HamdamTheme_CustomDialogStyle)
                .setTitle(getString(R.string.select_time))
                .setPositiveButton(getString(R.string.select), this)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        // Set custom dialog layout with numberpicker
        FrameLayout layout = (FrameLayout) dialog.findViewById(android.R.id.custom);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_wheel_select, layout);
        picker = (NumberPickerWrapper) view.findViewById(R.id.number_picker);

        // Set formatted numbers in picker
        String[] options = getFormattedHourArray();

        // Stored option's index in array is equal to its value - 1
        int which = prefs.getInt(AlarmUtil.ALARM_HOUR_KEY, AlarmUtil.DEFAULT_HOUR_ALARM) - 1;
        UtilWrapper.setPickerArray(picker, options, which);
        dialog.setView(view);

        dialog.show();
    }

    /*
     * Return a string representing a 12-hour time,
     * suffixed with 'am' or 'pm' as appropriate.
     */
    private String formatHour(int hourOfDay) {
        if (0 < hourOfDay && 24 > hourOfDay) {

            // Old phones with no RTL strings
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return (hourOfDay < 13 ?
                        LocaleUtils.buildLtr
                                (com.github.ebraminio.droidpersiancalendar.Constants.AM_IN_PERSIAN)
                                + " "
                                + LocaleUtils.buildLtr(Utils.formatNumber(hourOfDay))
                        : LocaleUtils.buildLtr
                        (com.github.ebraminio.droidpersiancalendar.Constants.PM_IN_PERSIAN)
                        + " "
                        + LocaleUtils.buildLtr(Utils.formatNumber(hourOfDay - 12)));
            } else {
                return (hourOfDay < 13 ?
                        Utils.formatNumber(hourOfDay)
                                + " "
                                + com.github.ebraminio.droidpersiancalendar.Constants.AM_IN_PERSIAN
                        : Utils.formatNumber(hourOfDay - 12)
                        + " "
                        + com.github.ebraminio.droidpersiancalendar.Constants.PM_IN_PERSIAN);
            }
        }
        return null;
    }

    /*
     * Return hours of day with 'am' and 'pm' appended.
     */
    private String[] getFormattedHourArray() {
        ArrayList<String> numbers = new ArrayList<>();
        for (int i = 1; i < 24; i++) {
            numbers.add(formatHour(i));
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    private void showFertilityPreferenceDialog(final String key) {
        final CharSequence[] reminderOptions = getResources().getStringArray(R.array.reminderDelayEntries);
        int which = Integer.parseInt(prefs.getString(key + AlarmUtil.STORED_VALUE_KEY, "-1"));
        new AlertDialog.Builder(getContext(), R.style.HamdamTheme_CustomDialogStyle)
                .setTitle(getString(R.string.select_reminder_interval))
                .setSingleChoiceItems(reminderOptions, which, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int selected) {
                        prefs.edit()
                                .putString(key + AlarmUtil.STORED_VALUE_KEY, Integer.toString(selected))
                                .apply();
                        dialogInterface.dismiss();
                    }
                })
                .setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(true)
                .create()
                .show();
    }

    /*
     * Set checked option on reminder interval list based on user's preferences.
     * This options list correlates to an array of single-choice entries:
     * Off (0), Daily (1), Weekly (7), Monthly (30), and Custom (all others).
     */
    private int setWhichOption(final int storedValue) {
        int which = -1;
        switch (storedValue) {
            case -1:
            case 0:
                which = -1;
                break;
            case 1:
                which = 0;
                break;
            case 7:
                which = 1;
                break;
            case 30: // R.integer.monthly
                which = 2;
                break;
            default:
                if (storedValue > 0) {
                    which = 3;
                }
                break;
        }
        return which;
    }

    /*
     * Show dialog offering the user choice of when to set a reminder.
     */
    private void showReminderPreferenceDialog(final String key) {
        final CharSequence[] reminderOptions = getResources().getStringArray(R.array.reminderIntervalEntries);
        int storedValue = Integer.parseInt(prefs.getString(key + AlarmUtil.STORED_VALUE_KEY, "-1"));
        int which = setWhichOption(storedValue);
        new AlertDialog.Builder(getContext(), R.style.HamdamTheme_CustomDialogStyle)
                .setTitle(getString(R.string.select_reminder_interval))
                .setSingleChoiceItems(reminderOptions, which, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int selected) {
                        if (selected == reminderOptions.length - 1) { // the last option, "Custom"
                            launchCustomWheelPicker(key);
                            dialogInterface.dismiss();
                        } else {
                            CharSequence[] reminderValues = getResources()
                                    .getStringArray(R.array.reminderIntervalValues);
                            String value = reminderValues[selected].toString();
                            prefs.edit()
                                    .putString(key + AlarmUtil.STORED_VALUE_KEY, value)
                                    .apply();
                            AlarmUtil.updateAlarm(getContext(), getRequestCode(key));
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(true)
                .create()
                .show();
    }

    /*
     * Set summary text for switch preferences (boolean values).
     */
    private void setSwitchPreferenceSummary(SharedPreferences prefs, Preference preference,
                                            String key) {
        // Switch is set to off
        if (!prefs.getBoolean(key, false)) {
            preference.setSummary(getString(R.string.off));
        } else {
            if (key.equals(getString(R.string.birth_control_reminder_key)) ||
                    key.equals(getString(R.string.breast_exam_reminder_key))) {
                int which = Integer.parseInt(prefs.getString(key + AlarmUtil.STORED_VALUE_KEY, "-1"));
                if (which > 0) {
                    String summary;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        summary = LocaleUtils.buildLtr(Utils.formatNumber(which)) + " "
                                + LocaleUtils.buildLtr(getString(R.string.remind_interval_summary));
                    } else {
                        summary = Utils.formatNumber(which) + " " +
                                getString(R.string.remind_interval_summary);
                    }
                    preference.setSummary(summary);
                }
            } else if (key.equals(getString(R.string.period_reminder_key)) ||
                    key.equals(getString(R.string.pms_reminder_key))) {
                String[] choices = getResources().getStringArray(R.array.reminderDelayEntries);
                int which = Integer.parseInt(prefs.getString(key + AlarmUtil.STORED_VALUE_KEY, "-1"));
                if (which >= 0 && which < choices.length) {
                    preference.setSummary(choices[which]);
                }
            } else {
                preference.setSummary(R.string.on);
            }
        }
    }

    /*
     * Check whether notifications have been enabled/disabled app-wide.
     * Called on notifications key preference change.
     * Overwrites settings in AndroidManifest.xml to reflect whether receiver
     * should listen for device boot settings to re-create alarms on phone boot.
     * Preference stores boolean data.
     * @param   context   current Application context
     * @param   sharedPreferences   preferences file
     * @param   key     String key representing current changed preference
     */
    private void checkGlobalNotifications(Context context,
                                          SharedPreferences sharedPreferences, String key) {
        ComponentName receiver = new ComponentName(context, CustomBroadcastReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        int[] alarmCodes = new int[]
                {AlarmUtil.RequestCodes.BREAST_EXAM_ALARM,
                        AlarmUtil.RequestCodes.PERIOD_ALARM,
                        AlarmUtil.RequestCodes.PMS_ALARM,
                        AlarmUtil.RequestCodes.MEDICATION_ALARM};

        // Enable Notifications set to true; enable receiver
        if (sharedPreferences.getBoolean(key, false)) {
            packageManager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            for (int code : alarmCodes) {
                String alarmKey = AlarmUtil.getKey(context, code);
                if (PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(alarmKey, false)) {
                    AlarmUtil.updateAlarm(context, code);
                }
            }

        } else { // Notifications are disabled; disable receiver
            packageManager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            // Cancel currently-scheduled alarms
            for (int code : alarmCodes) {
                AlarmUtil.cancelAlarm(context, code);
                AlarmUtil.cancelReboot(context, code);
            }
        }
    }


    /*
     * Update alarms and display toast message if alarm successfully set.
     * This method is called on preferences of type ListPreference.
     *
     * @param   context     current context
     * @param   key         String key representing current preference (String)
     *
     */
    @SuppressWarnings("null") // DateUtil won't return null
    private void checkAlarms(Context context,
                             final String key) {
        int code = getRequestCode(key);
        if (context != null) {
            Date alarmDate = AlarmUtil.updateAlarm(context, code);
            if (alarmDate != null) {
                PersianDate persianDate = DateUtil.gregorianDateToPersian(alarmDate);
                String message = Utils.dateToString(persianDate);

                // Confirm alarm to user via toast. For now, message only contains
                // future notification set date due to Toast/system language issue that
                // does not occur in other app toasts. However, might be preferable to display a complete
                // sentence ("Your next reminder is on <Date>"). @Todo
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } else {
                AlarmUtil.cancelAlarm(context, code);
            }
        } else {
            Log.e(TAG, "checkAlarms: tried to set alarm" +
                    "but context was null");
        }
    }


    // set custom request code for different notification type, or 0 as default code.

    private int getRequestCode(final String key) {
        if (key.equals(getString(R.string.birth_control_reminder_key))) {
            return AlarmUtil.RequestCodes.MEDICATION_ALARM;
        } else if (key.equals(getString(R.string.breast_exam_reminder_key))) {
            return AlarmUtil.RequestCodes.BREAST_EXAM_ALARM;
        } else if (key.equals(getString(R.string.pms_reminder_key))) {
            return AlarmUtil.RequestCodes.PMS_ALARM;
        } else if (key.equals(getString(R.string.period_reminder_key))) {
            return AlarmUtil.RequestCodes.PERIOD_ALARM;
        } else {
            return 0;
        }
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (currentKey.equals(AlarmUtil.ALARM_HOUR_KEY)) {
            prefs.edit()
                    .putInt(AlarmUtil.ALARM_HOUR_KEY,
                            (picker.getValue() + 1))
                    .apply();
        } else {
            prefs.edit()
                    .putString(currentKey + AlarmUtil.STORED_VALUE_KEY,
                            Integer.toString(picker.getValue() + 1))
                    .apply();
        }
        dialogInterface.dismiss();
    }
}

