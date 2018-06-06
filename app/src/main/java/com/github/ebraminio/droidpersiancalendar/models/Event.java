package com.github.ebraminio.droidpersiancalendar.models;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;

import com.hamdam.hamdam.util.DateUtil;

import java.util.Date;

/**
 * Originally part of the DroidPersianCalendar project
 * (https://github.com/ebraminio/DroidPersianCalendar).
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>, with minor modifications for this project.
 *
 * Modifications for Hamdam include adding a Gregorian Date constructor
 * option so that Event can be constructed either with Gregorian or Persian date.
 */
public class Event {
    private PersianDate date;
    private String title;

    public Event(PersianDate date, String title) {
        this.setDate(date);
        this.title = title;
    }

    public Event(Date date, String title) {
        this.setDate(date);
        this.title = title;
    }

    public PersianDate getDate() {
        return date;
    }

    public Date getGregorianDate() { return DateUtil.persianToGregorianDate(this.date); }

    public void setDate(PersianDate date) {
        this.date = date;
    }

    // Added: method to construct an event entity with a Gregorian date.
    public void setDate(Date date) {
        this.date = DateUtil.gregorianDateToPersian(date);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
