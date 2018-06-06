package com.hamdam.hamdam.view.fragment;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.hamdam.hamdam.R;
import com.hamdam.hamdam.adapters.ContentAdapter;
import com.hamdam.hamdam.model.StaticFact;
import com.hamdam.hamdam.util.UtilWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.hamdam.hamdam.Constants.DOMESTIC;

/**
 * A simple {@link BaseContentFragment} subclass responsible
 * for displaying static content on health.
 */
public class DomesticRightsInfoFragment extends BaseContentFragment {

    public DomesticRightsInfoFragment() {
        // Required empty public constructor
    }

    public static DomesticRightsInfoFragment newInstance() {
        return new DomesticRightsInfoFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), R.color.marriage_rights_status_bar));
        }

        View view = inflater.inflate(R.layout.fragment_fact, container, false);

        UtilWrapper.setActionBar(getActivity(), getString(R.string.domestic_rights),
                ContextCompat.getColor
                        (getActivity(), R.color.marriage_rights),
                Color.WHITE, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fact_recycler_view);
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
                                        .open(DOMESTIC), UTF8));
                return mProvider.loadStaticContent(reader, StaticFact.TOPIC_TYPES.MARRIAGE_RIGHTS);
            } catch (IOException ex) {
                Log.e("GetStaticContent", "IOException while opening file");
            }
        }
        return null;
    }

}
