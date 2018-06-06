package com.hamdam.hamdam.model;

import android.test.suitebuilder.annotation.SmallTest;

import com.hamdam.hamdam.enums.StatusEnum;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test Enums class
 */
@RunWith(JUnit4.class)
@SmallTest
public class StatusEnumTest extends TestCase {

    @Test
    public void testGetStatusMain() throws Exception {
        assertEquals(StatusEnum.StatusMain.BODY,
                StatusEnum.StatusType.BLEEDING.getStatusMain());
        assertEquals(StatusEnum.StatusMain.BODY,
                StatusEnum.StatusType.SLEEP.getStatusMain());
        assertNotSame(StatusEnum.StatusType.MOOD.getStatusMain(),
                StatusEnum.StatusType.BLEEDING.getStatusMain());

    }

    @Test
    public void testGetStatusType() throws Exception {
        assertEquals(StatusEnum.StatusType.FLUIDS,
                StatusEnum.StatusValue.ABNORMAL.getStatusType());
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.BLEEDING),
                StatusEnum.StatusType.BLEEDING);
    }

    @Test
    public void testGetStatusValue() throws Exception {
        assertEquals(StatusEnum.StatusValue.BACKACHE,
                StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.PAIN,
                        StatusEnum.Options.THREE));
        assertEquals(StatusEnum.StatusValue.LITTLE_SLEEP,
                StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SLEEP,
                        StatusEnum.Options.TWO));
    }

    @Test
    public void testValidTag() throws Exception {
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.BLEEDING),
                StatusEnum.StatusType.BLEEDING);
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.FLUIDS),
                StatusEnum.StatusType.FLUIDS);
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.PAIN),
                StatusEnum.StatusType.PAIN);
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.SLEEP),
                StatusEnum.StatusType.SLEEP);
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.EXERCISE),
                StatusEnum.StatusType.EXERCISE);
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.SEX),
                StatusEnum.StatusType.SEX);
        assertEquals(StatusEnum.StatusType.getByTag(StatusEnum.Topics.MOOD),
                StatusEnum.StatusType.MOOD);

    }

    @Test
    public void testNotNullTag() throws Exception {
        for (int i = 0; i < 7; i++) {
            assertNotNull(StatusEnum.StatusType.getByTag(i));
        }
    }

    @Test
    public void testValidOrdinal() throws Exception {
        assertEquals(StatusEnum.Options.ONE, StatusEnum.StatusValue.NO_SLEEP.getOrdinal());
        assertEquals(StatusEnum.Options.THREE, StatusEnum.StatusValue.BACKACHE.getOrdinal());
    }

    @Test
    public void testInvalidTag() throws Exception {
        assertNull(StatusEnum.StatusType.getByTag(13));
        assertNull(StatusEnum.StatusType.getByTag(-1));
    }

    @Test
    public void testInvalidOrdinal() throws Exception {
        assertNull(StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.EXERCISE,
                StatusEnum.Options.TWO));
        assertNull(StatusEnum.StatusValue.getByOrdinal(StatusEnum.StatusType.SEX,
                StatusEnum.Options.FOUR));


    }
}
