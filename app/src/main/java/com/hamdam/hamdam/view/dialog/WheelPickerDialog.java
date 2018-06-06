package com.hamdam.hamdam.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.hamdam.hamdam.util.NumberPickerWrapper;
import com.hamdam.hamdam.service.eventbus.WheelDialogEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * WheelPickerDialogFragment to display rotating wheel of numbers, formatted
 * as Persian digits.
 * <p/>
 * Fragment uses EventBus to post user's selection.
 */
public class WheelPickerDialog extends PreferenceDialogFragmentCompat {
    private String keyId;
    int min, max, defaultValue;


    public static WheelPickerDialog newInstance(int minPickerValue, int maxPickerValue,
                                                int defaultValue, String id) {
        WheelPickerDialog dialog = new WheelPickerDialog();
        dialog.min = minPickerValue;
        dialog.max = maxPickerValue;
        dialog.defaultValue = defaultValue;
        dialog.keyId = id;

        return dialog;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.dialog_wheel_select, null);

        final NumberPickerWrapper picker = (NumberPickerWrapper)
                view.findViewById(com.hamdam.hamdam.R.id.number_picker);

        picker.setMaxValue(this.max);

        picker.setMinValue(this.min);
        picker.setValue(this.defaultValue);

        picker.setWrapSelectorWheel(true);

        builder.setView(view);
        builder.setCustomTitle(null);
        builder.setPositiveButton(com.hamdam.hamdam.R.string.select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // post eventbus event
                EventBus.getDefault().post(new WheelDialogEvent(picker.getValue(), keyId));
                dismiss();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        return dialog;
    }

    @Override
    public void onDialogClosed(boolean b) {
    }
}
