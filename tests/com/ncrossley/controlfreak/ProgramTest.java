package com.ncrossley.controlfreak;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Unit & System tests for Program.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class ProgramTest
{
    /**
     * Test reading and writing a program with various temperatures.
     * This also tests reading and writing programs to a stream.
     * @throws CFException if the test fails
     * @throws IOException if the test fails
     */
    @Test
    public void testTemps() throws CFException, IOException
    {
        tempTest(0);
        tempTest(1);
        tempTest(86);
        tempTest(CFConstants.TEMP_MOD-1);
        tempTest(CFConstants.TEMP_MOD);
        tempTest(CFConstants.TEMP_MOD+1);
        tempTest(254);
        tempTest(255);
        tempTest(256);
        tempTest(257);
        tempTest(380);
        tempTest(CFConstants.TEMP_MAX);
    }


    /** Test temperatures too low. */
    @Test
    public void testLowTemp()
    {
        Exception exception = assertThrows(
            CFException.class, () -> tempTest(-1));

        String expectedMessage = "too low";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));
    }


    /** Test temp too high. */
    @Test
    public void testHighTemp()
    {
        Exception exception = assertThrows(
            CFException.class, () -> tempTest(CFConstants.TEMP_MAX+1));

        String expectedMessage = "too high";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));
    }


    private static void tempTest(int temp) throws CFException, IOException
    {
        Program program1 = Program.mkProgram(String.format("test program|%d|High|off",temp));
        try (CFOutBuffer out = new CFOutBuffer();
             CFInBuffer in = new CFInBuffer(getBytes(program1, out)))
        {
            Program program2 = Program.readProgram(in);
            assertEquals(temp,program1.temperature());
            assertEquals(temp,program2.temperature());
        }
    }


    private static byte[] getBytes(Program p, CFOutBuffer out) throws IOException, CFException
    {
        p.writeProgram(out);
        out.close();
        byte[] bytes = out.getBytes();
        return bytes;
    }


    /** Test too many programs. */
    @Test
    public void testTooMany()
    {
        Exception exception = assertThrows(
            CFException.class, () ->
                {
                    try (CFOutBuffer buffer = new CFOutBuffer())
                    {
                        for (int i=0; i<=CFConstants.MAX_ENTRIES; i++)
                        {
                            Program p = Program.mkProgram(String.format("P%d | 100 | Fast | off", i));
                            p.writeProgram(buffer);
                        }
                    }
                });

        String expectedMessage = "Too many programs";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage,actualMessage.contains(expectedMessage));
    }


    /**
     * Test parsing program from string.
     * @throws CFException if the test fails
     */
    @Test
    public void testMakeProgram() throws CFException
    {
        Program p1 = Program.mkProgram("p1|88|Slow|off");
        assertEquals("Program name wrong","p1",p1.name());
        assertEquals("Temperature wrong",88,p1.temperature());
        assertEquals("Power level wrong",PowerLevel.Slow,p1.power());
        assertTrue("Timer should be off",p1.timer().isOff());

        Program p2 = Program.mkProgram("p2|450|Fast|3:0:0|AtBeginning|KeepWarm");
        assertEquals("Program name wrong","p2",p2.name());
        assertEquals("Temperature wrong",450,p2.temperature());
        assertEquals("Timer wrong"," 3:00:00",p2.timer().toString());
        assertEquals("Power level wrong",PowerLevel.Fast,p2.power());
        assertFalse("Timer should be on",p2.timer().isOff());
    }



    /**
     * Test converting program to string.
     * @throws CFException if the test fails
     */
    @Test
    public void testToString() throws CFException
    {
        Program p1 = new Program("p1",88,PowerLevel.Slow,new Timer(0,0,0),TimerStart.AtBeginning,AfterTimer.ContinueCooking);
        assertEquals("Program name wrong","p1",p1.name());
        assertEquals("Temperature wrong",88,p1.temperature());
        assertEquals("Power level wrong",PowerLevel.Slow,p1.power());
        assertTrue("Timer should be off",p1.timer().isOff());
        assertMatches(p1.toString());
        assertMatches("p2|120|Fast|2:30:30|atprompt|keepwarm");
    }


    private static void assertMatches(String programStr) throws CFException
    {
        String str = Program.mkProgram(programStr).toString();
        String re = "^\\s*" + programStr.replaceAll("\\s*\\|\\s*", "\\\\s*\\\\|\\\\s*");
        re = re.replaceAll("\\s*$", "") + "\\s*$";
        Pattern pat = Pattern.compile(re, Pattern.CASE_INSENSITIVE);
        assertTrue(String.format("Pattern %s failed to match %s",re,str),pat.matcher(str).matches());
    }


    /**
     * Test comparing programs for ordering.
     * @throws CFException if the test fails
     */
    @Test
    public void testCompare() throws CFException
    {
        Program p1 = Program.mkProgram("p1|0|Slow|off");
        Program p2 = Program.mkProgram("p2|0|Slow|off");
        Program p3 = Program.mkProgram("p2|10|Slow|off");
        Program p4 = Program.mkProgram("p2|10|Fast|off");
        Program p5 = Program.mkProgram("p2|10|Fast|0:0:30|atbeginning|continuecooking");
        Program p6 = Program.mkProgram("p2|10|Fast|0:0:30|atprompt|continuecooking");
        Program p7 = Program.mkProgram("p2|10|Fast|0:0:30|atprompt|stopcooking");
        Program p8 = Program.mkProgram("p2|10|Fast|0:0:30|atprompt|stopcooking");
        assertTrue("Wrong program ordering",p1.compareTo(p2) < 0);
        assertTrue("Wrong program ordering",p2.compareTo(p3) < 0);
        assertTrue("Wrong program ordering",p3.compareTo(p4) < 0);
        assertTrue("Wrong program ordering",p4.compareTo(p5) < 0);
        assertTrue("Wrong program ordering",p5.compareTo(p6) < 0);
        assertTrue("Wrong program ordering",p6.compareTo(p7) < 0);
        assertEquals("Wrong program ordering",0, p7.compareTo(p8));
        assertEquals("Wrong program ordering",0, p1.compareTo(p1));
    }
}
