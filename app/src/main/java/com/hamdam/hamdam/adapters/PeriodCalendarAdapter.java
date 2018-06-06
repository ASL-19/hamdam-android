package com.hamdam.hamdam.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.github.ebraminio.droidpersiancalendar.Constants;

import com.hamdam.hamdam.view.fragment.PeriodMonthFragment;

/**
 * Simple PagerAdapter subclass to adapt {PeriodMonthFragment} for Calendar view.
 */
public class PeriodCalendarAdapter extends FragmentStatePagerAdapter {

    public PeriodCalendarAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        PeriodMonthFragment fragment = new PeriodMonthFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.OFFSET_ARGUMENT, position - Constants.MONTHS_LIMIT / 2);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return Constants.MONTHS_LIMIT;
    }
}

