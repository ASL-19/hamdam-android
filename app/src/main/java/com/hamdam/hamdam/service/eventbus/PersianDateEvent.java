package com.hamdam.hamdam.service.eventbus;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;

/**
 * Simple bus to post currently-selected/active date.
 */
public class PersianDateEvent {

    final PersianDate date;

    public PersianDateEvent(PersianDate date) {
        this.date = date;
    }

    public PersianDate getDate() {
        return this.date;
    }

    /**
     * Countdown event representing days until a target date/event.
     */
    public static class CountDownEvent extends PersianDateEvent {
        private int countdown;

        public CountDownEvent(PersianDate date, int countdown) {
            super(date);
            this.countdown = countdown;
        }

        public int getCountdown() {
            return countdown;
        }
    }
}
