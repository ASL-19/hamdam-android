package com.hamdam.hamdam.view.fragment;

/**
 * Base Fragment for all types of static content
 */

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hamdam.hamdam.presenter.PresenterContracts;
import com.hamdam.hamdam.presenter.StaticContentProviderImpl;
import com.hamdam.hamdam.view.activity.BaseActivity;

import static com.hamdam.hamdam.Constants.ANALYTICS_ACTION_VIEW;
import static com.hamdam.hamdam.Constants.ANALYTICS_CATEGORY_CONTENT;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseContentFragment extends Fragment implements
        PresenterContracts.StaticContentPresenter {
    protected TextView mTitleView;
    protected RecyclerView mRecyclerView;
    protected PresenterContracts.StaticContentProvider mProvider;
    protected static final String UTF8 = "utf8";
    protected Tracker mTracker;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mProvider = StaticContentProviderImpl.getInstance();
    }

    @Override
    public void onDetach() {
        mProvider = null;
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BaseActivity parent = ((BaseActivity) getActivity());
        if (parent != null) {
            mTracker = parent.getTracker();
            mTracker.setScreenName(getClass().getSimpleName());
        }
    }

    @Override
    public void showContentFragment(Fragment target, String tag) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(ANALYTICS_CATEGORY_CONTENT)
                .setAction(ANALYTICS_ACTION_VIEW)
                .setLabel(tag)
                .build());

        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(com.hamdam.hamdam.R.id.fragment_holder, target, tag)
                .addToBackStack(null)
                .commit();
    }

}
