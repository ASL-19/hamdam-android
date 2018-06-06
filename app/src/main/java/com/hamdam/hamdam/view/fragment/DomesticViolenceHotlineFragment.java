package com.hamdam.hamdam.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.view.activity.BaseActivity;

import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_CLICK;

/**
 * Fragment to display marriage rights/domestic violence static content.
 */
public class DomesticViolenceHotlineFragment extends Fragment {
    private Tracker mTracker;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTracker != null) {
            mTracker.setScreenName(getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Analytics
        BaseActivity parent = (BaseActivity) getActivity();
        if (parent != null) {
            mTracker = parent.getTracker();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_123, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }

        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.app_name), false);
        Button dialButton = (Button) view.findViewById(com.hamdam.hamdam.R.id.dial);
        dialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTracker != null) {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setLabel("DomesticRightsHotline")
                            .setAction(ANALYTICS_ACTION_CLICK)
                            .build());
                }
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setData(Uri.parse("tel:" + "+123"));
                getActivity().startActivity(callIntent);
            }
        });
        return view;
    }
}
