package com.hamdam.hamdam.view.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.hamdam.hamdam.model.StaticFact;
import com.hamdam.hamdam.presenter.StaticContentProviderImpl;
import com.hamdam.hamdam.util.UtilWrapper;
import com.hamdam.hamdam.adapters.ContentAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.hamdam.hamdam.Constants.ABOUT;

/**
 * Static fragment displaying information about this application.
 */
public class AboutHamdamFragment extends BaseContentFragment {

    public AboutHamdamFragment() {
        // Empty constructor required
    }

    public static AboutHamdamFragment newInstance() {
        return new AboutHamdamFragment();
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
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_about, container, false);
        mProvider = StaticContentProviderImpl.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }
        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.about), false);

        TextView titleFaq = (TextView) view.findViewById(com.hamdam.hamdam.R.id.title);
        TextView description = (TextView) view.findViewById(com.hamdam.hamdam.R.id.body);

        mRecyclerView = (RecyclerView) view.findViewById(com.hamdam.hamdam.R.id.about_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Populate data
        ArrayList<StaticFact> mData = getStaticContent();
        if (mData != null) {

            // Pull first element out for about page
            StaticFact aboutFact = mData.get(0);
            titleFaq.setText(aboutFact.hasHeading()
                    ? aboutFact.getHeading()
                    : aboutFact.getSubheading());
            description.setText(aboutFact.getBody());
            mData.remove(0);
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
                                        .open(ABOUT), "utf8"));
                return  mProvider.loadStaticContent(reader, StaticFact.TOPIC_TYPES.ABOUT);
            } catch (IOException ex) {
                Log.e("GetStaticContent", "IOException while opening file");
            }
        }
        return null;
    }

}
