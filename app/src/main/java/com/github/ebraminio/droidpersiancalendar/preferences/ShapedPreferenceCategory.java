package com.github.ebraminio.droidpersiancalendar.preferences;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.github.ebraminio.droidpersiancalendar.utils.Utils;

/**
 * Category of settings menu layout designed to hold preference options
 * (list, checkbox, toggle etc).
 * Created by ebraminio on 2/16/16.
 */
public class ShapedPreferenceCategory extends PreferenceCategory {
    public ShapedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShapedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShapedPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapedPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Utils.getInstance(getContext()).setFont(holder);
    }
}
