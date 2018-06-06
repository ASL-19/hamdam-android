package com.hamdam.hamdam.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.test.AndroidTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.hamdam.hamdam.util.AlarmUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.hamdam.hamdam.Constants.BROADCAST_REBOOT_ALARM;
import static com.hamdam.hamdam.Constants.BROADCAST_START_ALARM;

/**
 * Test broadcast receiver receiving date alarms, check reboot.
 * Thanks to Matt Thompson and the GeoPing project
 * (https://github.com/gabuzomeu/geoPingProject/) for their test design.
 */
public class CustomBroadcastReceiverTest extends AndroidTestCase {
    private CustomBroadcastReceiver mReceiver, mStubReceiver;
    private TestContext mContext;
    private final String CODE_ID = "CodeId"; // tag
    private List<Intent> mReceivedIntents;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mReceivedIntents = new ArrayList<>();
        mReceiver = new CustomBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);

                mReceivedIntents.add(intent);
            }
        };

        mStubReceiver = new CustomBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                mReceivedIntents.add(intent);
            }
        };
        mContext = new TestContext(InstrumentationRegistry.getTargetContext());
    }

    @Override
    public void tearDown() throws Exception {
        mReceivedIntents = null;
        mContext = null;
        super.tearDown();
    }

    private void setUpReceiver(String action, int id) {

        // PendingIntent that will perform alarm broadcast.
        Intent alarmIntent = new Intent(mContext, CustomBroadcastReceiver.class);
        alarmIntent.putExtra(CODE_ID, id);
        alarmIntent.setAction(action);

        IntentFilter filter = new IntentFilter(action);
        mContext.registerReceiver(mStubReceiver, filter);
        mStubReceiver.onReceive(mContext, alarmIntent);
    }

    @SmallTest
    public void testReceiveAlarmIntent() {
        final String BROADCAST = "BroadcastStartAlarm";
        setUpReceiver(BROADCAST, AlarmUtil.RequestCodes.PERIOD_ALARM);

        assertEquals(1, mReceivedIntents.size());
        assertNull(mStubReceiver.getResultData());

        Intent receivedIntent = mReceivedIntents.get(0);
        assertEquals(receivedIntent.getAction(), BROADCAST);
        assertEquals(AlarmUtil.RequestCodes.PERIOD_ALARM,
                receivedIntent.getIntExtra(CODE_ID, -1));
    }

    @SmallTest @UiThreadTest
    public void testReceiveRebootIntent() {
        final String REBOOT = "BroadcastRebootAlarm";
        setUpReceiver(REBOOT, AlarmUtil.RequestCodes.PERIOD_ALARM);

        assertEquals(1, mReceivedIntents.size());
        assertNull(mStubReceiver.getResultData());

        Intent receivedIntent = mReceivedIntents.get(0);
        assertEquals(receivedIntent.getAction(), REBOOT);
        assertEquals(AlarmUtil.RequestCodes.PERIOD_ALARM,
                receivedIntent.getIntExtra(CODE_ID, -1));

        int code = receivedIntent.getIntExtra(AlarmUtil.CODE_ID, 0);

        Date toBeScheduledAlarm = AlarmUtil.getAlarmDateNoOffset(mContext, code);
    }

    // Set alarms for an hour after present time and return context where broadcasts are registered
    private Context setAlarmContext(int code, long delay) {
        Context cxt = InstrumentationRegistry.getTargetContext();

        cxt.registerReceiver(mReceiver, new IntentFilter(BROADCAST_START_ALARM));
        cxt.registerReceiver(mReceiver, new IntentFilter(BROADCAST_REBOOT_ALARM));

        AlarmManager alarmManager = (AlarmManager)
                cxt.getSystemService(Context.ALARM_SERVICE);

        // Retrieve pendingIntent that will perform alarm broadcast.
        Intent alarmIntent = new Intent(cxt, CustomBroadcastReceiver.class);
        alarmIntent.putExtra(CODE_ID, code);
        alarmIntent.setAction(BROADCAST_START_ALARM);

        PendingIntent pendingAlarmIntent =
                PendingIntent.getBroadcast(cxt, code, alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC,
                Calendar.getInstance().getTimeInMillis() + delay, // one hour later
                pendingAlarmIntent);

        // pendingIntent that will perform reboot.
        Intent rebootIntent = new Intent(cxt, CustomBroadcastReceiver.class);
        rebootIntent.putExtra(CODE_ID, code);
        rebootIntent.setAction(BROADCAST_REBOOT_ALARM);

        PendingIntent pendingRebootIntent =
                PendingIntent.getBroadcast(cxt, code, rebootIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC,
                Calendar.getInstance().getTimeInMillis() + delay, // one hour later
                pendingRebootIntent);

        return cxt;
    }


    @SmallTest @UiThreadTest
    public void testSetAlarm() throws Exception {
        int code = AlarmUtil.RequestCodes.PERIOD_ALARM;
        Context cxt = setAlarmContext(code, (60 * 1000 * 60));

        assertTrue(AlarmUtil.isScheduledAlarm(cxt, code));
        assertTrue(AlarmUtil.isScheduledReboot(cxt, code));
    }

    @SmallTest @UiThreadTest
    public void testCancelAlarm() throws Exception {
        int code = AlarmUtil.RequestCodes.PERIOD_ALARM;
        Context cxt = setAlarmContext(code, (60 * 1000 * 60));
        AlarmUtil.cancelAlarm(cxt, code);

        assertFalse(AlarmUtil.isScheduledAlarm(cxt, code));
        assertFalse(AlarmUtil.isScheduledReboot(cxt, code));
    }


}
