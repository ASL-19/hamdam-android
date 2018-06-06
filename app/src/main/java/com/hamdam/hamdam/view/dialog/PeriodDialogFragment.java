package com.hamdam.hamdam.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.presenter.FertilityPresenterImpl;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.util.LocaleUtils;

import com.hamdam.hamdam.service.eventbus.UpdateViewEvent;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Dialog fragment allowing user to edit status of their period/menstrual cycle.
 */
public class PeriodDialogFragment extends AppCompatDialogFragment {

    private PersianDate persianDate;
    private PresenterContracts.FertilityPresenter mPresenter;
    private SwitchCompat toggleButton;
    private boolean isActivePeriod;
    private TextView mNumberView, dialogDate;
    private AppCompatImageView mPlus, mMinus;
    private int id, value = 0;
    private String dialogDateString;
    private static final String TITLE = "DialogTitle",
            IS_ACTIVE = "IsActive",
            VALUE = "VALUE";


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity != null) {
            mPresenter = FertilityPresenterImpl.getInstance(activity);
        }
    }

    @Override
    public void onDetach() {
        mPresenter = null;
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_ACTIVE, isActivePeriod);
        outState.putString(TITLE, dialogDateString);
        outState.putInt(VALUE, value);
        super.onSaveInstanceState(outState);
    }

    @Inject
    public static PeriodDialogFragment newInstance(PersianDate activeDate,
                                                   boolean isActivePeriod,
                                                   int id) {
        PeriodDialogFragment fragment = new PeriodDialogFragment();
        fragment.persianDate = activeDate;
        fragment.isActivePeriod = isActivePeriod;
        fragment.id = id;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                com.hamdam.hamdam.R.style.HamdamTheme_CustomDialogStyle);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.dialog_edit_period, null, false);

        mNumberView = (TextView) view.findViewById(com.hamdam.hamdam.R.id.period_length_number_text);
        dialogDate = (TextView) view.findViewById(com.hamdam.hamdam.R.id.dialog_date);

        if (savedInstanceState != null) {
            value = savedInstanceState.getInt(VALUE);
            isActivePeriod = savedInstanceState.getBoolean(IS_ACTIVE);
            dialogDateString = savedInstanceState.getString(TITLE);

        } else {
            // value
            //set default value for numbers
            SharedPreferences mPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity()
                            .getApplicationContext());
            String defaultDaysFromRes = Integer.toString
                    (getResources().getInteger
                            (com.hamdam.hamdam.R.integer.default_period_length));
            value = Integer.parseInt(mPrefs.getString
                    (getString(com.hamdam.hamdam.R.string.period_length_key),
                            defaultDaysFromRes));
            dialogDateString = buildDateString();
        }

        dialogDate.setText(dialogDateString);

        mPlus = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.plus);
        mMinus = (AppCompatImageView) view.findViewById(com.hamdam.hamdam.R.id.minus);

        mNumberView.setText(Utils.formatNumber(value));

        mPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePeriodState(true);
                value++;
                mNumberView.setText(Utils.formatNumber(value));
            }
        });

        mMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePeriodState(true);
                if (value > 1) {
                    value--;
                    mNumberView.setText(Utils.formatNumber(value));
                }
            }
        });

        toggleButton = (SwitchCompat) view.findViewById(com.hamdam.hamdam.R.id.dialog_toggle);
        toggleButton.setChecked(isActivePeriod);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                togglePeriodState(compoundButton.isChecked());
            }
        });

        if (isActivePeriod) {
            mPlus.setImageResource(com.hamdam.hamdam.R.drawable.ic_add_purple);
            mMinus.setImageResource(com.hamdam.hamdam.R.drawable.ic_remove_purple);
        } else {
            mPlus.setImageResource(com.hamdam.hamdam.R.drawable.ic_add_gray);
            mMinus.setImageResource(com.hamdam.hamdam.R.drawable.ic_remove_gray);
            mNumberView.setTextColor(ContextCompat.getColor(getActivity(), com.hamdam.hamdam.R.color.light_text_grey));
        }

        builder.setView(view)
                .setTitle(getString(com.hamdam.hamdam.R.string.edit_period_legend_text))
                .setCancelable(true)
                .setNegativeButton(com.hamdam.hamdam.R.string.dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(com.hamdam.hamdam.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                boolean onPeriod = toggleButton.isChecked();
                if (mPresenter != null) {
                    if (onPeriod) {
                        mPresenter.updatePeriodInfo(persianDate, value, id);
                    } else {
                        mPresenter.deletePeriodInfo(persianDate, id);
                    }
                }
                EventBus.getDefault().postSticky(new UpdateViewEvent(id));
                dialogInterface.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        return dialog;
    }

    public void togglePeriodState(boolean active) {
        if (active) {
            mPlus.setImageResource(com.hamdam.hamdam.R.drawable.ic_add_purple);
            mMinus.setImageResource(com.hamdam.hamdam.R.drawable.ic_remove_purple);
            mNumberView.setTextColor(ContextCompat.getColor(getActivity(),
                    com.hamdam.hamdam.R.color.dark_background_purple));
            mNumberView.setText(Utils.formatNumber(value));

        } else {
            mPlus.setImageResource(com.hamdam.hamdam.R.drawable.ic_add_gray);
            mMinus.setImageResource(com.hamdam.hamdam.R.drawable.ic_remove_gray);
            mNumberView.setTextColor(ContextCompat.getColor(getActivity(),
                    com.hamdam.hamdam.R.color.light_text_grey));
        }
        toggleButton.setChecked(active);
    }

    // Build date-relevant title.
    private String buildDateString() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) { // Rtl not supported
            return LocaleUtils.buildLtr(Utils.formatNumber(persianDate.getDayOfMonth()))
                    + " " + LocaleUtils.buildLtr(Utils.getMonthName(persianDate));
        } else {
            return Utils.getMonthName(persianDate) + " "
                    + Utils.formatNumber(persianDate.getDayOfMonth());
        }
    }

}
