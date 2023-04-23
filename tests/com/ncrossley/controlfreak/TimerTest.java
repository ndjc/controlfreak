package com.ncrossley.controlfreak;

import static org.junit.Assert.*;

import java.util.InputMismatchException;

import org.junit.Test;

/**
 * Unit tests for Timer.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class TimerTest
{

    /**
     * Test timer construction and isOff().
     */
    @Test
    public void test()
    {
        Timer t1 = new Timer(0,0,0);
        assertTrue("timer should be off",t1.isOff());

        Timer t2 = new Timer(0,0,1);
        assertFalse("timer should not be off",t2.isOff());

        badTimer(0,0,-1);
        badTimer(0,-1,0);
        badTimer(-1,0,0);
        badTimer(0,60,0);
        badTimer(0,0,60);
        badTimer(72,1,0);
        badTimer(72,0,1);
        badTimer(73,0,0);
    }


    private static void badTimer(int hours, int minutes, int seconds)
    {
        Exception exception = assertThrows(
            IllegalArgumentException.class, () -> new Timer(hours,minutes,seconds));

        String expectedMessage = "timer out of range";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));
    }

    /** Test time compareTo. */
    @Test
    public void testCompare()
    {
        Timer zero1 = new Timer(0,0,0);
        Timer zero2 = new Timer(0,0,0);

        Timer fiveSec = new Timer(0,0,5);
        Timer fiveMin1 = new Timer(0,5,0);
        Timer fiveMin2 = new Timer(0,5,0);

        Timer threeDays = new Timer(72,0,0);

        compareTimers(zero1,fiveSec);
        compareTimers(zero1,fiveMin1);
        compareTimers(zero1,fiveMin2);
        compareTimers(zero1,threeDays);
        compareTimers(fiveMin1,threeDays);

        equalTimers(zero1,zero2);
        equalTimers(fiveMin1,fiveMin2);
        equalTimers(threeDays,threeDays);
    }


    /** Test parsing Timer from String. */
    @Test
    public void testParse()
    {
        equalTimers(new Timer(0,0,0), Timer.mkValue("off"));
        equalTimers(new Timer(0,0,0), Timer.mkValue("no timer"));
        equalTimers(new Timer(72,0,0), Timer.mkValue("72:0:0"));
        equalTimers(new Timer(5,1,45), Timer.mkValue("5:01:45"));

        Exception exception = assertThrows(
            IllegalArgumentException.class, () -> Timer.mkValue("74:70:70"));

        String expectedMessage = "timer out of range";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));

        assertThrows(
            InputMismatchException.class, () -> Timer.mkValue("no digits"));
}


    private static void compareTimers(Timer t1, Timer t2)
    {
        assertTrue(String.format("'%s' should be less than '%s'",t1,t2),t1.compareTo(t2) < 0);
        assertFalse(String.format("'%s' should be less than '%s'",t1,t2),t2.compareTo(t1) < 0);
    }


    private static void equalTimers(Timer t1, Timer t2)
    {
        assertTrue(String.format("'%s' should be equal to '%s'",t1,t2),t1.compareTo(t2) == 0);
    }

}
