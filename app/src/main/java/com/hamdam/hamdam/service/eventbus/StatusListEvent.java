package com.hamdam.hamdam.service.eventbus;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.hamdam.hamdam.model.DailyStatus;

import java.util.ArrayList;

/**
 * Pass records of a day's status to display to user.
 */
public class StatusListEvent {
    private PersianDate date;
    private ArrayList<DailyStatus> statuses;

    public StatusListEvent(PersianDate date, ArrayList<DailyStatus> statuses) {
        this.date = date;
        this.statuses = statuses;
    }

    public PersianDate getDate() {
        return date;
    }

    public ArrayList<DailyStatus> getStatuses() {
        return statuses;
    }

}
