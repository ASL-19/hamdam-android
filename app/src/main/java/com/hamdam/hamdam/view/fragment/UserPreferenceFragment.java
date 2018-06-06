package com.hamdam.hamdam.view.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.ebraminio.droidpersiancalendar.preferences.ShapedListPreference;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.Constants;

import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.service.eventbus.WheelDialogEvent;
import com.hamdam.hamdam.view.dialog.WheelPickerDialog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * A simple {@link Fragment} subclass representing User preferences.
 */
public class UserPreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    private static final String TAG = "UserPreference";
    private SharedPreferences prefs;

    public UserPreferenceFragment() {
        // Required empty public constructor
    }

    // Note: although best practices are to register and de-register listeners in onResume/onPause,
    // if another activity involving preferences is launched, the listener will be deregistered and
    // the changes will not be introduced. So if a new preference activity is added,
    // listeners will have to be registered/de-reigstered in onStart and onStop.
    @Override
    public void onStart() {
        super.onStart();
        prefs.registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.user_preferences), false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(com.hamdam.hamdam.R.xml.user_preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        for (String key : prefs.getAll().keySet()) {
            setSummary(findPreference(key), key);
        }

        Preference clearDataPreference = findPreference(getString(com.hamdam.hamdam.R.string.clear_data_key));
        clearDataPreference.setOnPreferenceClickListener(this);

        Preference showTooltipsPreference = findPreference
                (getString(com.hamdam.hamdam.R.string.tooltips_complete_key));
        showTooltipsPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment = null;
        if (preference instanceof ShapedListPreference) {
            setArray((ShapedListPreference) preference);
            fragment = CustomListPreferenceDialog.newInstance(prefs, preference);
        } else if (preference instanceof ListPreference) {
            fragment = buildPreferenceDialog(preference);
        } else {
            super.onDisplayPreferenceDialog(preference); //@Todo
        }

        if (fragment != null) {
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            fragment.setArguments(bundle);
            fragment.setTargetFragment(this, 0);
            fragment.show(getChildFragmentManager(), fragment.getClass().getName());
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey()
                .equals(getString(com.hamdam.hamdam.R.string.clear_data_key))) {

            // Show warning dialog
            new AlertDialog.Builder(getContext(), com.hamdam.hamdam.R.style.HamdamTheme_CustomDialogStyle)
                    .setTitle(getString(com.hamdam.hamdam.R.string.clear_data))
                    .setMessage(com.hamdam.hamdam.R.string.clear_data_warning)
                    .setNegativeButton(getString(com.hamdam.hamdam.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton(getString(com.hamdam.hamdam.R.string.clear_data), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PresenterContracts.DatabasePresenter mPresenter = DatabaseHelperImpl.getInstance(getContext());
                            mPresenter.clearUserHistory();
                            prefs.edit()
                                    .putString(getString(com.hamdam.hamdam.R.string.last_period_date_key), "")
                                    .apply();
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(true)
                    .create()
                    .show();
            return true;
        } else if (preference.getKey()
                .equals(getString(com.hamdam.hamdam.R.string.tooltips_complete_key))) {
            prefs.edit()
                    .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_calendar_key), false)
                    .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_homepage_key), false)
                    .putBoolean(getString(com.hamdam.hamdam.R.string.tooltips_complete_question_key), false)
                    .apply();
            getActivity().finish();
            startActivity(getActivity().getIntent());
            return true;
        }
        return false;

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference preference = findPreference(key);
        if (key.equals(getString(com.hamdam.hamdam.R.string.enable_prediction_mode_key)) &&
                !prefs.getBoolean(key, false)) {
            Toast.makeText(getContext(),
                    getString(com.hamdam.hamdam.R.string.prediction_mode_explanation),
                    Toast.LENGTH_LONG).show();
        } else {
            setSummary(preference, key);
        }
    }

    /*
     * Set the summary view of a preference item so that user can see their current selection.
     */
    private void setSummary(Preference preference, String key) {
        if (preference != null &&
                !key.equals(getString(com.hamdam.hamdam.R.string.clear_data_key))
                && !key.equals(getString(com.hamdam.hamdam.R.string.enable_prediction_mode_key))) {
            if (preference instanceof ShapedListPreference) { // the birth control entries.
                int which = ((ListPreference) preference).findIndexOfValue((prefs.getString(key, null)));
                String[] formattedSummaries = getResources().getStringArray(com.hamdam.hamdam.R.array.birthControlEntries);
                if (which > 0 && which < formattedSummaries.length) {
                    preference.setSummary(formattedSummaries[which]);
                }
            } else if (preference instanceof ListPreference) { // It's a String number, needs to be a String persian number
                preference.setSummary(Utils.formatNumber(prefs.getString(key, null)));
            }
        }
    }

    /*
     * Retrieve selected item from wheel spinners.
     */
    @Subscribe
    public void onWheelDialogEvent(WheelDialogEvent event) {
        prefs.edit().putString(event.getKey(), Integer.toString(event.getValue())).apply();
    }

    public DialogFragment buildPreferenceDialog(Preference preference) {
        String key = preference.getKey();

        // Return a WheelPicker dialog supplied with the following values:
        // min value of wheel, max value of wheel, and default (visible) selection.
        // Default selection is taken from past user preferences, if available.
        if (key.equals(getString(com.hamdam.hamdam.R.string.cycle_length_key))) {
            return WheelPickerDialog.newInstance(Constants.DEFAULT_PERIOD_LENGTH,
                    Constants.DEFAULT_LONG_CYCLE_LENGTH,
                    getDefaultValue(key, Constants.DEFAULT_CYCLE_LENGTH), key);
        } else if (key.equals(getString(com.hamdam.hamdam.R.string.period_length_key))) {
            return WheelPickerDialog.newInstance(1, Constants.DEFAULT_CYCLE_LENGTH,
                    getDefaultValue(key, Constants.DEFAULT_PERIOD_LENGTH), key);
        } else if (key.equals(getString(com.hamdam.hamdam.R.string.pms_length_key))) {
            return WheelPickerDialog.newInstance(1, Constants.DEFAULT_PERIOD_LENGTH,
                    getDefaultValue(key, Constants.DEFAULT_PMS_LENGTH), key);
        }
        Log.e("UserPreference", "buildPreferenceDialog returned null");
        return null;
    }

    /*
     * Return either the stored preference as an integer, or a supplied default if
     * no stored preference can be found.
     */
    private int getDefaultValue(String key, int defaultValue) {
        return Integer.parseInt(prefs.getString(key, Integer.toString(defaultValue)));
    }

    public void setArray(ShapedListPreference preference) {
        String key = preference.getKey();

        if (key.equals(getString(com.hamdam.hamdam.R.string.birth_control_key))) {
            preference.setEntries(com.hamdam.hamdam.R.array.birthControlEntries);
            preference.setEntryValues(com.hamdam.hamdam.R.array.birthControlValues);
        } else {
            Log.e("User Preference", "ShapedListDialog created for unexpected preference");
        }
    }
}

