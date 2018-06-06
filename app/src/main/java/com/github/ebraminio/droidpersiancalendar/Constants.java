package com.github.ebraminio.droidpersiancalendar;

import com.hamdam.hamdam.R;

/**
 * From DroidPersianCalendar project
 * (https://github.com/ebraminio/DroidPersianCalendar)
 *
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class Constants {

    public static final String PREF_THEME = "Theme";
    public static final String LIGHT_THEME = "LightTheme";

    public static final int MONTHS_LIMIT = 800; // this should be an even number
    public static final String OFFSET_ARGUMENT = "OFFSET_ARGUMENT";
    public static final String BROADCAST_INTENT_TO_MONTH_FRAGMENT = "BROADCAST_INTENT_TO_MONTH_FRAGMENT";
    public static final String BROADCAST_FIELD_TO_MONTH_FRAGMENT = "BROADCAST_FIELD_TO_MONTH_FRAGMENT";
    public static final String BROADCAST_FIELD_SELECT_DAY = "BROADCAST_FIELD_SELECT_DAY";
    public static final String BROADCAST_RESTART_APP = "BROADCAST_RESTART_APP";
    public static final int BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY = Integer.MAX_VALUE;

    public static final char PERSIAN_COMMA = '،';
    public static final char RLM = '\u200F';
    public static final char[] ARABIC_DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'};
    public static final char[] PERSIAN_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶',
            '۷', '۸', '۹'};
    public static final String AM_IN_PERSIAN = "ق.ظ";
    public static final String PM_IN_PERSIAN = "ب.ظ";

    public static final int[] DAYS_ICONS = {0,
            R.drawable.day1, R.drawable.day2, R.drawable.day3,
            R.drawable.day4, R.drawable.day5, R.drawable.day6,
            R.drawable.day7, R.drawable.day8, R.drawable.day9,
            R.drawable.day10, R.drawable.day11, R.drawable.day12,
            R.drawable.day13, R.drawable.day14, R.drawable.day15,
            R.drawable.day16, R.drawable.day17, R.drawable.day18,
            R.drawable.day19, R.drawable.day20, R.drawable.day21,
            R.drawable.day22, R.drawable.day23, R.drawable.day24,
            R.drawable.day25, R.drawable.day26, R.drawable.day27,
            R.drawable.day28, R.drawable.day29, R.drawable.day30,
            R.drawable.day31};


}
