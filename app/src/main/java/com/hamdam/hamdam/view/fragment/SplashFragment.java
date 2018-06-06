package com.hamdam.hamdam.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hamdam.hamdam.R;

/**
 * Simple welcome fragment preceding onboarding screen.
 */
public class SplashFragment extends Fragment {

    public SplashFragment() {
    }

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.splash_title, container, false);
    }
}
