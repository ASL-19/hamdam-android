package com.hamdam.hamdam.view.fragment;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.hamdam.hamdam.model.StaticFact;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.adapters.ContentAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.hamdam.hamdam.Constants.HEALTH;

/**
 * A simple {@link BaseContentFragment} subclass responsible
 * for displaying static content on health.
 */
public class HealthInfoFragment extends BaseContentFragment {

    public HealthInfoFragment() {
        // Required empty public constructor
    }

    public static HealthInfoFragment newInstance() {
        return new HealthInfoFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTracker != null) {
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mTracker != null) {
            mTracker.setScreenName(getClass().getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.health_status_bar));
        }
        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.health_info),
                ContextCompat.getColor
                        (getActivity(), com.hamdam.hamdam.R.color.health),
                Color.WHITE, false);

        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_fact, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(com.hamdam.hamdam.R.id.fact_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Populate data
        ArrayList<StaticFact> mData = getStaticContent();
        if (mData != null) {
            mRecyclerView.setAdapter(new ContentAdapter(this, mData));
        }
        return view;
    }


    @Override
    public ArrayList<StaticFact> getStaticContent() {
        if (mProvider != null) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                getActivity()
                                        .getAssets()
                                        .open(HEALTH), UTF8));
                return mProvider.loadStaticContent(reader, StaticFact.TOPIC_TYPES.HEALTH);
            } catch (IOException ex) {
                Log.e("GetStaticContent", "IOException while opening file");
            }
        }
        return null;
    }
}
