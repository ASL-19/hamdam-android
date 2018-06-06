package com.hamdam.hamdam.presenter;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.hamdam.hamdam.enums.StatusEnum;
import com.hamdam.hamdam.model.DailyStatus;
import com.hamdam.hamdam.util.DateUtil;
import com.hamdam.hamdam.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Helper class containing database methods,
 * creation, CRUD and managing access to SQLite database.
 */
public class DatabaseHelperImpl extends SQLiteOpenHelper implements PresenterContracts.DatabasePresenter {
    private static final String TAG = "DatabaseHelper";

    private static DatabaseHelperImpl mInstance = null;
    private SQLiteDatabase db;
    private Context mContext;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private static final int SCHEMA = 1;
    private static final String DATABASE_NAME = "periodcalendar.db";

    // Load default period length and other stats specified by user via SharedPreferences
    private SharedPreferences mSharedPreferences;

    private final class TableConstants {
        // Table names
        static final String DAILY_INFO = "DailyInfo";
        static final String PERIOD_STATS = "PeriodStats";

        // Column names - General
        static final String START_DATE = "StartDate";
        static final String END_DATE = "EndDate";

        // Column names - Daily Status
        static final String TYPE = "StatusType";
        static final String VALUE = "StatusValue";

        // Column names - Period Stats
        static final String PMS_LENGTH = "PMSLength";

        // Create statements
        // Daily Info -- the bulk of the user data entered here. Any time user fills out daily question.
        static final String CREATE_DAILY = "CREATE TABLE IF NOT EXISTS " + DAILY_INFO + "(" +
                START_DATE
                + " DATETIME NOT NULL," + TYPE + " TEXT NOT NULL," + VALUE + " TEXT NOT NULL,"
                + "PRIMARY KEY (" + START_DATE + ", " + VALUE + ")" + ")";

        // Stats on past period cycle length/duration of menstruation and PMS
        static final String CREATE_PSTATS = "CREATE TABLE IF NOT EXISTS " + PERIOD_STATS + "(" +
                START_DATE + " DATETIME NOT NULL," + END_DATE + " DATETIME NOT NULL,"
                + PMS_LENGTH + " INTEGER," + "PRIMARY KEY (" + START_DATE + ")" + ")";
    }

    /*
     * getInstance of singleton DatabaseHelperImpl
     */
    public static synchronized DatabaseHelperImpl getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelperImpl(context.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelperImpl(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
        this.mContext = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();

        try {

            // Create tables
            db.execSQL(TableConstants.CREATE_DAILY);
            db.execSQL(TableConstants.CREATE_PSTATS);
            insertDefaultValues(db, new ContentValues());
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d(TAG, "Upgrading database version. Old data will be removed.");
        db.execSQL("DROP TABLE IF EXISTS " + TableConstants.DAILY_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TableConstants.PERIOD_STATS);

        onCreate(db);
    }

    @Override
    public void clearUserHistory() {
        db.delete(TableConstants.DAILY_INFO, null, null);
        db.delete(TableConstants.PERIOD_STATS, null, null);
    }

    /*
     * Called during onCreate only. Occurs inside an already open transaction.
     * Insert values from shared preferences into period stats chart.
     */
    private void insertDefaultValues(SQLiteDatabase db, ContentValues cv) {
        db.beginTransaction();

        try {

            // Load default period data from shared preferences.
            String defaultStart = mSharedPreferences.getString
                    (mContext.getString(com.hamdam.hamdam.R.string.last_period_date_key),
                            null);
            int defaultLength = Integer.parseInt(mSharedPreferences.getString
                    (Constants.PERIOD_LENGTH,
                            Integer.toString(Constants.DEFAULT_PERIOD_LENGTH)));

            if (defaultStart != null && !TextUtils.isEmpty(defaultStart)) {
                // Create and insert an end date based on this start date.
                Date start = dateFormat.parse(defaultStart);
                Date end = generateEndDate(start, defaultLength);

                cv.put(TableConstants.START_DATE, defaultStart);
                cv.put(TableConstants.END_DATE, dateFormat.format(end));
                db.insert(TableConstants.PERIOD_STATS, null, cv);
                db.setTransactionSuccessful();
            } else {
                Log.e(TAG, "Date was null");
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    /*
     * Return boolean representing whether status type supports multiple
     * responses for the same day.
     */
    public boolean allowMultipleResponse(DailyStatus status) {
        return StatusEnum.isMultiAnswer(status.getType());
    }

    // Update or Insert daily status (mood, pain, sleep etc).
    @Override
    public boolean updateStatus(@NonNull DailyStatus status) {
        boolean success = false;
        db = this.getWritableDatabase();
        db.beginTransaction();
        long result;

        try {

            ContentValues cv = new ContentValues();
            cv.put(TableConstants.START_DATE, dateFormat.format
                    (status.getGregorianDate()));
            cv.put(TableConstants.TYPE, status.getStatusValue().getStatusType().name());
            cv.put(TableConstants.VALUE, status.getStatusValue().name());

            // Check for types that do not allow multiple entries one type on a given date,
            // and overwrite any such records
            if (!allowMultipleResponse(status)) {
                db.delete(TableConstants.DAILY_INFO,
                        TableConstants.START_DATE + "=? and "
                                + TableConstants.TYPE + "=?",
                        new String[]{dateFormat.format(status.getGregorianDate()),
                                status.getStatusValue().getStatusType().name()});
            }

            result = db.insertWithOnConflict(TableConstants.DAILY_INFO,
                    TableConstants.START_DATE, cv, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
            success = true;
        } catch (SQLException ex) {
            Log.e(TAG, "Add/update error: " + ex.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }

    @Override
    public boolean isActivePeriodDate(Date testDate) {
        db = this.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = null;
        boolean conflict = true;

        try {
            cursor = db.query(TableConstants.PERIOD_STATS, new String[]{TableConstants.START_DATE,
                            TableConstants.END_DATE}, TableConstants.START_DATE
                            + "<=? and " + TableConstants.END_DATE + ">=?",
                    new String[]{dateFormat.format(testDate),
                            dateFormat.format(testDate)}, null, null, null);

            if (!cursor.moveToFirst()) { // There are no periods within specified range.
                conflict = false;
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
        }
        return conflict;
    }

    // Generate a Date that is duration days after start date.
    @Override
    @NonNull
    public Date generateEndDate(@NonNull Date startDate, @NonNull Integer duration) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, duration - 1); // Start with day 1

        return cal.getTime();
    }

    /*
     * Update the menstruation calendar by adding a new period to the PeriodStats table.
     * (See overloaded constructor below)
     */
    @Override
    public boolean updatePeriodStats(@NonNull Date startDate, @NonNull Integer duration) {
        Date endDate = generateEndDate(startDate, duration);
        int pmsLength = Integer.parseInt(mSharedPreferences.getString
                (Constants.PMS_LENGTH, Integer.toString
                        (Constants.DEFAULT_PMS_LENGTH)));
        return updatePeriodStats(startDate, endDate, pmsLength);
    }

    /*
     * Update the menstruation calendar by adding a new period to PeriodStats table.
     *
     * If user has no data entered for the timeframe, inserts new record.
     *
     * Overlapping records (where start or end date overlap a subsequent record) are not allowed.
     * If user is adding data which partially overlaps existing cycles or spans multiple
     * cycles, deletes those old records and replaces them with new record.
     *
     * @param   startDate   Date that period starts
     * @param   endDate     Date that period ends; if not specified by user, generated based on
     *                      their specified preferences at onboarding.
     * @oaram   pmsLength   length of PMS; if not specified by user, generated based on
     *                      their specified preferences at onboarding. (Currently specifying PMS
     *                      per cycle is not an available UI feature and occurs at onboarding
     *                      or via user settings).
     *
     * @returns true if record successfully inserted
     */
    @Override
    public boolean updatePeriodStats(@NonNull Date startDate, @Nullable Date endDate,
                                     @Nullable Integer pmsLength) {

        boolean success = false;
        if (endDate == null) {
            int duration = Integer.parseInt
                    (mSharedPreferences.getString(mContext.getResources()
                                    .getString(com.hamdam.hamdam.R.string.period_length_key),
                            Integer.toString(Constants.DEFAULT_PERIOD_LENGTH)));
            endDate = generateEndDate(startDate, duration);
        }

        if (pmsLength == null) {
            pmsLength = Integer.parseInt(mSharedPreferences.getString
                    (Constants.PMS_LENGTH, Integer.toString(Constants.DEFAULT_PMS_LENGTH)));
        }

        db = this.getWritableDatabase();
        db.beginTransaction();

        ContentValues cv = new ContentValues();
        cv.put(TableConstants.START_DATE, dateFormat.format(startDate));
        cv.put(TableConstants.END_DATE, dateFormat.format(endDate));
        cv.put(TableConstants.PMS_LENGTH, pmsLength);

        // Check for non-overlapping dates.
        // Entering a conflicting end date deletes and overwrites previous entry.
        try {
            Set<Date> insideRecords = getRecordsBetween(startDate, endDate).keySet();
            if (!insideRecords.isEmpty()) {
                for (Date d : insideRecords) {
                    boolean s = deletePeriod(d); // Delete by start date.
                }
            }

            // Not spanning any other records -- insert.
            long result = db.insertWithOnConflict(TableConstants.PERIOD_STATS,
                    TableConstants.START_DATE,
                    cv, SQLiteDatabase.CONFLICT_REPLACE);
            if (result < 0) { // either an error or a conflict.

            }

            db.setTransactionSuccessful();
            success = true;
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }


    @Override
    public boolean deleteDailyStatus(DailyStatus status) {
        boolean success = false;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            String day = dateFormat.format(status.getGregorianDate());
            int resultInt = db.delete(TableConstants.DAILY_INFO, TableConstants.START_DATE
                    + "=? and "
                    + TableConstants.TYPE + "=?", new String[]{day, status.getType().name()});
            if (resultInt > 0) {
                db.setTransactionSuccessful();
                success = true;
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }

    /*
     * Delete any records that occur over date targetDate.
     * @param   targetDate  Date to search matching records
     * @returns true if found a record to delete.
     */
    @Override
    public boolean deletePeriod(Date targetDate) {
        boolean success = false;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            String day = dateFormat.format(targetDate);
            int resultInt = db.delete(TableConstants.PERIOD_STATS, TableConstants.START_DATE
                            + "<=? and " + TableConstants.END_DATE + " >=?",
                    new String[]{day, day});
            if (resultInt > 0) {
                success = true;
                db.setTransactionSuccessful();
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }

    // Returns all status objects associated with current day.
    @Override
    @NonNull
    public ArrayList<DailyStatus> getStatusToday(@NonNull Date d) {
        ArrayList<DailyStatus> statusToday = new ArrayList<>();
        Cursor cursor = null;

        db = this.getReadableDatabase();
        db.beginTransaction();

        try { // equivalent SQL: "select from DailyInfo where StartDate = d"
            cursor = db.query(TableConstants.DAILY_INFO, null,
                    TableConstants.START_DATE + "=?",
                    new String[]{dateFormat.format(d)}, null, null, null);

            while (cursor.moveToNext()) {
                StatusEnum.StatusValue value = StatusEnum.StatusValue.valueOf
                        (cursor.getString(cursor.getColumnIndex(TableConstants.VALUE)));
                statusToday.add(new DailyStatus(d, value));
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
        }
        return statusToday;
    }


    // Return the average length of the user's cycles given a list of past values
    @Override
    public int getAverageCycleLength() {
        Collection<Integer> lengths = getCycleLengths().values();
        Float avg = (float) Integer.parseInt(mSharedPreferences.getString
                (mContext.getString(com.hamdam.hamdam.R.string.cycle_length_key),
                        Integer.toString(Constants.DEFAULT_CYCLE_LENGTH)));

        if (!lengths.isEmpty()) {
            for (float f : lengths) {
                avg += f;
            }
            avg /= lengths.size() + 1; // Average with their default length included.
        }

        return Math.round(avg);
    }

    /*
     * Gets a map of start dates and their associated cycle lengths.
     * This can be used to graph period history, as well as calculated averages and
     * predict future cycle length(s).
     * Returns a Hashmap of dates to integers, where the date represents the start date of
     * a menstrual cycle, and the integer represents the number of days in that cycle.
     * Does not return projected dates, only existing records.
     *
     * Note: A cycle length can only be determined once the next cycle has started.
     * Therefore, if a user inputs dates (for example) Jan 1, Jan 29, Feb 26, March 25, the
     * most recent key will be Feb 26, with a value of 28.
     */
    @Override
    @NonNull
    public HashMap<Date, Integer> getCycleLengths() {
        db = this.getReadableDatabase();
        HashMap<Date, Integer> lengths = new HashMap<>();
        Cursor c = null;

        db.beginTransaction();

        try {
            // Step 1: Get most recent previous period start
            c = db.query(TableConstants.PERIOD_STATS, new String[]{TableConstants.START_DATE},
                    null, null, null, null, TableConstants.START_DATE + " DESC");

            if (c.moveToFirst()) { // There is a period date record.
                Date mostRecent = dateFormat.parse
                        (c.getString(c.getColumnIndex(TableConstants.START_DATE)));

                while (c.moveToNext()) { // There are records of past periods for date comparison.
                    Date previous = dateFormat.parse
                            (c.getString(c.getColumnIndex(TableConstants.START_DATE)));

                    // A cycle has length 1 even if start and end are 0 days apart.
                    int daysApart = DateUtil.getDaysApart(mostRecent, previous) + 1;
                    lengths.put(previous, daysApart);

                    mostRecent = previous;
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }
        return lengths;
    }


    // Get date of last period to occur before maxDate.
    // Returns null if no previous records are found in the database.
    @Override
    @Nullable
    public Date getLastStartDate(@NonNull Date maxDate) {

        db = this.getReadableDatabase();
        Date mostRecent = null; // get a start date (even estimation) at onboarding
        Cursor c = null;

        db.beginTransaction();

        try {
            // Step 1: Get most recent previous period start/
            // "select (StartDate) from PeriodStats where StartDate <= <max date> order by desc"
            c = db.query(TableConstants.PERIOD_STATS, new String[]{TableConstants.START_DATE},
                    TableConstants.START_DATE + "<=?", new String[]{dateFormat.format(maxDate)},
                    null, null, TableConstants.START_DATE + " DESC");

            if (c.moveToFirst()) { // There is a period date record.
                mostRecent = dateFormat.parse
                        (c.getString(c.getColumnIndex(TableConstants.START_DATE)));
            }

            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }

        return mostRecent;
    }

    /*
     * Get all records between given dates, returning a Map<Date start, Integer duration>
     * representing all matching records.
     * Includes records partially within specified range (for example, starting within
     * range but ending after date range or vice-versa).
     *
     * @returns HashMap<Date, Integer> representing corresponding start dates
     * of period records and their duration in days.
     * An empty map is returned if no records are found.
     *
     */
    @Override
    @NonNull
    public Map<Date, Integer> getRecordsBetween(@NonNull Date start, @NonNull Date stop) {

        db = this.getReadableDatabase();
        Map<Date, Integer> records = new HashMap<>();
        Date startDate, endDate;
        Cursor c = null;
        String startDateString = dateFormat.format(start);
        String stopDateString = dateFormat.format(stop);

        db.beginTransaction();

        // Select any records that span this range.
        // 3 cases: Date1 <= StartDate <= Date2; Date1 <= StopDate <= Date2,
        // Date1 <= StartDate and StopDate <= Date2
        try {
            c = db.query(TableConstants.PERIOD_STATS, new String[]{TableConstants.START_DATE,
                            TableConstants.END_DATE},
                    TableConstants.START_DATE + "<=? AND " +
                            TableConstants.END_DATE + ">=?",
                    new String[]{stopDateString, startDateString},
                    null, null, TableConstants.START_DATE + " ASC");

            while (c.moveToNext()) { // While there are matching records

                startDate = dateFormat.parse
                        (c.getString(c.getColumnIndex(TableConstants.START_DATE)));
                endDate = dateFormat.parse
                        (c.getString(c.getColumnIndex(TableConstants.END_DATE)));
                int duration = DateUtil.getDaysApart(endDate, startDate) + 1; // duration is 1 more than daysApart

                // Add start and duration to map
                records.put(startDate, duration);
            }
            db.setTransactionSuccessful();

        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());

        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());

        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }
        return records;
    }

    /*
     * Returns int representing user's position in their menstruation cycle (e.g., Day 1, Day 12).
     * Value will always be greater than or equal to zero.
     * If a user's period is late, this value will be greater than their average cycle length
     * (e.g., a value of 30).
     * To calculate the number of days a user is late by, daysTillStartDate() is used, and will return
     * negative values if a user is later than average.
     */
    @Override
    public int calculateCyclePosition(@NonNull Date current) {
        Date previousStartDate = getLastStartDate(current);
        if (previousStartDate == null) { // User has cleared all data or encountered problem onboarding
            Log.w(TAG, "No cycle data available");
            return Constants.OUT_OF_RANGE;
        } else if (DateUtil.clearTimeStamp(previousStartDate)
                == DateUtil.clearTimeStamp(new Date())) {
        }
        int position = DateUtil.getDaysApart(current, previousStartDate); // don't add 1
        return position;
    }

    /*
     * Returns the record immediately after Date earlyDate, or null if no subsequent records.
     * @param   earlyDate   Date after which to return first available period record.
     */
    @Nullable
    public Date getNextPeriodRecord(@NonNull Date earlyDate) {

        db = this.getReadableDatabase();
        Date nextRecordDate = null;
        Cursor c = null;

        db.beginTransaction();

        try {
            // Step 1: Get most recent previous period start/
            // "select (StartDate) from PeriodStats where StartDate <= <max date as a string> order by desc"
            c = db.query(TableConstants.PERIOD_STATS, new String[]{TableConstants.START_DATE},
                    TableConstants.START_DATE + ">?", new String[]{dateFormat.format(earlyDate)},
                    null, null, TableConstants.START_DATE + " ASC");

            if (c.moveToFirst()) { // There is at least one period date record after target date.
                nextRecordDate = dateFormat.parse
                        (c.getString(c.getColumnIndex(TableConstants.START_DATE)));
            }

            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }

        return nextRecordDate;
    }

    /*
     * Returns anticipated Date of next period start.
     * Note that if period is late (anticipated date is past), this will still return a past date.
     * This will be used to fill the calendar UI.
     */
    @Override
    @Nullable
    public Date projectNextStartDate(@NonNull Date current) {

        // Check if there are records after current date.
        // If so, return those; if not, make projection.
        Date maybeExistingRecord = getNextPeriodRecord(current);
        if (maybeExistingRecord != null) {
            return maybeExistingRecord;
        } else {
            Calendar cal = new GregorianCalendar();
            cal.setTime(current);
            int daysTillNewCycle = daysTillStartDate(current);
            if (daysTillNewCycle == Constants.OUT_OF_RANGE) { // No previous data
                return null;
            }

            // If requesting prediction on day of most recent cycle, advance prediction ahead by a cycle.
            // If user is using smart prediction mode (default), predict via average cycle length;
            // if user is using regular mode, use the value they specified in shared prefs
            if (daysTillNewCycle == Constants.FLAG_TODAY) { // found existing record
                daysTillNewCycle = getCycleIncrement();
            }
            if (daysTillNewCycle >= Constants.DEFAULT_LONG_CYCLE_LENGTH) {
                //This means their next predicted cycle is more than 45 days away.
            }
            cal.add(Calendar.DATE, daysTillNewCycle);
            return cal.getTime();
        }
    }

    /*
     * Return a map of dates within a specified range corresponding to projected periods and their
     * lengths. If an event falls only partially within the date range, the length will reflect
     * only the part falling within the date range.
     */
    @NonNull
    public Map<Date, Integer> projectRecordsBetween(@NonNull Date startDate,
                                                    @NonNull Date endDate, boolean isPeriod) {
        Map<Date, Integer> projections = new HashMap<>();
        Date projectedStart, tempDate, date = DateUtil.clearTimeStamp(new Date());
        int defaultLength = Integer.parseInt(mSharedPreferences.getString
                (mContext.getString(com.hamdam.hamdam.R.string.period_length_key),
                        Integer.toString(Constants.DEFAULT_PERIOD_LENGTH)));
        if (date != null) {
            projectedStart = projectNextStartDate(date);

            // If ovulation, start predicting OFFSET number of days before period,
            // and set length of cycles to ovulation length
            if (!isPeriod) {
                projectedStart = DateUtil.rollCalendar(projectedStart,
                        -1 * Constants.DEFAULT_OVULATION_OFFSET);
                defaultLength = Constants.DEFAULT_OVULATION_LENGTH;
            }

            if (projectedStart != null) {
                tempDate = DateUtil.clearTimeStamp(projectedStart);

                // As long as have not passed last displayed date
                while (tempDate != null && tempDate.getTime() <= endDate.getTime()) {

                    // Check case where projection starts before current date but might end within range.
                    // In that case, add the remaining part of the period to the database.
                    if (tempDate.getTime() < startDate.getTime()) {
                        Date projectedEndDate = DateUtil.rollCalendar(projectedStart, defaultLength);
                        if (projectedEndDate != null && projectedEndDate.getTime() >= startDate.getTime()) {
                            int duration = DateUtil.getDaysApart(projectedEndDate, startDate);
                            projections.put(DateUtil.rollCalendar(projectedEndDate, -1 * duration), duration);
                        }
                    }
                    // If after start date and before end date, add projection to map
                    if (tempDate.getTime() >= startDate.getTime()) {
                        projections.put(tempDate, defaultLength);
                    }

                    // Roll calendar forward by appropriate increment and check dates again
                    int increment = getCycleIncrement();
                    if (increment > 0) {
                        tempDate = DateUtil.rollCalendar(tempDate, increment);

                    } else { // Should not occur--means invalid entry in shared preferences.
                        Log.e(TAG,
                                "onProjectionEvent recovered invalid Shared Preference: "
                                        + "cycle length " + increment);
                        break;
                    }
                }
            }
        }
        return projections;
    }

    /*
     * Returns an int representing how many days until next period starts (projected).
     * If int is negative, means period is late.
     * If int is zero, means period is today.
     */
    @Override
    public int daysTillStartDate(Date currentDate) {

        // look for last record, get difference from current.
        int position = calculateCyclePosition(currentDate);

        // Cycle position returned an error
        if (position < 0) {
            return Constants.OUT_OF_RANGE;

            // GetLastStartDate returned today's date (there is an existing record for this day)
        } else if (position == 0) {
            return Constants.FLAG_TODAY;

            // Somewhere else in the cycle.
            // Either return average cycle length, or the stored user in settings if SmartPredict is off.
        } else {
            return getCycleIncrement() - position;
        }
    }

    /*
     * Calculate next ovulation date relative to {@param Date} target date.
     * This is an estimation only, and can be inaccurate; therefore, OFFSET takes
     * into account the need to show a window (range) of possible ovulation dates
     * (the estimation range in this case is 6 days, although it is
     * possible to create a more accurate estimate using more sophisticated data-
     * gathering).
     * @returns Date representing beginning of next ovulation window,
     * or null if no values to predict from.
     */
    @Override
    @Nullable
    public Date calculateOvulationStart(Date currentDate) {
        Date lastStart = getLastStartDate(currentDate);
        if (lastStart == null) {
            Log.e(TAG,
                    "No dates to project from--was there a problem onboarding?");
            return null;
        }
        // Ovulation is generally about 10-16 days before your next anticipated period.
        // Return beginning of estimated window for ovulation, reflected in UI.
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(lastStart);
        calendar.add(Calendar.DATE, -1 * Constants.DEFAULT_OVULATION_OFFSET);
        return calendar.getTime();

    }

    /*
     * Get the length of menstruation cycles used for predictions of future cycle
     * start dates. Either this is * defined as the average cycle length,
     * calculated dynamically based on all the user's data (SmartPredict on),
     * or is simply the value for Cycle Length that the user has stored
     * in User Settings (SmartPredict off).
     */
    @Override
    public int getCycleIncrement() {
        boolean usePredictionMode = mSharedPreferences.getBoolean
                (mContext.getString(com.hamdam.hamdam.R.string.enable_prediction_mode_key), true);
        if (usePredictionMode) {
            return getAverageCycleLength();
        } else {
            return Integer.parseInt(mSharedPreferences.getString
                    (mContext.getString(com.hamdam.hamdam.R.string.cycle_length_key),
                            Integer.toString(Constants.DEFAULT_CYCLE_LENGTH)));
        }
    }

    /*
     * Return a list of dates corresponding to the beginning of the ovulation window
     * for each cycle record found in the database.
     * For each cycle, the actual (estimated) ovulation date occurs
     * (DEFAULT_OVULATION_LENGTH / 2)  days after the date in this list, because the
     * list indicates a range of dates with the lower boundary being the list entry.
     */
    @Override
    public List<Date> getPastOvulationDates() {
        HashMap<Date, Integer> periods = getPeriodLengths();
        List<Date> ovulationStartDates = new ArrayList<>();

        Calendar calendar = new GregorianCalendar();
        for (Date d : periods.keySet()) {
            calendar.setTime(d);
            calendar.add(Calendar.DATE, -1 * Constants.DEFAULT_OVULATION_OFFSET);
            ovulationStartDates.add(calendar.getTime());
        }
        return ovulationStartDates;
    }

    /*
     * Calculate average period length based on data in user's PeriodStats table.
     */
    @Override
    public int getAveragePeriodLength() {
        Collection<Integer> lengths = getPeriodLengths().values();
        Float avg = (float) Integer.parseInt(mSharedPreferences.getString
                (mContext.getString(com.hamdam.hamdam.R.string.period_length_key),
                        Integer.toString(Constants.DEFAULT_PERIOD_LENGTH)));

        if (!lengths.isEmpty()) {
            for (float f : lengths) {
                avg += f;
            }
            avg /= lengths.size() + 1; // Average with their default length included.
        }
        return Math.round(avg);
    }


    /*
     * Return past period start dates of periods mapped to period lengths.
     */
    @Override
    @NonNull
    public HashMap<Date, Integer> getPeriodLengths() {
        db = this.getReadableDatabase();
        HashMap<Date, Integer> lengths = new HashMap<>();
        Cursor c = null;

        db.beginTransaction();

        try {
            c = db.query(TableConstants.PERIOD_STATS, new String[]{TableConstants.START_DATE,
                            TableConstants.END_DATE},
                    null, null, null, null,
                    TableConstants.START_DATE + " DESC");

            while (c.moveToNext()) { // While there are records
                Date start = dateFormat.parse
                        (c.getString(c.getColumnIndex(TableConstants.START_DATE)));

                Date end = dateFormat.parse
                        (c.getString(c.getColumnIndex(TableConstants.END_DATE)));

                int daysApart = DateUtil.getDaysApart(end, start) + 1; // 0 days apart has length 1
                lengths.put(start, daysApart);
            }

            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException ex) { // two catch blocks req'd
            Log.e(TAG, ex.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }
        return lengths;
    }

    /*
     * Return map of frequency of user data on each DailyStatus subject.
     */
    @Override
    @NonNull
    public HashMap<StatusEnum.StatusType, Integer> getStatusHistory() {
        HashMap<StatusEnum.StatusType, Integer> result = new HashMap<>();

        Cursor c = null;
        db = this.getReadableDatabase();
        db.beginTransaction();

        try {
            for (StatusEnum.StatusType t : StatusEnum.StatusType.values()) {
                c = db.query(TableConstants.DAILY_INFO, new String[]{TableConstants.START_DATE,
                                TableConstants.TYPE}, TableConstants.TYPE + "=?",
                        new String[]{t.name()}, null, null, null);
                result.put(t, c.getCount());
            }

            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    /*
     * Get summary of the frequency of entries in each status type category.
     * This is used to display data summaries in the graph.
     * @param type  Target StatusType (such as Mood, Pain, etc)
     *
     * @returns     HashMap<StatusValue, Integer> representing a grouping of values and their frequency,
      *             all of which are part of the same type of data.
      *             (For example, a query on statusType Pain could yield the following map:
      *             {(Pain.HEADACHE, 1), (Pain.BACKACHE, 4)}
     */
    @Override
    @NonNull
    public HashMap<StatusEnum.StatusValue, Integer> getStatusValueSummary(StatusEnum.StatusType type) {
        HashMap<StatusEnum.StatusValue, Integer> result = new HashMap<>();
        final String COUNT = "Count";

        Cursor c = null;
        db = this.getReadableDatabase();
        db.beginTransaction();

        try {

            // "Select StartDate, Value, Count(Value) as COUNT from DailyInfo where Type = t group by Value"
            c = db.query(TableConstants.DAILY_INFO, new String[]{TableConstants.START_DATE,
                            TableConstants.VALUE, "COUNT(*) as " + COUNT},
                    TableConstants.TYPE + "=?", new String[]{type.name()}, TableConstants.VALUE, null, null);

            while (c.moveToNext()) {
                result.put(StatusEnum.StatusValue.valueOf
                                (c.getString(c.getColumnIndex(TableConstants.VALUE))),
                        c.getInt(c.getColumnIndex(COUNT)));
            }

            // Include members of the same category with zero values--they are relevant for graph
            for (StatusEnum.StatusValue v : StatusEnum.StatusValue.values()) {
                if (v.getStatusType().equals(type) && !result.containsKey(v)) {
                    result.put(v, 0);
                }
            }

            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

}
