package com.ncrossley.controlfreak;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for TimerStart.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class TimerStartTest
{
    /**
     * Test TimerStart constructors.
     * @throws CFException if the test fails
     */
    @Test
    public void testNewTimerStart() throws CFException
    {
        assertEquals(TimerStart.AtBeginning, TimerStart.mkValue(0));
        assertEquals(TimerStart.AtSetTemperature, TimerStart.mkValue(1));
        assertEquals(TimerStart.AtPrompt, TimerStart.mkValue(2));
    }


    /**
     * Test AfterTimer getValue.
     * @throws CFException if the test fails
     */
    @Test
    public void testGetValue() throws CFException
    {
        assertEquals(0, TimerStart.AtBeginning.getValue());
        assertEquals(1, TimerStart.AtSetTemperature.getValue());
        assertEquals(2, TimerStart.AtPrompt.getValue());
    }


    /**
     * Test parsing AfterTimer value from string.
     * @throws CFException if the test fails
     */
    @Test
    public void testMakeValue() throws CFException
    {
        for (String s : new String[]{"atsettemperature", "at set temperature","at set","at temperature","set"})
        {
            assertEquals(TimerStart.AtSetTemperature, TimerStart.mkValue(s));
        }

        for (String s : new String[]{"atprompt", "at prompt","prompt"})
        {
            assertEquals(TimerStart.AtPrompt, TimerStart.mkValue(s));
        }

        for (String s : new String[]{"atbeginning","at beginning","beginning","immediately"})
        {
            assertEquals(TimerStart.AtBeginning, TimerStart.mkValue(s));
        }
    }


    /** Test failure to make values. */
    @Test
    public void testFail()
    {
        Exception exception = assertThrows(
            CFException.class, () -> TimerStart.mkValue(3));

        String expectedMessage = "Invalid timer start value";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));

        exception = assertThrows(
            CFException.class, () -> TimerStart.mkValue("bad"));

        expectedMessage = "Invalid timer start";
        actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));
    }
}
