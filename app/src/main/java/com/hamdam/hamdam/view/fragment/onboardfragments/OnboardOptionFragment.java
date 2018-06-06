package com.hamdam.hamdam.view.fragment.onboardfragments;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hamdam.hamdam.R;
import com.hamdam.hamdam.util.NumberPickerWrapper;
import com.hamdam.hamdam.util.UtilWrapper;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnboardOptionFragment extends OnboardFragment {
	private static final String TAG = "OnboardOptionFragment";

    private NumberPickerWrapper mPicker;

    public OnboardOptionFragment() {
        // Required empty public constructor
    }

    public static OnboardOptionFragment newInstance(int position) {
        OnboardOptionFragment fragment = new OnboardOptionFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        fragment.setArguments(b);
        return fragment;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboard_numberselector, container, false);
        mPicker = (NumberPickerWrapper) view.findViewById(R.id.number_picker);
        // Find and set layout elements
        super.setInstructions(view);

        offset = setSpinner();

        return view;
    }

    /*
     * Sets pickers for choosing number of days in certain part of fertility cycle,
     * such as period length or total cycle length.
     */
    public int setSpinner() {
        int min = 0, range, defaultStart = 0;
        String[] options;
        Resources resources = mContext.getResources();
        switch (this.mPositionId) {
            case 0:
                options = resources.getStringArray(R.array.birthControlEntries);
                defaultStart = options.length - 1;
                break;
            case 1:
                min = resources.getInteger(R.integer.default_min_cycle_length);
                range = resources.getInteger(R.integer.max_cycle_length);
                defaultStart = resources.getInteger(R.integer.default_cycle_length);
                options = UtilWrapper.getFormattedStringArray(min, range);
                break;
            case 2:
                range = 20;
                defaultStart = resources.getInteger(R.integer.default_period_length);
                options = UtilWrapper.getFormattedStringArray(min, range);
                break;
            case 3:
                Log.e("Spinner setup", "NumberSpinner reached but DateSpinner expected");
                return -1;
            default:
                Log.e("Spinner setup", "Default case reached with improper setup of "
                        + Integer.toString(mPositionId));
                return -1;
        }

        UtilWrapper.setPickerArray(mPicker, options, defaultStart - min); // the index of the default value

        return min;
    }

    public int getValue() {
        int index = mPicker.getValue();
        return index + offset;
    }

}
