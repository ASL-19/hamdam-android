package com.hamdam.hamdam.service.eventbus;

public class WheelDialogEvent {
    private final int value;
    private final String key;

    public WheelDialogEvent(int value, String key) {
        this.value = value;
        this.key = key;
    }

    public int getValue() {
        return this.value;
    }

    public String getKey() {
        return this.key;
    }
}
