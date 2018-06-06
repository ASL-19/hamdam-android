package com.hamdam.hamdam.util;

import android.view.View;

/**
 * Simple animations utility class.
 */
public class AnimateUtils {
    private static final int DURATION = 100;
    private static final int MED_DURATION = 350;
    private static final float SCALE_UP_FACTOR = 1.25f;
    private static final float NO_SCALE = 1.0f;
    private static final float ALPHA_FADE = 0.5f;

    /*
     * Enlarge View proportionally over DURATION milliseconds and
     * remove fade (opacity 100%).
     * @param   view    target
     * @param   duration    duration of animation (milliseconds)
     */
    public static void scaleUp(View view, int duration) {
        view.animate()
                .alpha(1.0f)
                .scaleX(SCALE_UP_FACTOR)
                .scaleY(SCALE_UP_FACTOR)
                .setDuration(duration)
                .start();
    }

    /*
     * Shrink View proportionally over DURATION milliseconds
     * and add fade (opacity ALPHA_FADE * 100 %).
     * @param   view    target
     * @param   duration    duration of animation (milliseconds)
     */
    public static void scaleDown(View view, int duration) {
        view.animate()
                .alpha(ALPHA_FADE)
                .scaleX(NO_SCALE)
                .scaleY(NO_SCALE)
                .setDuration(duration)
                .start();
    }

    /*
     * Use default duration DURATION if no duration specified
     */
    public static void scaleDown(View view) {
        scaleDown(view, DURATION);
    }

    public static void scaleUp(View view) {
        scaleUp(view, DURATION);
    }

    public static void fadeIn(View view, float alpha) {
        fadeIn(view, DURATION, alpha);
    }

    public static void fadeIn(View view, int duration, float alpha) {
        view.setAlpha(0.f);
        view.animate()
                .alpha(alpha)
                .setDuration(duration)
                .setStartDelay(DURATION)
                .start();
    }

    public static void fadeIn(View view) {
        fadeIn(view, DURATION, 1.0f);
    }


    public static void medFadeIn(View view) {
        fadeIn(view, MED_DURATION, 1.0f);
    }

    public static void medFadeOut(View view) {
        view.setAlpha(1.0f);
        view.animate()
                .alpha(0f)
                .setDuration(MED_DURATION)
                .setStartDelay(DURATION)
                .start();
    }
    public static void fadeOut(View view) {
        view.setAlpha(1.0f);
        view.animate()
                .alpha(0f)
                .setDuration(DURATION)
                .setStartDelay(DURATION)
                .start();
    }

    public static void quickBrighten(View view) {
        view.setAlpha(0.5f);
        view.animate()
                .alpha(1.0f)
                .setDuration(DURATION)
                .setStartDelay(DURATION*2)
                .start();
    }

    public static void quickDim(View view) {
        view.setAlpha(1.0f);
        view.animate()
                .alpha(0.5f)
                .setDuration(DURATION)
                .setStartDelay(DURATION)
                .start();
    }
}
