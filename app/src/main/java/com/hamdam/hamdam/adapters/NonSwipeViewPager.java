package com.hamdam.hamdam.adapters;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Non-swipeable ViewPager designed for the onboarding process.
 * Thanks to stackoverflow users @louielouie and @peter for their
 * implementation.
 */
public class NonSwipeViewPager extends ViewPager {

    public NonSwipeViewPager(Context context) {
        super(context);
    }

    public NonSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        // Override swiping by keyboard arrows--for emulator, tv, or tablet
        return false;
    }
}
