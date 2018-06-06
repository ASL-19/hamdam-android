package com.hamdam.hamdam.view.fragment.onboardfragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.presenter.PresenterContracts;


/**
 * Abstract Fragment class containing elements common to all Onboarding fragments, including
 * an OnboardView interface implemented by the parent activity.
 */
public abstract class OnboardFragment extends Fragment {
    private static final String TAG = "OnboardFragment";

    protected int mPositionId;
    protected int offset = 0;
    protected Context mContext;
    protected static final String POSITION = "POSITION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle b = getArguments();
            mPositionId = b.getInt(POSITION);
        } else {
            mPositionId = savedInstanceState.getInt(POSITION);
        }
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

    public void setInstructions(View view) {
        // Find and set layout elements
        TextView instructionTextView = (TextView) view.findViewById(com.hamdam.hamdam.R.id.instructions);
        Utils.getInstance(mContext).setTypeface(instructionTextView); // Set to Persian font

        // Onboard Activity always implements instruction-setting method
        ((PresenterContracts.OnboardView) mContext).setInstructions(instructionTextView, mPositionId);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, mPositionId);
        super.onSaveInstanceState(outState);
    }

}
