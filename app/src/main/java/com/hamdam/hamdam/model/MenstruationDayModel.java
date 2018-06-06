package com.hamdam.hamdam.model;

import com.github.ebraminio.droidpersiancalendar.models.Day;

import java.util.Date;
import java.util.List;

/**
 * Custom Day class designed to hold both Persian day and menstruation cycle information
 * for display on the calendar.
 */
public class MenstruationDayModel extends Day {
    private boolean mIsPeriod = false, mIsOvulation = false, mIsPeriodProjection = false;
    private int periodDayIndex = 0, ovulationDayIndex = 0, ovDimness = 1;
    private List<DailyStatus> statusList;
    private Date gregorianDate;

    public boolean isPeriod() {
        return mIsPeriod;
    }

    public void setPeriod(boolean period) {
        mIsPeriod = period;
    }

    public List<DailyStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<DailyStatus> statusList) {
        this.statusList = statusList;
    }

    public boolean isOvulation() {
        return mIsOvulation;
    }

    public void setOvulation(boolean ovulation) {
        mIsOvulation = ovulation;
    }

    public void setGregorianDate(Date date){
        this.gregorianDate = date;
    }

    public Date getGregorianDate() {
        return gregorianDate;
    }

    public int getPeriodDayIndex() {
        return periodDayIndex;
    }

    public void setPeriodDayIndex(int periodDayIndex) {
        this.periodDayIndex = periodDayIndex;
    }

    public int getOvulationDayIndex() {
        return ovulationDayIndex;
    }

    public void setOvulationDayIndex(int ovulationDayIndex) {
        this.ovulationDayIndex = ovulationDayIndex;
    }

    public boolean isPeriodProjection() {
        return mIsPeriodProjection;
    }

    public void setPeriodProjection(boolean periodProjection) {
        mIsPeriodProjection = periodProjection;
    }

    public int getOvDimness() {
        return ovDimness;
    }

    public void setOvDimness(int ovDimness) {
        this.ovDimness = ovDimness;
    }
}
