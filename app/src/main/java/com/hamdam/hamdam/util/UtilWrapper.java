package com.hamdam.hamdam.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.Constants;
import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.R;
import com.hamdam.hamdam.enums.StatusEnum;

import com.hamdam.hamdam.model.MenstruationDayModel;
import com.hamdam.hamdam.model.StaticFact;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend functionality of Droid Persian Calendar's utils class to provide custom
 * formatted arrays of Persian digits and fulfill extended utility roles.
 */
public final class UtilWrapper {
    private static final String TAG = "UtilWrapper";

    public static String[] getFormattedStringArray(int min, int range) {
        String[] elements = new String[range];
        for (int i = 0; i < range; i++) {
            elements[i] = formatNumber(Integer.toString(i + min));
        }
        return elements;
    }

    // Alter calendar's formatNumber method to use only persian digits.
    public static String formatNumber(String number) {
        StringBuilder sb = new StringBuilder();
        for (char i : number.toCharArray()) {
            if (Character.isDigit(i)) {
                sb.append(Constants.PERSIAN_DIGITS[Integer.parseInt(i + "")]);
            } else {
                sb.append(i);
            }
        }
        return sb.toString();
    }


    public static int fillDateWheels(NumberPickerWrapper yearPicker, NumberPickerWrapper monthPicker,
                              NumberPickerWrapper dayPicker, int yearRange) {
        PersianDate date = Utils.getToday();

        // Set years
        String[] years = new String[yearRange];
        int startingYearOnYearSpinner = date.getYear();
        for (int i = 0; i < yearRange; i++) { // Set years in decreasing order with current year in top position on the list
            years[yearRange - i - 1] = formatNumber
                    (Integer.toString(startingYearOnYearSpinner - yearRange + i + 1));
        }

        setPickerArray(yearPicker, years, 0);

        String[] months = Utils.monthsNamesOfCalendar();

        setPickerArray(monthPicker, months, date.getMonth() - 1);

        // Set days
        String[] days = new String[31];
        for (int i = 0; i < days.length; ++i) {
            days[i] = formatNumber(Integer.toString(i + 1));
        }
        setPickerArray(dayPicker, days, date.getDayOfMonth() - 1);

        return startingYearOnYearSpinner - yearRange; // The zero-offset of the items in array.
        // An item at position 0 in the spinner would be in year (0 + offset).
    }

    public static void setPickerArray(NumberPickerWrapper picker, final String[] options,
                                      int defaultValue) {

        picker.setMinValue(0);
        picker.setDisplayedValues(options);
        picker.setMaxValue(options.length - 1);

        picker.setFormatter(new NumberPickerWrapper.Formatter() {
            @Override
            public String format(int value) {
                return options[value];
            }
        });
        picker.setValue(defaultValue);
    }

    /*
     * Return list of Days of fixed length to be used in a timeline adapter,
     * such that currentDate is always in centre position in the list.
     *
     * For days in the first half of the month, this means padding the head of the
     * list with days from the previous month; for days in the latter half of the month,
     * this means padding the tail of the list with days from the future month.
     *
     * @param   days        Chronological list of all days in currentDate's Persian calendar month.
     * @param   currentDate PersianDate to put in centre of timeline.
     * @param   timelineLength  integer representing total number of days to show in timeline
     * @param   monthsBack  position relative to the current month; always 0 for current month,
     *                      but preserves option to create timeline for other months by passing in
     *                      a nonzero value.
     */
    public static List<MenstruationDayModel> fillDaysToCenter(List<MenstruationDayModel> days, final PersianDate currentDate,
                                                              final int timelineLength, final int monthsBack) {
        final int TIMELINE_MIDDLE = timelineLength / 2 + 1;
        int offset = TIMELINE_MIDDLE - currentDate.getDayOfMonth(),
                paddingNumber = timelineLength - days.size();
        List<MenstruationDayModel> paddedDays = new ArrayList<>();
        paddedDays.addAll(days);

        try {
            if (offset > 0) { // In first half of month; pad head of list with previous days
                List<MenstruationDayModel> prevMonthDays = DateUtil.getFertilityDays(monthsBack + 1); //one month earlier

                if (paddingNumber > 0) { // Timeline is too short, needs to be filled with extra days
                    List<MenstruationDayModel> nextMonthDays = DateUtil.getFertilityDays(monthsBack - 1);
                    for (int i = 0; i < paddingNumber; i++) {
                        paddedDays.add(nextMonthDays.get(i));
                    }
                }

                // Fill earlier indices in array with days from previous month, shifting days
                // always adding to front of list
                for (int i = 0; i < offset; i++) {
                    paddedDays.add(0,
                            prevMonthDays.get((prevMonthDays.size() - 1 - i)));
                }

            } else { // In latter half of month; pad end of list with future days
                List<MenstruationDayModel> nextMonthDays = DateUtil.getFertilityDays(monthsBack - 1); // one month later

                // Calculate whether any additional days need to be added to the array
                int pOffset = Math.abs(offset);

                // Remove too-early indices
                for (int i = 0; i < pOffset; i++) {
                    paddedDays.remove(0);
                }

                // If too few days displaying, set pOffset to number of additional days needed
                if (paddingNumber > 0) {
                    pOffset += paddingNumber;
                }

                // Fill in later indices in array with days from next month, appending to list
                for (int i = 0; i < pOffset; i++) {
                    paddedDays.add(nextMonthDays.get(i));
                }
            }

            // Trim array if needed
            while (paddedDays.size() > timelineLength) {
                paddedDays.remove(paddedDays.size() - 1);
            }

            return paddedDays;

        } catch (IndexOutOfBoundsException e) {
            // Default to showing only days from current month.
            Log.e(TAG, e.getMessage());
            return days;
        }
    }

    /*
     * Return string resource associated with status label.
     */
    public static String getStatusLabel(Context context, StatusEnum.StatusType type) {
        switch (type) {
            case PAIN:
                return context.getString(com.hamdam.hamdam.R.string.pain);
            case SLEEP:
                return context.getString(com.hamdam.hamdam.R.string.sleep);
            case SEX:
                return context.getString(com.hamdam.hamdam.R.string.sex);
            case FLUIDS:
                return context.getString(com.hamdam.hamdam.R.string.fluids);
            case MOOD:
                return context.getString(com.hamdam.hamdam.R.string.mood);
            case EXERCISE:
                return context.getString(com.hamdam.hamdam.R.string.exercise);
            case BLEEDING:
                return context.getString(com.hamdam.hamdam.R.string.bleeding);
            default:
                break;
        }
        return null;
    }

    /*
     * Return string resource associated with status label.
     */
    public static int getStatusColor(Context context, StatusEnum.StatusType type) {
        switch (type) {
            case PAIN:
                return ContextCompat.getColor(context, com.hamdam.hamdam.R.color.pain);
            case SLEEP:
                return ContextCompat.getColor(context, com.hamdam.hamdam.R.color.sleep);
            case SEX:
                return ContextCompat.getColor(context, com.hamdam.hamdam.R.color.sex);
            case FLUIDS:
                return ContextCompat.getColor(context, com.hamdam.hamdam.R.color.fluids);
            case MOOD:
                return ContextCompat.getColor(context, com.hamdam.hamdam.R.color.mood);
            case EXERCISE:
                return ContextCompat.getColor(context, com.hamdam.hamdam.R.color.exercise);
            case BLEEDING:
                return ContextCompat.getColor(context, com.hamdam.hamdam.R.color.bleeding);
            default:
                break;
        }
        return -1;
    }

    @Nullable
    public static String getValueLabel(Context context, StatusEnum.StatusValue value) {
        if (value != null) {
            // Ordinal is an enum ONE,TWO,THREE,FOUR; values correspond to indices 0-3.
            int index = value.getOrdinal().getValue();
            String[] labels;
            switch (value.getStatusType()) {
                case BLEEDING:
                    labels = context.getResources().getStringArray(com.hamdam.hamdam.R.array.bleeding_labels);
                    break;
                case SEX:
                    labels = context.getResources().getStringArray(com.hamdam.hamdam.R.array.sex_labels);
                    break;
                case PAIN:
                    labels = context.getResources().getStringArray(com.hamdam.hamdam.R.array.pain_labels);
                    break;
                case FLUIDS:
                    labels = context.getResources().getStringArray(com.hamdam.hamdam.R.array.fluids_labels);
                    break;
                case MOOD:
                    labels = context.getResources().getStringArray(com.hamdam.hamdam.R.array.mood_labels);
                    break;
                case EXERCISE:
                    labels = context.getResources().getStringArray(com.hamdam.hamdam.R.array.exercise_labels);
                    break;
                case SLEEP:
                    labels = context.getResources().getStringArray(com.hamdam.hamdam.R.array.sleep_labels);
                    break;
                default:
                    labels = null;
                    break;
            }
            if (labels != null) {
                return labels[index];
            } else {
                Log.e(TAG, "No label found");
            }
        }
        return null;
    }

    public static void setActionBar(final Activity activity, String title,
                                    int barColor, int textColor, boolean isDarkNavBar) {

        TextView toolbarTitle = (TextView) activity.findViewById(com.hamdam.hamdam.R.id.toolbar_text);
        ActionBar mActionBar = ((AppCompatActivity) activity).getSupportActionBar();

        if (mActionBar != null) {
            mActionBar
                    .setBackgroundDrawable
                            (new ColorDrawable(barColor));
            setHamburgerIcon(mActionBar, isDarkNavBar);
        } else {
            Log.e(TAG, "Null actionbar");
        }
        if (toolbarTitle != null) {
            toolbarTitle.setText(title);
            toolbarTitle.setTextSize(20);
            toolbarTitle.setTextColor(textColor);
        }
    }

    public static void setActionBar(final Activity activity, String title,
                                    boolean lightTheme) {
        int barColor, textColor;
        if (lightTheme) {
            textColor = ContextCompat.getColor
                    (activity, com.hamdam.hamdam.R.color.light_background_purple);
            barColor = Color.WHITE;
        } else {
            textColor = Color.WHITE;
            barColor = ContextCompat.getColor
                    (activity, com.hamdam.hamdam.R.color.light_background_purple);
        }
        setActionBar(activity, title, barColor, textColor, lightTheme);
    }


    public static StatusEnum.StatusValue findStatusValue(StatusEnum.Options whichButton, int pageId) {
        StatusEnum.StatusType statusType = StatusEnum.StatusType.getByTag(pageId);
        if (statusType != null && whichButton != null) {
            return StatusEnum.StatusValue.getByOrdinal
                    (statusType, whichButton);
        }
        Log.e(TAG, "Null value found for Status or Button");
        return null;
    }

    public static void setHamburgerIcon(ActionBar actionBar, boolean isDarkNavBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (isDarkNavBar) {
            actionBar.setHomeAsUpIndicator(com.hamdam.hamdam.R.drawable.ic_nav_hamburger);
        } else {
            actionBar.setHomeAsUpIndicator(com.hamdam.hamdam.R.drawable.ic_nav_hamburger_white);
        }
    }

    public static int getBorderColor(StaticFact.TOPIC_TYPES topic) {
        switch (topic) {
            case HEALTH:
                return com.hamdam.hamdam.R.color.health;
            case MARRIAGE_RIGHTS:
                return com.hamdam.hamdam.R.color.marriage_rights;
            default:
                return com.hamdam.hamdam.R.color.light_background_purple;
        }
    }
    public static int getStatusBarColor(StaticFact.TOPIC_TYPES topic) {
        switch (topic) {
            case HEALTH:
                return R.color.health_status_bar;
            case MARRIAGE_RIGHTS:
                return R.color.marriage_rights_status_bar;
            default:
                return R.color.primary_dark;
        }
    }

}
