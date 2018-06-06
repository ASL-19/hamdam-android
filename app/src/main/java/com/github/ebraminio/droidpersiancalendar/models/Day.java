package com.github.ebraminio.droidpersiancalendar.models;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;

/**
 * Part of the DroidPersianCalendar project
 * (https://github.com/ebraminio/DroidPersianCalendar).
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 *
 */
public class Day {
    private String num;
    private boolean holiday;
    private boolean today;
    private int dayOfWeek;
    private PersianDate persianDate;
    private boolean event;

    public boolean hasEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public void setHoliday(boolean holiday) {
        this.holiday = holiday;
    }

    public boolean isToday() {
        return today;
    }

    public void setToday(boolean today) {
        this.today = today;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public PersianDate getPersianDate() {
        return persianDate;
    }

    public void setPersianDate(PersianDate persianDate) {
        this.persianDate = persianDate;
    }
}
