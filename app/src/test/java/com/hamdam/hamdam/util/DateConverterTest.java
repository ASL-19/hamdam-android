package com.hamdam.hamdam.util;

import com.github.ebraminio.droidpersiancalendar.calendar.CivilDate;
import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * Test Date Converter class.
 */
public class DateConverterTest extends TestCase {

    private PersianDate persianDate1;
    private PersianDate persianDate2;
    private Date date1;
    private SimpleDateFormat sdf;

    @Before
    public void setUp() throws Exception {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2016, Calendar.JULY, 12);
        date1 = DateUtil.clearTimeStamp(calendar.getTime());
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    }

    @After
    public void tearDown() throws Exception {
        date1 = null;
        persianDate1 = null;
        persianDate2 = null;
        sdf = null;
    }

    @Test
    public void testPersianToGregorianDate() throws Exception {
        persianDate1 = new PersianDate(1395, 4, 22); // July 12 2016
        Date actual = DateUtil.persianToGregorianDate(persianDate1);
        assertEquals(date1, actual);
    }

    @Test // Persian Leap Day
    public void testPersianToGregorianDateLeap() throws Exception {
        persianDate1 = new PersianDate(1395, 12, 30); // leap year
        Date actual = DateUtil.persianToGregorianDate(persianDate1);
        Calendar calendar = new GregorianCalendar();
        calendar.set(2017, Calendar.MARCH, 20);

        Date gDate = DateUtil.clearTimeStamp(calendar.getTime());
        assertEquals(gDate, actual);
    }

    @Test // Gregorian Leap Day
    public void testGregorianDateToPersianLeap() throws Exception {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2016, Calendar.FEBRUARY, 29);
        Date leapDate = DateUtil.clearTimeStamp(calendar.getTime());
        persianDate1 = DateUtil.gregorianDateToPersian(leapDate);
        PersianDate persianDate3 = new PersianDate(1394, 12, 10);
        assertEquals(persianDate3.getYear(), persianDate1.getYear());
        assertEquals(persianDate3.getMonth(), persianDate1.getMonth());
    }

    @Test
    public void testGregorianDateToPersian() throws Exception {
        persianDate1 = DateUtil.gregorianDateToPersian(date1);
        PersianDate persianDate3 = new PersianDate(1395, 4, 22); // 12 july 2016
        assertEquals(persianDate3.getYear(), persianDate1.getYear());
        assertEquals(persianDate3.getMonth(), persianDate1.getMonth());
        assertEquals(persianDate3.getDayOfMonth(), persianDate1.getDayOfMonth());
    }

    @Test
    public void testGregorianTwiceConvert() throws Exception {
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.JANUARY, 31);
        Date expected = DateUtil.clearTimeStamp(cal.getTime());
        persianDate1 = DateUtil.gregorianDateToPersian(expected);
        Date actual = DateUtil.clearTimeStamp(DateUtil.persianToGregorianDate(persianDate1));
        assertEquals(expected, actual);
    }

    @Test
    public void testPersianTwiceConvert() throws Exception {
        PersianDate expected = new PersianDate(1397, 11, 11); // Jan 31 2017
        Date intermed = DateUtil.persianToGregorianDate(expected);
        PersianDate actual = DateUtil.gregorianDateToPersian(intermed);
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }

    @Test
    public void testCivilToDate() throws Exception {
        CivilDate civilDate = new CivilDate(2016, 7, 12); // july 12 2016 (Civil has nonzero month)
        Date expected = date1;
        Date actual = DateUtil.civilToDate(civilDate);
        assertEquals(expected, actual);
    }


    public void testIsFutureDateTrue() throws Exception {
        Date d = sdf.parse("2017-08-08");
        assertTrue(DateUtil.isFutureDate(d));
    }

    public void testIsFutureDateFalse() throws Exception {
        Date d = sdf.parse("2015-09-09");
        assertFalse(DateUtil.isFutureDate(d));
    }

    public void testIsFutureDatePresentDay() {
        Date d = DateUtil.clearTimeStamp((new Date()));
        assertFalse(DateUtil.isFutureDate(d));
    }
}
