package com.hamdam.hamdam.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ebraminio.droidpersiancalendar.calendar.CivilDate;
import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.models.Day;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;

import com.hamdam.hamdam.model.MenstruationDayModel;
import com.hamdam.hamdam.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class to extend function of DroidPersianCalendar's
 * Date manipulation methods and provide conversion between Gregorian and Jalali
 * dates, as well as date comparison.
 */
public class DateUtil {
    private static final String TAG = "DateUtil";
    /*
     * Wrapper for converting PersianDate object to Date object.
     */
    public static Date persianToGregorianDate(PersianDate persianDate) {
        CivilDate civ = com.github.ebraminio.droidpersiancalendar.utils.DateConverter.persianToCivil(persianDate);
        return civilToDate(civ);
    }

    /*
     * Wrapper for converting Date object to PersianDate.
     */
    public static PersianDate gregorianDateToPersian(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        CivilDate civ = new CivilDate(calendar);
        return com.github.ebraminio.droidpersiancalendar.utils.DateConverter.civilToPersian(civ);
    }

    /*
     * Coverts a CivilDate object (which has Y, M, D) to a Date object with time 00:00:00.
     */
    public static Date civilToDate(CivilDate civilDate) {
        Calendar cal = new GregorianCalendar();
        cal.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        return clearTimeStamp(cal.getTime());
    }

    /*
     * Clears the timestamp, resetting to 00:00:00 (midnight). Because PersianDate
     * objects do not store a timestamp, it would be inaccurate to construct a Date object
     * with timestamp from a PersianDate object.
     */
    @Nullable
    public static Date clearTimeStamp(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // This is a very simplistic calculation to approximate how long a user's cycle will be. It will
    // always be rounded to the nearest whole day. It takes into account leap days, but does not
    // check daylight savings time, since all times are calculated at midnight.
    public static int getDaysApart(@NonNull Date recent, @NonNull Date previous) {
        final long milli_to_day = 1000 * 60 * 60 * 24L;
        long offset = 0; // if leap year or dst
        PersianDate recentDate = gregorianDateToPersian(recent);
        PersianDate prevDate = gregorianDateToPersian(previous);

        // Check leap years
        if (prevDate.isLeapYear() && (prevDate.getMonth() == 12)
                && (recentDate.getMonth() == 1)) {
            offset = milli_to_day; // add a leap day
        }

        // Not checking DST--days are calculated at midnight (Time 00:00:00).
        int apart = Math.abs(Math.round((clearTimeStamp(recent).getTime() -
                clearTimeStamp(previous).getTime() + offset) /
                (float) milli_to_day));

        return apart;
    }

    // return True if date is future, false if it is today or in the past.
    public static boolean isFutureDate(@NonNull Date d) {
        Date current = DateUtil.clearTimeStamp((new Date()));
        return (d.getTime() - current.getTime() > 0);
    }


    /*
     * Shift date PersianDate by the number of months indicated by monthShift.
     * Note that shift can be positive or negative.
     * Months are indexed at 1.
     */
    @Nullable
    public static PersianDate shiftDate(@Nullable PersianDate todayDate, int monthShift) {
        if (todayDate == null) {
            return null;
        }
        int month = todayDate.getMonth() - monthShift;
        month -= 1;
        int year = todayDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        todayDate.setMonth(month);
        todayDate.setYear(year);
        todayDate.setDayOfMonth(1);

        return todayDate;
    }

    /*
     *  Compare a gregorian Event Date to a Persian Date by month.
     *  If eventDate is in the same month as compareDate, return 0.
     *  If eventDate is before compareDate, return 1.
     *  If eventDate is after compareDate, return -1.
     */
    public static int compareDate(@Nullable Date eventDate, @Nullable PersianDate compareDate) {
        int result = -1;

        if (eventDate != null && compareDate != null) {
            PersianDate persianEventDate = DateUtil.gregorianDateToPersian(eventDate);

            if (compareDate.getMonth() == persianEventDate.getMonth()
                    && compareDate.getYear() == persianEventDate.getYear()) {
                result = 0;

            } else if (compareDate.getYear() > persianEventDate.getYear()) {
                result = 1;

            } else if (compareDate.getMonth() > persianEventDate.getMonth()) {
                result = 1;
            }

        }
        return result;
    }

    // Return a future date that is <increment> days from current date.
    @Nullable
    public static Date rollCalendar(@Nullable Date current, int increment) {
        if (current == null) {
            return null;
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(current);

        calendar.add(Calendar.DATE, increment);
        return calendar.getTime();
    }

    public static List<MenstruationDayModel> getFertilityDays(List<Day> days) {
        List<MenstruationDayModel> cycleDays = new ArrayList<>();

        for (Day d : days) {
            MenstruationDayModel fDay = new MenstruationDayModel();
            fDay.setDayOfWeek(d.getDayOfWeek());
            fDay.setPersianDate(d.getPersianDate());
            fDay.setNum(d.getNum());
            fDay.setToday(d.isToday());
            fDay.setGregorianDate(DateUtil
                    .persianToGregorianDate
                            (fDay.getPersianDate()));

            cycleDays.add(fDay);
        }
        return cycleDays;
    }
    /*
     * Adapt calendar to use Fertility days, child of Day.
     */
    public static List<MenstruationDayModel> getFertilityDays(int offset) {
        List<Day> days = Utils.getDays(offset);

        return getFertilityDays(days);
    }

    /*
     * Add period and ovulation to a list of MenstruationDayModel objects by examining the accompanying records.
     */
    public static List<MenstruationDayModel> setFertilityMonth(List<MenstruationDayModel> days,
                                                               Map<Date, Integer> periodRecords,
                                                               List<Date> ovRecords,
                                                               Map<Date, Integer> periodProjections,
                                                               Map<Date, Integer> ovProjections) {

        // Check for events that start or end outside range, but are partially within range
        HashMap<Date, Integer> endPeriodDates = new HashMap<>();
        List<Date> endOvulationDays = new ArrayList<>();
        for (Date startPeriod : periodRecords.keySet()) {
            Date endPeriod = DateUtil.rollCalendar(startPeriod, periodRecords.get(startPeriod));
            endPeriodDates.put(endPeriod, periodRecords.get(startPeriod));
        }
        for (Date startOv : ovRecords) {
            Date endOv = DateUtil.rollCalendar(startOv, Constants.DEFAULT_OVULATION_LENGTH);
            endOvulationDays.add(endOv);
        }

        for (MenstruationDayModel d : days) {
            int index = days.indexOf(d);
            if (periodRecords.keySet().contains(d.getGregorianDate())) { // Set period to 'true' based on period lengths in db
                int length = periodRecords.get(d.getGregorianDate());
                for (int i = index; i < index + length; i++) {
                    if (i < days.size()) {
                        days.get(i).setPeriod(true);
                        days.get(i).setPeriodDayIndex(i - index); // day of period (0, 1...( for UI shading
                    }
                }
                periodRecords.keySet().remove(d.getGregorianDate()); // so don't recheck when checking startDates
            }

            if (ovRecords.contains(d.getGregorianDate())) { // set ovulation to true
                for (int i = index; i < index + Constants.DEFAULT_OVULATION_LENGTH; i++) {
                    if (i < days.size()) {
                        days.get(i).setOvulation(true);
                        days.get(i).setOvulationDayIndex(i - index); // day of ovulation (0, 1, ...) for UI shading

                        // Set dimness--lower means dimmer
                        days.get(i).setOvDimness(calculateDimness(i - index,
                                Constants.DEFAULT_OVULATION_LENGTH));
                    }
                }
                ovRecords.remove(d.getGregorianDate()); // don't recheck
            }

            // Predictions
            if (periodProjections.keySet().contains(d.getGregorianDate())) {
                for (int i = index; i < index + periodProjections.get(d.getGregorianDate()); i++) {
                    if (i < days.size() && !days.get(i).isPeriod()) {
                        days.get(i).setPeriodProjection(true);
                        days.get(i).setPeriodDayIndex(i - index);
                    }
                }
                periodProjections.keySet().remove(d.getGregorianDate());
            }

            if (ovProjections.keySet().contains(d.getGregorianDate())) {
                int duration = ovProjections.get(d.getGregorianDate());
                for (int i = index; i < index + duration; i++) {
                    if (i < days.size()) {
                        days.get(i).setOvulation(true);
                        days.get(i).setOvulationDayIndex(i - index);

                        // Set dimness for ovulation--lower means dimmer
                        // Events from previous months have to have different dimness calculation
                        if (duration < Constants.DEFAULT_OVULATION_LENGTH) {
                            days.get(i).setOvDimness(calculateDimness(duration - (i - index),
                                    Constants.DEFAULT_OVULATION_LENGTH));
                        }
                        else {
                            days.get(i).setOvDimness(calculateDimness(i - index,
                                    Constants.DEFAULT_OVULATION_LENGTH));
                        }
                    }
                }
                ovProjections.keySet().remove(d.getGregorianDate());
            }

            // Some periods and ovulation will start out of range; look at end dates and count backwards
            // index -=1 in order to count back to start day.
            index--;

            if (endPeriodDates.keySet().contains(d.getGregorianDate())) {
                for (int i = index; i > index - endPeriodDates.get(d.getGregorianDate()); i--) {
                    if (i >= 0 && !days.get(i).isPeriod()) {
                        days.get(i).setPeriod(true);
                        days.get(i).setPeriodDayIndex(index - i);
                    }
                }
                endPeriodDates.keySet().remove(d.getGregorianDate()); // found it, don't recheck
            }

            // Some periods and ovulation will start out of range; look at end dates and count backwards
            if (endOvulationDays.contains(d.getGregorianDate())) {
                for (int i = index; i > index - Constants.DEFAULT_OVULATION_LENGTH; i--) {
                    if (i >= 0 && !days.get(i).isPeriod()) {
                        days.get(i).setOvulation(true);
                        days.get(i).setOvulationDayIndex(index - i);

                        // Set a dimness for ovulation--a lower number means dimmer
                        days.get(i).setOvDimness(calculateDimness
                                (index - i, Constants.DEFAULT_OVULATION_LENGTH));
                    }
                }
                endOvulationDays.remove(d.getGregorianDate()); // don't recheck
            }
        }
        return days;
    }

    /*
     * Create dimness gradient for ovulation days--the lower the result is, the more dimmed the
     * day icon should be.
     */
    public static int calculateDimness(int index, int length) {
        if (index < length / 2) {
            return index;
        } else {
            return length - 1 - index;
        }
    }

    public static List<MenstruationDayModel> clearFertilityRecords(List<MenstruationDayModel> days) {
        for (MenstruationDayModel d : days) {
            d.setOvulation(false);
            d.setPeriod(false);
        }
        return days;
    }

}
