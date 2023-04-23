package com.ncrossley.controlfreak;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for AfterTimer.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class AfterTimerTest
{
    /**
     * Test AfterTimer constructors.
     * @throws CFException if the test fails
     */
    @Test
    public void testNewAfterTimer() throws CFException
    {
        assertEquals(AfterTimer.ContinueCooking, AfterTimer.mkValue(0));
        assertEquals(AfterTimer.StopCooking, AfterTimer.mkValue(1));
        assertEquals(AfterTimer.KeepWarm, AfterTimer.mkValue(2));
        assertEquals(AfterTimer.RepeatTimer, AfterTimer.mkValue(3));
    }


    /**
     * Test AfterTimer getValue.
     * @throws CFException if the test fails
     */
    @Test
    public void testGetValue() throws CFException
    {
        assertEquals(0, AfterTimer.ContinueCooking.getValue());
        assertEquals(1, AfterTimer.StopCooking.getValue());
        assertEquals(2, AfterTimer.KeepWarm.getValue());
        assertEquals(3, AfterTimer.RepeatTimer.getValue());
    }


    /**
     * Test parsing AfterTimer value from string.
     * @throws CFException if the test fails
     */
    @Test
    public void testMakeValue() throws CFException
    {
        for (String s : new String[]{"continuecooking", "continue cooking","continue"})
        {
            assertEquals(AfterTimer.ContinueCooking, AfterTimer.mkValue(s));
        }

        for (String s : new String[]{"stopcooking", "stop cooking","stop"})
        {
            assertEquals(AfterTimer.StopCooking, AfterTimer.mkValue(s));
        }

        for (String s : new String[]{"keepwarm","keep warm","keep","warm"})
        {
            assertEquals(AfterTimer.KeepWarm, AfterTimer.mkValue(s));
        }

        for (String s : new String[]{"repeattimer","repeat timer","repeat"})
        {
            assertEquals(AfterTimer.RepeatTimer, AfterTimer.mkValue(s));
        }
    }


    /** Test failure to make values. */
    @Test
    public void testFail()
    {
        Exception exception = assertThrows(
            CFException.class, () -> AfterTimer.mkValue(4));

        String expectedMessage = "Invalid after timer value";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));

        exception = assertThrows(
            CFException.class, () -> AfterTimer.mkValue("bad"));

        expectedMessage = "Invalid after timer";
        actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));
    }
}
