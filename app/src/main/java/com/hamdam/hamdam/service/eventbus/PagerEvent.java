package com.hamdam.hamdam.service.eventbus;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;

/**
 * Notify pager of changes to a tagged item
 * by another part of the app (for example, when a user
 * updates their information midway-through using a Viewpager-based
 * quiz).
 */
public class PagerEvent {
    private final int tag;
    private final boolean enabled;
    private final PersianDate date;

    public PagerEvent(PersianDate date, int tag, boolean enabled) {
        this.date = date;
        this.tag = tag;
        this.enabled = enabled;
    }

    public PersianDate getDate() {
        return date;
    }

    public int getTag() {
        return tag;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
