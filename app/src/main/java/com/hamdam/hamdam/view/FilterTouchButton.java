package com.hamdam.hamdam.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Custom base button class.
 */

public class FilterTouchButton extends Button {

    public FilterTouchButton(Context context) {
        super(context);
        setFilterTouchesWhenObscured(true);
    }

    public FilterTouchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFilterTouchesWhenObscured(true);
    }

    public FilterTouchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFilterTouchesWhenObscured(true);
    }

    @TargetApi(21)
    public FilterTouchButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFilterTouchesWhenObscured(true);
    }

}
