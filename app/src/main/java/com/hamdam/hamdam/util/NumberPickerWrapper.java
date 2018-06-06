package com.hamdam.hamdam.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;

/**
 * NumberPicker subclass to adjust small font size in NumberPicker.
 * Particular issue because Farsi font is smaller than equivalent English
 * font and affects readability. Not ideal to require this workaround, but
 * seems necessary.
 *
 * Thanks to stackoverflow user aheuermann for this solution.
 */
public class NumberPickerWrapper extends NumberPicker {
    public NumberPickerWrapper(Context context) {
        super(context);
    }

    public NumberPickerWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberPickerWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NumberPickerWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setTransparentDivider();
    }

    // Change the default colour bar in NumberPicker to invisible.
    // Android does not expose this functionality and we are forced to use (evil) reflection.
    // Thanks to Stackoverflow user Bojan Kseneman for his suggestion.
    private void setTransparentDivider() {
        try {
            Field fDividerDrawable = NumberPicker.class.getDeclaredField("mSelectionDivider");
            fDividerDrawable.setAccessible(true);
            Drawable d = (Drawable) fDividerDrawable.get(this);
            d.setColorFilter(ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.white),
                    PorterDuff.Mode.CLEAR);
            d.invalidateSelf();
            postInvalidate(); // Drawable is 'dirty'
        }
        catch (Exception e) {
            // Leave default color
        }
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View view) {
        if(view instanceof EditText){
            ((EditText) view).setTextSize(26);
        }
    }


}
