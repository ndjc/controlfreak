package com.ncrossley.controlfreak;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for PowerLevel.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class PowerLevelTest
{
    /**
     * Test PowerLevel constructors.
     * @throws CFException if the test fails
     */
    @Test
    public void testNewPowerLevel() throws CFException
    {
        assertEquals(PowerLevel.Slow, PowerLevel.mkValue(0));
        assertEquals(PowerLevel.Medium, PowerLevel.mkValue(1));
        assertEquals(PowerLevel.Fast, PowerLevel.mkValue(2));
        assertEquals(PowerLevel.Max, PowerLevel.mkValue(3));
    }


    /**
     * Test PowerLevel getValue.
     * @throws CFException if the test fails
     */
    @Test
    public void testGetValue() throws CFException
    {
        assertEquals(0, PowerLevel.Slow.getValue());
        assertEquals(1, PowerLevel.Medium.getValue());
        assertEquals(2, PowerLevel.Fast.getValue());
        assertEquals(3, PowerLevel.Max.getValue());
    }


    /**
     * Test parsing PowerLevel value from string.
     * @throws CFException if the test fails
     */
    @Test
    public void testMakeValue() throws CFException
    {
        for (String s : new String[]{"medium"})
        {
            assertEquals(PowerLevel.Medium, PowerLevel.mkValue(s));
        }

        for (String s : new String[]{"high","fast"})
        {
            assertEquals(PowerLevel.Fast, PowerLevel.mkValue(s));
        }

        for (String s : new String[]{"max"})
        {
            assertEquals(PowerLevel.Max, PowerLevel.mkValue(s));
        }

        for (String s : new String[]{"low","slow"})
        {
            assertEquals(PowerLevel.Slow, PowerLevel.mkValue(s));
        }
    }


    /** Test failure to make values. */
    @Test
    public void testFail()
    {
        Exception exception = assertThrows(
            CFException.class, () -> PowerLevel.mkValue(4));

        String expectedMessage = "Invalid power level value";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));

        exception = assertThrows(
            CFException.class, () -> PowerLevel.mkValue("bad"));

        expectedMessage = "Invalid power level";
        actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));
    }
}
