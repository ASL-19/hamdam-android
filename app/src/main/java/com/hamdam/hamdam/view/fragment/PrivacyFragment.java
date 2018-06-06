package com.hamdam.hamdam.view.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hamdam.hamdam.util.UtilWrapper;

/**
 * Hamdam privacy policy.
 */

public class PrivacyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_privacy, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }

        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.privacy_policy), false);

        // Load content into textview.
        TextView titleView = (TextView) view.findViewById(com.hamdam.hamdam.R.id.title);
        TextView subTitleView = (TextView) view.findViewById(com.hamdam.hamdam.R.id.subtitle);
        TextView googleAnalyticsText = (TextView) view.findViewById(com.hamdam.hamdam.R.id.body2);
        titleView.setText(getString(com.hamdam.hamdam.R.string.privacy_policy));
        subTitleView.setText(getString(com.hamdam.hamdam.R.string.subheading_google_analytics));
        TextView contentView = (TextView) view.findViewById(com.hamdam.hamdam.R.id.body);
        contentView.setText(getString(com.hamdam.hamdam.R.string.privacy_policy_body));
        googleAnalyticsText.setText(getString(com.hamdam.hamdam.R.string.google_analytics_body));

        return view;
    }
}
