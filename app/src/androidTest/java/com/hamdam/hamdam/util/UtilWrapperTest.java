package com.hamdam.hamdam.util;

import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;

import com.hamdam.hamdam.model.MenstruationDayModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Test UtilWrapper class.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class UtilWrapperTest extends AndroidTestCase {
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private List<MenstruationDayModel> setMonthList(int month, int day, int timelineLength) {

        int currentMonthInt = Utils.getToday().getMonth();
        int adjustMonthAmount = currentMonthInt - month;

        PersianDate targetDate = new PersianDate(1395, month, day);

        List<MenstruationDayModel> daysList = DateUtil.getFertilityDays(adjustMonthAmount);
        return UtilWrapper
                .fillDaysToCenter(daysList, targetDate, timelineLength,
                adjustMonthAmount);
    }

    @Test // esf 15
    public void testFillDaysShortMonth() throws Exception {
        List<MenstruationDayModel> result = setMonthList(12, 15, 29);
        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue(date.equals(new PersianDate(1395, 12, 15)));
    }

    @Test // esf 15
    public void testFillDaysShortMonthShortTimeline() throws Exception {
        List<MenstruationDayModel> result = setMonthList(12, 15, 11);
        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue("Expected 1395/12/15 but was " + date.getYear()
                        + "/" + date.getMonth()
                        + "/" + date.getDayOfMonth(),
                date.equals(new PersianDate(1395, 12, 15)));

    }

    @Test // fav 15
    public void testFillDaysLongMonth() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 15, 29);
        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue("Expected 1395/1/15 but was " + date.getYear()
                        + "/" + date.getMonth()
                        + "/" + date.getDayOfMonth(),
                date.equals(new PersianDate(1395, 1, 15)));
    }

    @Test // fav 6
    public void testFillDaysPosOffsetLong() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 6, 29);
        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue("Expected 1395/1/6 but was " + date.getYear()
                        + "/" + date.getMonth()
                        + "/" + date.getDayOfMonth(),
                date.equals(new PersianDate(1395, 1, 6)));

    }

    @Test // fav 20
    public void testFillDaysNegOffset() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 20, 29);
        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue("Expected 1395/1/6 but was " + date.getYear()
                        + "/" + date.getMonth()
                        + "/" + date.getDayOfMonth(),
                date.equals(new PersianDate(1395, 1, 20)));

    }

    @Test // fav 1
    public void testFillDaysOverflow() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 31, 29);
        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue("Expected 1395/1/6 but was " + date.getYear()
                        + "/" + date.getMonth()
                        + "/" + date.getDayOfMonth(),
                date.equals(new PersianDate(1395, 1, 31)));

        assertEquals(29, result.size());

    }

    @Test
    public void testUndersizedArray() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 31, 34);

        assertEquals(34, result.size());
    }

    @Test // test a short month with extra padding, make sure timeline long enough
    public void testUndersizedArrayEnoughPadding() throws Exception {
        List<MenstruationDayModel> result = setMonthList(12, 15, 35);

        assertEquals(35, result.size());

        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue("Expected 1395/12/15 but was " + date.getYear()
                        + "/" + date.getMonth()
                        + "/" + date.getDayOfMonth(),
                date.equals(new PersianDate(1395, 12, 15)));
    }

    @Test // pad tail of list, expect month 1
    public void testPadArrayTail() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 22, 25);
        PersianDate endDate = result.get(result.size() - 1)
                .getPersianDate();
        assertEquals(2, endDate.getMonth());

    }

    @Test // pad tail of list, expect month 1
    public void testPadArrayTailWrapMonth() throws Exception {
        List<MenstruationDayModel> result = setMonthList(12, 17, 33);
        PersianDate endDate = result.get(result.size() - 1)
                .getPersianDate();
        assertEquals(1, endDate.getMonth());

    }

    @Test // fav 14; pad beginning of list with prev month - expect month 12
    public void testPadArrayHeadWrapMonth() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 14, 33);
        PersianDate startDate = result.get(0)
                .getPersianDate();
        assertEquals(12, startDate.getMonth());

    }

    @Test
    public void testEvenItemArray() throws Exception {
        List<MenstruationDayModel> result = setMonthList(12, 15, 28);
        PersianDate date = result.get(result.size() / 2)
                .getPersianDate();

        assertTrue(date.equals(new PersianDate(1395, 12, 15)));
    }

    @Test
    public void testWrapNewYear() throws Exception {
        List<MenstruationDayModel> result = setMonthList(12, 20, 28);

        PersianDate date = result.get(result.size() - 1)
                .getPersianDate();

        assertEquals(1396, date.getYear());
    }


    @Test
    public void testWrapPreviousYear() throws Exception {
        List<MenstruationDayModel> result = setMonthList(1, 4, 28);

        PersianDate date = result.get(0)
                .getPersianDate();

        assertEquals(1394, date.getYear());
    }


}
