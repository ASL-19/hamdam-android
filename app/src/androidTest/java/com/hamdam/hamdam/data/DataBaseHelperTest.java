package com.hamdam.hamdam.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.model.DailyStatus;
import com.hamdam.hamdam.presenter.DatabaseHelperImpl;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Test class for DatabaseHelperImpl.
 */
public class DataBaseHelperTest extends AndroidTestCase {

    private DatabaseHelperImpl dataBaseHelper;
    private SQLiteDatabase db;
    private Date d = DateUtil.clearTimeStamp(new Date());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    DailyStatus dailyStatus = new DailyStatus(d, StatusEnum.StatusValue.AVERAGE_SLEEP);
    private Context mContext;
    private SharedPreferences preferences;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        //mContext = getInstrumentation().getContext();
        dataBaseHelper = DatabaseHelperImpl.getInstance(mContext);
        db = dataBaseHelper.getWritableDatabase();
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        setDefaultTestPreferences(preferences);

    }

    private void setDefaultTestPreferences(SharedPreferences prefs) {
        prefs.edit()
                .putString(mContext.getString(com.hamdam.hamdam.R.string.period_length_key), "28")
                .putString(mContext.getString(com.hamdam.hamdam.R.string.last_period_date_key), "")
                .apply();
    }

    @Override
    public void tearDown() throws Exception {
        dataBaseHelper.close();
        boolean result = mContext.deleteDatabase(dataBaseHelper.getDatabaseName());
        assertTrue("Delete Database returned false", result);
        preferences.edit()
                .clear()
                .apply();
        super.tearDown();
    }


    @SmallTest
    public void testUpdatePeriodStatsNotEmpty() throws Exception {
        dataBaseHelper.updatePeriodStats(d, Constants.DEFAULT_PERIOD_LENGTH);
        Cursor c = db.rawQuery("SELECT * FROM PeriodStats ORDER BY StartDate DESC", null);
        assertTrue("Empty cursor", c.moveToFirst());
        c.close();
    }

    @SmallTest
    public void testUpdatePeriodStatsGetDate() throws Exception {
        dataBaseHelper.updatePeriodStats(d, Constants.DEFAULT_PERIOD_LENGTH);
        Cursor c = db.rawQuery("SELECT * FROM PeriodStats ORDER BY StartDate DESC", null);
        if (c.moveToFirst()) {
            Date newD = dateFormat.parse(c.getString(c.getColumnIndex("StartDate")));
            Date expected = DateUtil.clearTimeStamp(d);
            assertEquals(expected, newD);
        } else {
            fail("Empty cursor when updated period expected");
        }
        c.close();
    }

    //@Test
    public void testDeleteDailyStatus() throws Exception {
        dataBaseHelper.updateStatus(new DailyStatus(d, StatusEnum.StatusValue.BACKACHE));
        dataBaseHelper.deleteDailyStatus(new DailyStatus(d, StatusEnum.StatusValue.BACKACHE));
        Cursor c = db.query("DailyInfo", null, "StatusValue =?", new String[]{StatusEnum.StatusValue.BACKACHE.name()},
                null, null, null);
        assertFalse(c.moveToFirst());
        c.close();
    }

    //@Test
    public void testGetAllStatusToday() throws Exception {
        DailyStatus breastStatus = new DailyStatus(new Date(), StatusEnum.StatusValue.BREASTS);
        dataBaseHelper.updateStatus(dailyStatus);
        dataBaseHelper.updateStatus(breastStatus);
        ArrayList<DailyStatus> ds = new ArrayList<>();
        ds.add(dailyStatus);
        ds.add(breastStatus);

        ArrayList<DailyStatus> actual = dataBaseHelper.getStatusToday(DateUtil.clearTimeStamp((new Date())));
        assertEquals(ds.size(), actual.size());
    }

    //@Test - Override "High Energy" with "Low Energy" but not "medium" (different day) or "sleep" (different type)
    public void testOverrideDailyStatus() throws Exception {
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-06"), StatusEnum.StatusValue.ABNORMAL));
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-06"), StatusEnum.StatusValue.EGGWHITE));
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-06"), StatusEnum.StatusValue.STICKY));
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-07"), StatusEnum.StatusValue.CREAMY));
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-06"), StatusEnum.StatusValue.LOTS_SLEEP));
        Cursor c = db.rawQuery("Select StatusValue from DailyInfo where StartDate=? and StatusType=?",
                new String[]{"2016-06-06", StatusEnum.StatusValue.CREAMY.getStatusType().name()});
        if (!c.moveToFirst()) {
            fail("expected status results, found empty cursor");
        }
        assertEquals(StatusEnum.StatusValue.STICKY.name(), c.getString(c.getColumnIndex("StatusValue")));
        c.close();
    }

    //@Test
    public void testDontOverrideStatus() throws Exception {
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-06"), StatusEnum.StatusValue.BREASTS));
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-06"), StatusEnum.StatusValue.BACKACHE));
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-06-06"), StatusEnum.StatusValue.CREAMY));
        dataBaseHelper.updateStatus(new DailyStatus(dateFormat.parse("2016-05-06"), StatusEnum.StatusValue.CREAMY));

        Cursor c = db.rawQuery("Select StatusValue from DailyInfo where StartDate=? and StatusType=?",
                new String[]{"2016-06-06", StatusEnum.StatusValue.CREAMY.getStatusType().name()});
        if (!c.moveToFirst()) {
            fail("expected status results, found empty cursor");
        }
        assertEquals(StatusEnum.StatusValue.CREAMY.name(), c.getString(c.getColumnIndex("StatusValue")));
    }

    //@Test
    public void testGetDaysApart() throws Exception { //Complicated: this references gregorian dates?
        int expected = 3;
        float actual = DateUtil.getDaysApart(dateFormat.parse("2016-06-06"), dateFormat.parse("2016-06-03"));
        assertEquals(expected, Math.round(actual));
        actual = DateUtil.getDaysApart(dateFormat.parse("2016-07-03"), dateFormat.parse("2016-06-30"));
        assertEquals(expected, Math.round(actual));
        actual = DateUtil.getDaysApart(dateFormat.parse("2016-08-02"), dateFormat.parse("2016-07-30"));
        assertEquals(expected, Math.round(actual));
        expected = 28;
        actual = DateUtil.getDaysApart(dateFormat.parse("2016-03-25"), dateFormat.parse("2016-02-26"));
        assertEquals(expected, Math.round(actual));

    }

    //@Test
    public void testGetAverageCycleLength() throws Exception {
        updatePeriodDb();
        assertEquals(28, dataBaseHelper.getAverageCycleLength());

    }

    //@Test
    public void testGetCycleLengths() throws Exception {
        HashMap<Date, Integer> testLens = new HashMap<>();
        Date d1 = dateFormat.parse("2016-01-01");
        Date d2 = dateFormat.parse("2016-01-29");
        Date d3 = dateFormat.parse("2016-02-26");
        Date d4 = dateFormat.parse("2016-03-25");
        int len = 28;
        testLens.put(d1, len);
        testLens.put(d2, len);
        testLens.put(d3, len);

        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);
        dataBaseHelper.updatePeriodStats(d2, Constants.DEFAULT_PERIOD_LENGTH);
        dataBaseHelper.updatePeriodStats(d3, Constants.DEFAULT_PERIOD_LENGTH);
        dataBaseHelper.updatePeriodStats(d4, Constants.DEFAULT_PERIOD_LENGTH);
        HashMap actual = dataBaseHelper.getCycleLengths();
        assertTrue(("actual: " + actual.toString() + "expected: " + testLens.toString()), (testLens.equals(actual)));
    }

    // @Test // Note: use on future!
    public void testDaysTillStartOne() throws Exception {
        updatePeriodDb();

        assertEquals(27, dataBaseHelper.daysTillStartDate(dateFormat.parse("2016-03-26")));
    }

    // @Test
    public void testDaysTillStartHalf() throws Exception {
        updatePeriodDb();

        assertEquals(14, dataBaseHelper.daysTillStartDate(dateFormat.parse("2016-04-08")));
    }


    // @Test
    public void testDaysTillStartThirteen() throws Exception {
        updatePeriodDb();

        assertEquals(13, dataBaseHelper.daysTillStartDate(dateFormat.parse("2016-04-09")));
    }


    // @Test // Should use defaults if only one date
    public void testDaysTillStartLate() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);

        assertEquals(-4, dataBaseHelper.daysTillStartDate(dateFormat.parse("2016-02-02")));
    }


    // @Test // Should use defaults if only one date
    public void testDaysTillStartVeryLate() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);

        assertEquals(-64, dataBaseHelper.daysTillStartDate(dateFormat.parse("2016-04-02")));
    }

    // @Test // Should use defaults if only one date
    public void testDaysTillStartPeriodToday() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);

        assertEquals(0, dataBaseHelper.daysTillStartDate(dateFormat.parse("2016-01-29")));
    }


    // @Test // Should use defaults if only one date
    public void testCalculateCyclePeriodToday() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);

        assertEquals(28, dataBaseHelper.calculateCyclePosition(dateFormat.parse("2016-01-29")));
    }


    // @Test // Should use defaults if only one date
    public void testCalculateCycleLate() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);

        assertEquals(32, dataBaseHelper.calculateCyclePosition(dateFormat.parse("2016-02-02")));
    }


    // @Test
    public void testGetLastStartDateOneEntry() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);
        assertEquals(d1, dataBaseHelper.getLastStartDate(dateFormat.parse("2016-01-02")));
    }

    // @Test // insert older date after newer date -- newer date should still be returned
    public void testGetLastStartDateMultiEntry() throws Exception {
        Date d1 = dateFormat.parse("2016-03-25");
        Date d2 = dateFormat.parse("2016-01-01");
        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);
        assertEquals(d1, dataBaseHelper.getLastStartDate(dateFormat.parse("2016-03-27")));
    }

    //@Test
    public void testProjectStartDateEmpty() throws Exception {

        Date d = dataBaseHelper.projectNextStartDate(dateFormat.parse("2016-03-26"));
        assertNull(d);
    }

    //@Test
    public void testProjectStartDateFuture() throws Exception {
        updatePeriodDb();

        Date d = dataBaseHelper.projectNextStartDate(dateFormat.parse("2016-03-26"));
        assertEquals(dateFormat.parse("2016-04-22"), d);
    }

    //@Test // gives next cycle.
    public void testProjectStartDateDayTooEarly() throws Exception {
        updatePeriodDb();

        Date d = dataBaseHelper.projectNextStartDate(dateFormat.parse("2016-01-29"));
        assertEquals(dateFormat.parse("2016-02-26"), d);
    }

    public void testGetNextPeriodRecordNullEmpty() throws Exception {
        Date d = dataBaseHelper.getNextPeriodRecord(new Date());
        assertNull(d);
    }

    public void testGetNextPeriodRecordNullExisting() throws Exception {
        updatePeriodDb();
        Date d = dataBaseHelper.getNextPeriodRecord(new Date());
        assertNull(d);
    }

    public void testGetNextPeriodRecordValidDate() throws Exception {
        updatePeriodDb();
        Date d = dataBaseHelper.getNextPeriodRecord(dateFormat.parse("2016-02-26"));
        assertEquals(dateFormat.parse("2016-03-25"), d);

    }

    // return the date right after target, even if there are other more recent records
    public void testGetNextPeriodRecordTestOrder() throws Exception {
        updatePeriodDb();
        Date d = dataBaseHelper.getNextPeriodRecord(dateFormat.parse("2016-01-29"));
        assertEquals(dateFormat.parse("2016-02-26"), d);
    }

    //@Test
    public void testProjectStartDateDayOne() throws Exception {
        updatePeriodDb();

        Date d = dataBaseHelper.projectNextStartDate(dateFormat.parse("2016-02-27"));
        assertEquals(dateFormat.parse("2016-03-25"), d);
    }

    // If 'projection' is for day with currently-recorded cycle, project forward to next month.
    public void testProjectStartDateSameDay() throws Exception {
        updatePeriodDb();

        Date d = dataBaseHelper.projectNextStartDate(dateFormat.parse("2016-03-25"));
        assertEquals(dateFormat.parse("2016-04-22"), d);
    }

    private void updatePeriodDb() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        Date d2 = dateFormat.parse("2016-01-29");
        Date d3 = dateFormat.parse("2016-02-26");
        Date d4 = dateFormat.parse("2016-03-25");

        dataBaseHelper.updatePeriodStats(d1, Constants.DEFAULT_PERIOD_LENGTH);
        dataBaseHelper.updatePeriodStats(d2, Constants.DEFAULT_PERIOD_LENGTH);
        dataBaseHelper.updatePeriodStats(d3, Constants.DEFAULT_PERIOD_LENGTH);
        dataBaseHelper.updatePeriodStats(d4, Constants.DEFAULT_PERIOD_LENGTH);
    }

    //@Test // Ovulation will be at [startdate] - offset.
    public void testProjectOvulationStart() throws Exception {
        Date d = dateFormat.parse("2016-08-01");
        dataBaseHelper.updatePeriodStats(d, Constants.DEFAULT_PERIOD_LENGTH);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(d);
        calendar.add(Calendar.DATE, -1 * Constants.DEFAULT_OVULATION_OFFSET);
        assertEquals(calendar.getTime(), dataBaseHelper.calculateOvulationStart(d));
    }

    //@Test // Ovulation will be at [startdate] - 14.
    public void testProjectOvulationStartLongCycle() throws Exception {
        Date d = dateFormat.parse("2016-08-01");
        Date d2 = dateFormat.parse("2016-08-30");
        dataBaseHelper.updatePeriodStats(d, Constants.DEFAULT_PERIOD_LENGTH);
        dataBaseHelper.updatePeriodStats(d2, Constants.DEFAULT_PERIOD_LENGTH);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(d2);
        calendar.add(Calendar.DATE, -1 * Constants.DEFAULT_OVULATION_OFFSET);
        assertEquals(calendar.getTime(), dataBaseHelper.calculateOvulationStart(d2));
    }

    public void testUniqueKeyWithConflict() throws Exception {
        DailyStatus status1 = new DailyStatus
                (d, StatusEnum.StatusValue.BACKACHE);
        DailyStatus status2 = new DailyStatus
                (d, StatusEnum.StatusValue.CRAMPS);
        try {
            dataBaseHelper.updateStatus(status1);
            dataBaseHelper.updateStatus(status2);
        } catch (SQLException e) {
            fail("SqliteException" + e.getMessage());
        }
    }

    public void testUniqueKeyRetrieveItems() throws Exception {
        DailyStatus status1 = new DailyStatus
                (d, StatusEnum.StatusValue.BACKACHE);
        DailyStatus status2 = new DailyStatus
                (d, StatusEnum.StatusValue.CRAMPS);
        try {
            dataBaseHelper.updateStatus(status1);
            dataBaseHelper.updateStatus(status2);
        } catch (SQLException e) {
            fail("SqliteException" + e.getMessage());
        }
        ArrayList<DailyStatus> statuses = dataBaseHelper.getStatusToday(d);
        int actual = 0, expected = 2;
        for (DailyStatus s : statuses) {
            if (s.getType().equals(StatusEnum.StatusType.PAIN)) {
                actual++;
            }
        }
        assertEquals(expected, actual);
    }

    public void testUniqueKeyUpsert() throws Exception {
        DailyStatus status1 = new DailyStatus
                (d, StatusEnum.StatusValue.BACKACHE);
        DailyStatus status2 = new DailyStatus
                (d, StatusEnum.StatusValue.BACKACHE);
        try {
            dataBaseHelper.updateStatus(status1);
            dataBaseHelper.updateStatus(status2);
        } catch (SQLException e) {
            fail("SqliteException" + e.getMessage());
        }
        ArrayList<DailyStatus> statuses = dataBaseHelper.getStatusToday(d);
        int actual = 0, expected = 1;
        for (DailyStatus s : statuses) {
            if (s.getType().equals(StatusEnum.StatusType.PAIN)) {
                actual++;
            }
        }
        assertEquals(expected, actual);
    }

    public void testIsActivePeriodTrue() throws Exception {
        dataBaseHelper.updatePeriodStats(d, 4);
        assertTrue(dataBaseHelper.isActivePeriodDate(d));
    }

    public void testIsActivePeriodMiddleTrue() throws Exception {
        dataBaseHelper.updatePeriodStats(d, 4);
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        cal.add(Calendar.DATE, 2);
        assertTrue(dataBaseHelper.isActivePeriodDate(cal.getTime()));
    }

    public void testUpdateCycleErasingCheckCount() throws Exception {
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-01-28"), 4);

        Cursor c = db.rawQuery("SELECT * FROM PeriodStats ORDER BY StartDate DESC", null);
        assertEquals(c.getCount(), 4);
    }

    public void testUpdateCycleErasingCheckEndDate() throws Exception {
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-01-28"), 2);

        Cursor c = db.rawQuery("SELECT * FROM PeriodStats where StartDate = '2016-01-28'", null);
        if (c.moveToFirst()) {
            Date actual = dateFormat.parse(c.getString(c.getColumnIndex("EndDate")));
            assertEquals(dateFormat.parse("2016-01-30"), actual);
        } else {
            fail("Empty cursor when updated period expected");
        }
        c.close();
    }

    public void testUpdateCycleErasingCheckDate() throws Exception {
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-01-28"), 4);

        Date d = dataBaseHelper.getLastStartDate(dateFormat.parse("2016-02-05"));
        assertEquals(d, dateFormat.parse("2016-01-28"));
    }

    public void testUpdateCycleErasingCheckNoOldEntry() throws Exception {
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-01-28"), 4);
        Cursor c = db.rawQuery("SELECT * FROM PeriodStats WHERE StartDate = '2016-01-29'", null);
        assertFalse(c.moveToFirst());
        c.close();
    }

    public void testUpdateCycleOverrideAllDates() throws Exception {
        Cursor c = db.rawQuery("SELECT * FROM PeriodStats ORDER BY StartDate DESC", null);
        assertEquals(0, c.getCount());

        Date d1 = dateFormat.parse("2016-01-01");
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(d1, 300); // so long it will wipe out intermed dates
        c = db.rawQuery("SELECT * FROM PeriodStats ORDER BY StartDate DESC", null);
        assertEquals(1, c.getCount());
        c.close();
    }

    public void testUpdateCycleDontErase() throws Exception {
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-01-10"), 3);
        Cursor c = db.rawQuery("SELECT * FROM PeriodStats ORDER BY StartDate DESC", null);
        assertEquals(c.getCount(), 5);
        c.close();
    }

    public void testUpdateCycleDontEraseCloseDates() throws Exception {
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-02-03"), 3);
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-03-02"), 3);
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2015-12-28"), 3);
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-03-21"), 3);
        Cursor c = db.rawQuery("SELECT * FROM PeriodStats ORDER BY StartDate DESC", null);
        assertEquals(8, c.getCount());
        c.close();
    }

    public void testUpdateCycleStartEndOverlap() throws Exception {
        buildPeriodStats();
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-03-21"), 4); // overwrite old cycle that started then
        Cursor c = db.rawQuery("SELECT * FROM PeriodStats where StartDate = '2016-03-25'", null);
        assertFalse(c.moveToFirst());
        c.close();
    }

    private void buildPeriodStats() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        Date d2 = dateFormat.parse("2016-01-29");
        Date d3 = dateFormat.parse("2016-02-26");
        Date d4 = dateFormat.parse("2016-03-25");
        Date[] mDates = new Date[]{d1, d2, d3, d4};

        for (Date d : mDates) {
            dataBaseHelper.updatePeriodStats(d, 4);
        }
    }

    private void setStatusEntries() throws Exception {
        Date d1 = dateFormat.parse("2016-01-01");
        Date d2 = dateFormat.parse("2016-01-29");
        DailyStatus[] mEntries = new DailyStatus[]{
                new DailyStatus(d, StatusEnum.StatusValue.BACKACHE),
                new DailyStatus(d, StatusEnum.StatusValue.ABNORMAL),
                new DailyStatus(d, StatusEnum.StatusValue.BREASTS),
                new DailyStatus(d, StatusEnum.StatusValue.YES_EXERCISE),
                new DailyStatus(d1, StatusEnum.StatusValue.YES_EXERCISE),
                new DailyStatus(d1, StatusEnum.StatusValue.BREASTS),
                new DailyStatus(d2, StatusEnum.StatusValue.BREASTS),
                new DailyStatus(d, StatusEnum.StatusValue.BREASTS) // shouldn't add to count; duplicate
        };
        for (DailyStatus status : mEntries) {
            dataBaseHelper.updateStatus(status);
        }
    }

    public void testGetStatusValueSummarySingleEntry() throws Exception {
        setStatusEntries();
        HashMap<StatusEnum.StatusValue, Integer> mresult =
                dataBaseHelper.getStatusValueSummary(StatusEnum.StatusType.FLUIDS);
        assertTrue(mresult.get(StatusEnum.StatusValue.ABNORMAL).equals(1));
    }

    public void testGetStatusValueSummaryMultiEntry() throws Exception {
        setStatusEntries();
        HashMap<StatusEnum.StatusValue, Integer> mresult =
                dataBaseHelper.getStatusValueSummary(StatusEnum.StatusType.PAIN);
        assertTrue(mresult.get(StatusEnum.StatusValue.BREASTS).equals(3));
        assertTrue(mresult.get(StatusEnum.StatusValue.BACKACHE).equals(1));
    }

    public void testGetStatusValueSummaryNoEntry() throws Exception {
        setStatusEntries();
        HashMap<StatusEnum.StatusValue, Integer> mresult =
                dataBaseHelper.getStatusValueSummary(StatusEnum.StatusType.FLUIDS);
        assertTrue(mresult.get(StatusEnum.StatusValue.CREAMY).equals(0));
    }

    // test if even categories with zero responses are added to map--they should be.
    public void testGetStatusValueGetAllCategories() throws Exception {
        setStatusEntries();
        HashMap<StatusEnum.StatusValue, Integer> mresult =
                dataBaseHelper.getStatusValueSummary(StatusEnum.StatusType.FLUIDS);
        assertTrue(mresult.size() == 4);

        HashMap<StatusEnum.StatusValue, Integer> mresult2 =
                dataBaseHelper.getStatusValueSummary(StatusEnum.StatusType.SEX);
        assertTrue(mresult2.size() == 2);
    }

    public void testGetStatusHistoryEmpty() throws Exception {
        HashMap<StatusEnum.StatusValue, Integer> mresult =
                dataBaseHelper.getStatusValueSummary(StatusEnum.StatusType.FLUIDS);

        // returns a map of 4 options with zero results each
        for (StatusEnum.StatusValue v : mresult.keySet()) {
            assertEquals(0, (int) mresult.get(v));
        }
    }

    public void testGetStatusHistoryMulti() throws Exception {
        setStatusEntries();
        HashMap<StatusEnum.StatusType, Integer> mResult = dataBaseHelper.getStatusHistory();
        assertTrue(mResult.get(StatusEnum.StatusType.PAIN).equals(4));
    }

    public void testGetStatusHistoryNull() throws Exception {
        setStatusEntries();
        HashMap<StatusEnum.StatusType, Integer> mResult = dataBaseHelper.getStatusHistory();
        assertEquals(0, (int) mResult.get(StatusEnum.StatusType.MOOD));
    }

    public void testGetStatusHistoryGetAllEntries() throws Exception {
        setStatusEntries();
        HashMap<StatusEnum.StatusType, Integer> mResult = dataBaseHelper.getStatusHistory();
        int activeCategories = 0;
        for (StatusEnum.StatusType t : mResult.keySet()) { // 7 elements in keyset, including inactive categories
            if (mResult.get(t) > 0) {
                activeCategories++;
            }
        }
        assertEquals(3, activeCategories);
    }

    private void setPeriodStats() throws Exception {
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-01-01"), 8);
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-01-29"), 6);
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-02-26"), 2);
        dataBaseHelper.updatePeriodStats(dateFormat.parse("2016-03-25"), 4);
    }


    public void testGetPeriodLengths() throws Exception {
        setPeriodStats();
        HashMap<Date, Integer> lengths = dataBaseHelper.getPeriodLengths();
        assertEquals(4, lengths.size());
    }


    public void testGetPeriodLengthsCheckValue() throws Exception {
        setPeriodStats();
        HashMap<Date, Integer> lengths = dataBaseHelper.getPeriodLengths();
        assertTrue(lengths.get(dateFormat.parse("2016-01-29")) == 6);
    }


    public void testGetPeriodLengthsEmpty() throws Exception {
        HashMap<Date, Integer> lengths = dataBaseHelper.getPeriodLengths();
        assertEquals(0, lengths.size());
    }

    // if no records found, use default
    public void testGetAveragePeriodLengthEmpty() throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
                .putString(Constants.PERIOD_LENGTH, "3")
                .apply();
        assertEquals(Integer.parseInt(prefs.getString(Constants.PERIOD_LENGTH,
                Integer.toString(Constants.DEFAULT_PERIOD_LENGTH))),
                dataBaseHelper.getAveragePeriodLength());
    }

    public void testGetAveragePeriodLength() throws Exception {
        setPeriodStats();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
                .putString(Constants.PERIOD_LENGTH, "5")
                .apply();
        assertTrue(dataBaseHelper.getAveragePeriodLength() == Math.round(((8 + 6 + 4 + 2) + 5) / 5));
    }

}
