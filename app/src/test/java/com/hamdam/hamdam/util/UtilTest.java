package com.hamdam.hamdam.util;

import junit.framework.TestCase;

import org.junit.Test;

/**
 *
 */
public class UtilTest extends TestCase {

    private static final int EVEN_LEN = 6, ODD_LEN = 9, SHORT_LEN = 5;

    /*

    public static int calculateDimness(int index, int length) {
        return Math.abs(((index + 1) / 2) *2 - (length + 1) / 2);
    }
     */

    @Test
    public void testDimnessZero() throws Exception {
        int actual = DateUtil.calculateDimness(0, EVEN_LEN);
        assertEquals(0, actual);
    }

    @Test
    public void testDimnessEnd() throws Exception {
        int actual = DateUtil.calculateDimness(5, EVEN_LEN);
        assertEquals(0, actual);
    }

    @Test
    public void testDimnessMid() throws Exception {
        int actual = DateUtil.calculateDimness(2, EVEN_LEN);
        assertEquals(2, actual);
    }

    @Test
    public void testDimnessSimpleMid() throws Exception {
        int actual = DateUtil.calculateDimness(3, EVEN_LEN);
        assertEquals(2, actual);
    }

    @Test
    public void testDimnessNearBegin() throws Exception {
        int actual = DateUtil.calculateDimness(4, EVEN_LEN);
        assertEquals(1, actual);
    }

    @Test
    public void testDimnessOddLength() throws Exception {
        int actual = DateUtil.calculateDimness(0, ODD_LEN);
        assertEquals(0, actual);
    }

    @Test
    public void testDimnessOddLengthMid() throws Exception {
        int actual = DateUtil.calculateDimness(1, ODD_LEN);
        assertEquals(1, actual);
    }

    @Test
    public void testDimnessOddLengthEnd() throws Exception {
        int actual = DateUtil.calculateDimness(7, ODD_LEN);
        assertEquals(1, actual);
    }
}
