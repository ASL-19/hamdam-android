package com.hamdam.hamdam.service.eventbus;

/**
 * Update calendar month.
 */
public class UpdateViewEvent {
    private int tag;
    private boolean period = false;

    public UpdateViewEvent(int tag) {
        this.tag = tag;
    }

    public UpdateViewEvent(int tag, boolean isPeriod) {
        this.tag = tag;
        this.period = isPeriod;
    }

    public int getTag() {
        return tag;
    }

    public boolean isPeriod() {
        return period;
    }
}
