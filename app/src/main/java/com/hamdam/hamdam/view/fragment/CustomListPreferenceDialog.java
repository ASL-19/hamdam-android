package com.hamdam.hamdam.view.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.widget.ListView;


/**
 * ListPreference with custom theme.
 */
public class CustomListPreferenceDialog extends DialogFragment {
	private static final String TAG = "CustomListPrefDialog";

    private SharedPreferences mPrefs;
    private Preference mPreference;

    public static CustomListPreferenceDialog newInstance(SharedPreferences preferences, Preference preference) {
        CustomListPreferenceDialog mDialog = new CustomListPreferenceDialog();
        mDialog.mPreference = preference;
        mDialog.mPrefs = preferences;

        return mDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return buildDialog();
    }

    public Dialog buildDialog() {

        int index = ((ListPreference) mPreference)
                .findIndexOfValue(((ListPreference) mPreference).getValue());

        AlertDialog.Builder mBuilder = new AlertDialog.Builder
                (getContext(), com.hamdam.hamdam.R.style.HamdamTheme_CustomDialogStyle);

        return mBuilder.setSingleChoiceItems(((ListPreference) mPreference).getEntries(), index,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        ListView listView = ((AlertDialog) dialogInterface).getListView();
                        int selected = listView.getCheckedItemPosition();
                        mPreference.setSummary(((ListPreference) mPreference)
                                .getEntries()[selected].toString());
                        mPrefs.edit()
                                .putString(mPreference.getKey(),
                                        ((ListPreference) mPreference)
                                                .getEntryValues()[selected]
                                                .toString())
                        .apply();

                    }
                })
                .setPositiveButton(com.hamdam.hamdam.R.string.select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(com.hamdam.hamdam.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

}
