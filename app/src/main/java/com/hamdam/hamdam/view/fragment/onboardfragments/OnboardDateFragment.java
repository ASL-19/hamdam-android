package com.hamdam.hamdam.view.fragment.onboardfragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ebraminio.droidpersiancalendar.calendar.DayOutOfRangeException;
import com.github.ebraminio.droidpersiancalendar.calendar.MonthOutOfRangeException;
import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.calendar.YearOutOfRangeException;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.util.NumberPickerWrapper;
import com.hamdam.hamdam.util.UtilWrapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnboardDateFragment extends OnboardFragment {
	private static final String TAG = "OnboardDateFragment";

    private NumberPickerWrapper yearPicker, monthPicker, dayPicker;
    private final int YEARS = 10;

    public OnboardDateFragment() {
        // Required empty public constructor
    }

    public static OnboardDateFragment newInstance(int position) {
        OnboardDateFragment fragment = new OnboardDateFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // inherits bundle checking/args from super
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_onboard_periodselector, container, false);

        // Find and set instructions
        super.setInstructions(view);

        yearPicker = (NumberPickerWrapper) view.findViewById(com.hamdam.hamdam.R.id.yearPicker);
        monthPicker = (NumberPickerWrapper) view.findViewById(com.hamdam.hamdam.R.id.monthPicker);
        dayPicker = (NumberPickerWrapper) view.findViewById(com.hamdam.hamdam.R.id.dayPicker);

        offset = UtilWrapper.fillDateWheels
                (yearPicker, monthPicker, dayPicker, YEARS);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    /*
     * Get data from spinners and save it as a SimpleDateFormat date. Note that
     * since this date is going directly into SharedPreferences, it is saved in Gregorian
     * time to accord with the database. (All persistent dates are stored in gregorian time only).
     * If invalid date is selected, return null.
     */
    @Nullable
    public String getValue() {
        int year = offset + (YEARS - yearPicker.getValue());
        int month = monthPicker.getValue() + 1;
        int day = dayPicker.getValue() + 1;

        try {
            PersianDate pDate = new PersianDate(year, month, day);
            Date gDate = DateUtil.persianToGregorianDate(pDate);

            // Date will be stored as a String in shared preferences and in the database.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return dateFormat.format(gDate);
        } catch (DayOutOfRangeException | MonthOutOfRangeException
                | YearOutOfRangeException e) { // User chose invalid date; exception handled in container class
            Log.e("OnboardDateFragment", "Invalid date was chosen");
            return null;
        }
    }
}
