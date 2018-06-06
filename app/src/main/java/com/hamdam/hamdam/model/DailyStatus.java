package com.hamdam.hamdam.model;


import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.models.Event;
import com.hamdam.hamdam.enums.StatusEnum;

import java.util.Date;

/**
 * Status class responsible for holding user information
 * as reported daily. Status contains a date, type and a value (subtype).
 */
public class DailyStatus extends Event {

    private StatusEnum.StatusValue statusValue;

    public DailyStatus(Date date, StatusEnum.StatusValue statusValue) {
        super(date, null);
        this.statusValue = statusValue;
    }

    public DailyStatus(PersianDate date, StatusEnum.StatusValue statusValue) {
        super(date, null);
        this.statusValue = statusValue;
    }

    public void setStatusValue(StatusEnum.StatusValue statusValue) {
        this.statusValue = statusValue;
    }

    public StatusEnum.StatusValue getStatusValue() {
        return this.statusValue;

    }

    public StatusEnum.StatusType getType() {
        return this.statusValue.getStatusType();
    }

    public StatusEnum.StatusMain getMainCategory() {
        return this.getType().getStatusMain();
    }

}
