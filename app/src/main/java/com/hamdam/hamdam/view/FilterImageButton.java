package com.hamdam.hamdam.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

/**
 * Set filterTouchesWhenObscured for image buttons.
 */

public class FilterImageButton extends AppCompatImageButton {
    public FilterImageButton(Context context) {
        super(context);
        setFilterTouchesWhenObscured(true);
    }

    public FilterImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFilterTouchesWhenObscured(true);
    }

    public FilterImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFilterTouchesWhenObscured(true);
    }
}
