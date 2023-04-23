package com.ncrossley.controlfreak;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Scanner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a single program for the Breville Control Freak.
 *
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.

 * @param name the program name (at most 20 bytes)
 * @param temperature in ºF
 * @param power the power level (speed)
 * @param timer the timer value, or 0 if no timer is set
 * @param timerStart when the timer is to start
 * @param afterTimer what happens when the timer expires
 */
@SuppressFBWarnings("IMC_IMMATURE_CLASS_WRONG_FIELD_ORDER")
record Program(
        String     name,
        int        temperature,
        PowerLevel power,
        Timer      timer,
        TimerStart timerStart,
        AfterTimer afterTimer) implements Comparable<Program>
{
    /**
     * The maximum temperature supported by the Breville Control Freak.
     */
    public static final int TEMP_MIN     = 0;


    /**
     * The maximum temperature supported by the Breville Control Freak.
     */
    public static final int TEMP_MAX     = 482;


    /**
     * The longest cooking time supported by the Breville Control Freak.
     */
    public static final int MAX_HOURS    = 72;


    // Constants exposed to test code
    static final int TEMP_MOD = 255;

    // Internal implementation constants
    private static final int ENTRY_LENGTH = 36;
    private static final int NAME_LENGTH  = 26;


    /**
     * Construct a new immutable Program.
     *
     * @param name the program name
     * @param temperature the temperature in ºF
     * @param power the heating power
     * @param timer the timer
     * @param timerStart when to start the timer
     * @param afterTimer what to do after the timer is up
     */
    Program(String name, int temperature, PowerLevel power, Timer timer, TimerStart timerStart,
            AfterTimer afterTimer)
    {
        if (name.length() > NAME_LENGTH)
        {
            throw new IllegalArgumentException("program name too long");
        }
        if (temperature < TEMP_MIN || temperature > TEMP_MAX)
        {
            throw new IllegalArgumentException("temperature out of range");
        }
        this.name = name;
        this.temperature = temperature;
        this.power = power;
        this.timer = timer;
        this.timerStart = timerStart;
        this.afterTimer = afterTimer;
    }


    @Override
    public int compareTo(Program o)
    {
        int c;
        if ((c=name.compareTo(o.name)) != 0)
        {
            return c;
        }
        else if ((c=Integer.compare(temperature,o.temperature)) != 0)
        {
            return c;
        }
        else if ((c=power.compareTo(o.power)) != 0)
        {
            return c;
        }
        else if ((c=timer.compareTo(o.timer)) != 0)
        {
            return c;
        }
        else if ((c=timerStart.compareTo(o.timerStart)) != 0)
        {
            return c;
        }
        else
        {
            return afterTimer.compareTo(o.afterTimer);
        }
    }


    @Override
    public String toString()
    {
        return String.format("%-26s | %3d | %6s | ", name, temperature, power)
            + (timer.isOff() ? String.format("%-46s","no timer")
                : String.format("%8s | %-16s | %-16s", timer, timerStart, afterTimer));
    }


    /**
     * Scan a string, parsing it into a Control Freak program.
     * The expected syntax is that produced by the @link #toString() method.
     * @param s the string to be parsed
     * @throws CFException if the string is malformed
     * @return the parsed program
     */
    public static Program mkProgram(String s) throws CFException
    {
        try (Scanner sc = new Scanner(s))
        {
            sc.useDelimiter("\\s*\\|\\s*|\\s*$");
            String theName = sc.next();

            //int theTemperature = Integer.min(sc.nextInt(), TEMP_MAX);
            int theTemperature = sc.nextInt();
            if (theTemperature < TEMP_MIN)
            {
                throw new CFException(String.format("Temperature %d too low (min %d)", theTemperature,TEMP_MIN));
            }
            if (theTemperature > TEMP_MAX)
            {
                throw new CFException(String.format("Temperature %d too high (max %d)", theTemperature,TEMP_MAX));
            }

            PowerLevel thePower = PowerLevel.mkValue(sc.next());
            Timer theTimer = Timer.mkValue(sc.next());
            TimerStart theTimerStart = (theTimer.isOff() ? TimerStart.AtBeginning : TimerStart.mkValue(sc.next()));
            AfterTimer theAfterTimer = (theTimer.isOff() ? AfterTimer.ContinueCooking : AfterTimer.mkValue(sc.next()));
            return new Program(theName,theTemperature,thePower,theTimer,theTimerStart,theAfterTimer);
        }
    }


    /**
     * Read and parse the hex representation of a program from the given buffer.
     * @param buffer the buffer containing a hex program
     * @return the parsed program
     * @throws CFException if the hex representation in the buffer is malformed
     * @throws IOException if the input cannot be read
     */
    public static Program readProgram(CFInBuffer buffer) throws CFException, IOException
    {
        byte[] entry = new byte[ENTRY_LENGTH];
        int len = buffer.readEntry(entry);
        if (len < 0 || entry[0] == -1) return null;

        if (len != ENTRY_LENGTH)
        {
            throw new CFException(String.format("Program entry was %d bytes long, should have been %d", len, ENTRY_LENGTH));
        }

        int nameEnd = indexOfByte(0,entry,0,NAME_LENGTH);
        if (nameEnd == 0)
        {
            throw new CFException(String.format("Program name blank in entry %s",HexFormat.of().withUpperCase().formatHex(entry)));
        }

        if (nameEnd < 0)
        {
            nameEnd = 26;
        }
        String programName = new String(entry, 0, nameEnd, StandardCharsets.US_ASCII);

        int temp = entry[30] & 0xff;
        int control = entry[31] & 0xff;
        if ((control & 0x80) != 0) temp += TEMP_MOD;
        temp = Integer.min(temp,TEMP_MAX);

        AfterTimer afterTimer = AfterTimer.mkValue((control >> 5) & 0x03);
        PowerLevel powerLevel = PowerLevel.mkValue((control >> 2) & 0x03);
        TimerStart timerStart = TimerStart.mkValue(control & 0x03);

        int hours = entry[32] & 0xff;
        int minutes = entry[33] & 0xff;
        int seconds = entry[34] & 0xff;
        Timer timer = new Timer(hours, minutes, seconds);

        int checkSum = 0;
        for (int i=0; i < ENTRY_LENGTH-1; i++)
        {
            checkSum += entry[i] & 0xff;
        }
        checkSum &= 0xff;
        if (checkSum != (entry[35] & 0xff))
        {
            throw new CFException(String.format("Checksum does not match (%02x != %02x)",checkSum,entry[35]));
        }

        return new Program(programName,temp,powerLevel, timer, timerStart, afterTimer);
    }


    /**
     * Write the hex representation of a program to a given buffer.
     * @param out the buffer to which the hex representation is written
     * @throws IOException if the output cannot be written
     * @throws CFException if the program is malformed
     */
    public void writeProgram(CFOutBuffer out) throws IOException, CFException
    {
        byte[] entry = new byte[ENTRY_LENGTH];

        // Write program name to buffer
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        int nlen = nameBytes.length;
        if (nlen > NAME_LENGTH)
        {
            throw new CFException(String.format("Program name %s too long (max %d)", name,NAME_LENGTH));
        }
        for (int i=0; i<=NAME_LENGTH; i++)
        {
            entry[i] = (i < nlen ? nameBytes[i] : 0);
        }

        // Write temperature to buffer
        if (temperature > TEMP_MOD)
        {
            entry[30] = (byte) (temperature - TEMP_MOD);
            entry[31] = (byte) 0x80;
        }
        else
        {
            entry[30] = (byte)(temperature & 0xff);
        }

        // Write control values to buffer
        entry[31] |= afterTimer.getValue() << 5;
        entry[31] |= power.getValue() << 2;
        entry[31] |= timerStart.getValue();

        // Write timer to buffer
        entry[32] = (byte)(timer.hours());
        entry[33] = (byte)(timer.minutes());
        entry[34] = (byte)(timer.seconds());

        // Write checksum to buffer
        int checkSum = 0;
        for (int i=0; i < ENTRY_LENGTH-1; i++)
        {
            checkSum += entry[i] & 0xff;
        }
        entry[35] = (byte)(checkSum & 0xff);

        // Write buffer to output
        out.writeEntry(entry);
    }


    private static int indexOfByte(int item, byte[] arr, int start, int len)
    {
        for (int offset = start; offset < arr.length && offset < start+len; offset++)
        {
            if (item == arr[offset])
            {
                return offset;
            }
        }
        return -1;
    }
}
