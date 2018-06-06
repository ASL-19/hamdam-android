package com.github.ebraminio.droidpersiancalendar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.github.ebraminio.droidpersiancalendar.Constants;
import com.github.ebraminio.droidpersiancalendar.calendar.AbstractDate;
import com.github.ebraminio.droidpersiancalendar.calendar.CivilDate;
import com.github.ebraminio.droidpersiancalendar.calendar.DayOutOfRangeException;
import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.models.Day;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * https://github.com/ebraminio/DroidPersianCalendar
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class Utils {

    private Context context;
    private Typeface typeface;
    private SharedPreferences prefs;
    private static final String[] PERSIAN_MONTHS = {
            "فروردین",
            "اردیبهشت",
            "خرداد",
            "تیر",
            "مرداد",
            "شهریور",
            "مهر",
            "آبان",
            "آذر",
            "دی",
            "بهمن",
            "اسفند"};


    private Utils(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static WeakReference<Utils> myWeakInstance;

    public static Utils getInstance(Context context) {
        if (myWeakInstance == null || myWeakInstance.get() == null) {
            myWeakInstance = new WeakReference<>(new Utils(context.getApplicationContext()));
        }
        return myWeakInstance.get();
    }

    private void initTypeface() {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/iransans.ttf");
        }
    }

    public void setTypeface(TextView textView) {
        initTypeface();
        textView.setTypeface(typeface);
    }

    public void setFont(TextView textView) {
        setTypeface(textView);
        textView.setText(textView.getText().toString());
    }

    public void setFontShapeAndGravity(TextView textView) {
        setFont(textView);
        textView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
    }

    public void setFont(PreferenceViewHolder holder) {
        // See android.support.v7.preference.Preference#onBindViewHolder
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        if (titleView != null) {
            setFont(titleView);
        }
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (summaryView != null) {
            setFont(summaryView);
        }
    }

    private static char[] preferredDigits = Constants.PERSIAN_DIGITS;

    public void setTheme(Context context) {
        context.setTheme(com.hamdam.hamdam.R.style.HamdamTheme);
    }

    public String getTheme() {
        return prefs.getString(Constants.PREF_THEME, Constants.LIGHT_THEME);
    }

    public static PersianDate getToday() {
        return DateConverter.civilToPersian(new CivilDate(new GregorianCalendar()));
    }


    public static String formatNumber(int number) {
        return formatNumber(Integer.toString(number));
    }

    public static String formatNumber(String number) {
        if (preferredDigits == Constants.ARABIC_DIGITS)
            return number;

        StringBuilder sb = new StringBuilder();
        for (char i : number.toCharArray()) {
            if (Character.isDigit(i)) {
                sb.append(preferredDigits[Integer.parseInt(i + "")]);
            } else {
                sb.append(i);
            }
        }
        return sb.toString();
    }

    public static String dateToString(AbstractDate date) {
        return formatNumber(date.getDayOfMonth()) + ' ' + getMonthName(date) + ' ' +
                formatNumber(date.getYear());
    }


    public static String[] monthsNamesOfCalendar() {
        return PERSIAN_MONTHS;
    }

    public static String getMonthName(AbstractDate date) {
        return monthsNamesOfCalendar()[date.getMonth() - 1];
    }

    public static List<Day> getDays(int offset) {
        List<Day> days = new ArrayList<>();
        PersianDate persianDate = getToday();
        int month = persianDate.getMonth() - offset;
        month -= 1;
        int year = persianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        persianDate.setMonth(month);
        persianDate.setYear(year);
        persianDate.setDayOfMonth(1);

        int dayOfWeek = DateConverter.persianToCivil(persianDate).getDayOfWeek() % 7;

        try {
            PersianDate today = getToday();
            for (int i = 1; i <= 31; i++) {
                persianDate.setDayOfMonth(i);

                Day day = new Day();
                day.setNum(formatNumber(i));
                day.setDayOfWeek(dayOfWeek);

                day.setPersianDate(persianDate.clone());

                if (persianDate.equals(today)) {
                    day.setToday(true);
                }

                days.add(day);
                dayOfWeek++;
                if (dayOfWeek == 7) {
                    dayOfWeek = 0;
                }
            }
        } catch (DayOutOfRangeException e) {
            // @author ebraminio: it was expected
        } catch (NullPointerException e) {
            Log.e("PersianDate", e.getMessage());
        }

        return days;
    }
}
