package com.hamdam.hamdam.service;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.test.AndroidTestCase;

import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.util.AlarmUtil;
import com.hamdam.hamdam.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Test notification dates
 */
public class NotificationTest extends AndroidTestCase {

    private DatabaseHelperImpl dataBaseHelper;
    private Date d = DateUtil.clearTimeStamp(new Date());
    private Context mContext;
    private Calendar cal;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        mContext = InstrumentationRegistry.getTargetContext();
        dataBaseHelper = DatabaseHelperImpl.getInstance(mContext);
        cal = new GregorianCalendar();
    }

    @Override
    public void tearDown() throws Exception {
        dataBaseHelper.close();
        mContext.deleteDatabase(dataBaseHelper.getDatabaseName());
        cal = null;
        super.tearDown();
    }

    @SmallTest
    public void testAlarmLateCycleStats() {
        final int LATENESS = 5;
        addStats(3, LATENESS);
        cal.setTime(new Date());
        Date testDate = DateUtil.clearTimeStamp(AlarmUtil.getAlarmDateNoOffset
                (mContext, AlarmUtil.RequestCodes.PERIOD_ALARM));
        cal.add(Calendar.DATE, LATENESS);
        Date expectedDate = DateUtil.clearTimeStamp(cal.getTime());
        assertEquals(expectedDate, testDate);
    }

    @SmallTest
    public void testAlarmDueTodayCycleStats() {
        final int DUE = 0;
        addStats(3, DUE);
        cal.setTime(new Date());
        Date testDate = DateUtil.clearTimeStamp(AlarmUtil.getAlarmDateNoOffset
                (mContext, AlarmUtil.RequestCodes.PERIOD_ALARM));
        cal.add(Calendar.DATE, DUE);
        Date expectedDate = DateUtil.clearTimeStamp(cal.getTime());
        assertEquals(expectedDate, testDate);
    }

    @SmallTest
    public void testAlarmEarlyCycleStats() {
        final int EARLYNESS = -5;
        addStats(3, EARLYNESS);
        cal.setTime(new Date());
        Date testDate = DateUtil.clearTimeStamp(AlarmUtil.getAlarmDateNoOffset
                (mContext, AlarmUtil.RequestCodes.PERIOD_ALARM));
        cal.add(Calendar.DATE, EARLYNESS);
        Date expectedDate = DateUtil.clearTimeStamp(cal.getTime());
        assertEquals(expectedDate, testDate);
    }

    // Add period stats to database. Offset refers to when cycle prediction should fall.
    // 0 means current day, numbers < 0 mean cycle should be late by n days,
    // numbers > 0 mean cycle is due in n days.
    private void addStats(int reps, int offset) {
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * (Constants.DEFAULT_CYCLE_LENGTH - offset));
        Date mCurrent = cal.getTime();

        for (int i = 0; i < reps; i++) {
            dataBaseHelper.updatePeriodStats(mCurrent, 3);
            cal.add(Calendar.DATE, -1 * Constants.DEFAULT_CYCLE_LENGTH);
            mCurrent = cal.getTime();
        }
    }

    @SmallTest
    public void testNotificationDate() {
        final int EARLYNESS = 5;
        addStats(3, EARLYNESS);
        cal.setTime(new Date());
        Date curious = dataBaseHelper.projectNextStartDate(new Date());
        Date testDate = DateUtil.clearTimeStamp(AlarmUtil.getAlarmDateNoOffset
                (mContext, AlarmUtil.RequestCodes.PERIOD_ALARM));
        cal.add(Calendar.DATE, EARLYNESS);
        Date expectedDate = DateUtil.clearTimeStamp(cal.getTime());
        assertEquals(expectedDate, testDate);
    }
}
