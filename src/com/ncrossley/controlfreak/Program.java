package com.ncrossley.controlfreak;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Scanner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a single program for the Breville Control Freak.
 * <p>
 * The binary representation of a program is as follows:
 * Bytes 00-25 contain the null-terminated name.
 * Bytes 26-29 are unused.
 * Byte  30    corresponds to the temperature in Fahrenheit modulo 256
 * Byte  31    control byte
 * 			   bit 7: if set, add 255 to the temperature to (for temperatures from 256-482)
 *             bits 5-6: action after timer: Continue, Stop, Keep Warm, Repeat
 *             bit 4: unused
 *             bits 2-3: power level (Low, Medium, High, Max)
 *             bits 0-1: Timer start control (At Beginning, At Set Temperature, At Prompt)
 * Byte 32     corresponds to the hour timer value (0-71, or 72 if both minutes and seconds are 0)
 * Byte 33     corresponds to the minute timer value (0-59).
 * Byte 34     corresponds to the second timer value (0-59).
 * Byte 35     is a checksum of all previous bytes.
 *
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.

 * @param name the program name (at most 26 ASCII bytes)
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
        if (name.length() > CFConstants.PROGRAM_NAME_LENGTH)
        {
            throw new IllegalArgumentException(String.format("program name %s too long",name));
        }
        if (!name.matches("[A-Za-z 0-9(),./:-]+"))
        {
            throw new IllegalArgumentException(String.format("invalid characters in name %s",name));
        }
        if (temperature < CFConstants.TEMP_MIN || temperature > CFConstants.TEMP_MAX)
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
            + (timer.isOff() ? String.format("%s","no timer")
                : String.format("%8s | %-16s | %s", timer, timerStart, afterTimer));
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

            int theTemperature = sc.nextInt();
            if (theTemperature < CFConstants.TEMP_MIN)
            {
                throw new CFException(String.format("Temperature %d too low (min %d)", theTemperature,CFConstants.TEMP_MIN));
            }
            if (theTemperature > CFConstants.TEMP_MAX)
            {
                throw new CFException(String.format("Temperature %d too high (max %d)", theTemperature,CFConstants.TEMP_MAX));
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
        Entry entry = buffer.readEntry(EntryType.ProgramEntry);
        if (entry == null) return null;
        byte[] entryBytes = entry.getBytes();

        int len = entryBytes.length;
        if (len != CFConstants.ENTRY_LENGTH)
        {
            throw new CFException(String.format("Program entry was %d bytes long, should have been %d", len, CFConstants.ENTRY_LENGTH));
        }

        int nameEnd = indexOfByte(0,entryBytes,0,CFConstants.PROGRAM_NAME_LENGTH);
        if (nameEnd == 0)
        {
            throw new CFException(String.format("Program name blank in entry %s",HexFormat.of().withUpperCase().formatHex(entryBytes)));
        }

        if (nameEnd < 0)
        {
            nameEnd = 26;
        }
        String programName = new String(entryBytes, 0, nameEnd, StandardCharsets.US_ASCII);

        int temp = entryBytes[30] & 0xff;
        int control = entryBytes[31] & 0xff;
        if ((control & 0x80) != 0) temp += CFConstants.TEMP_MOD;
        temp = Integer.min(temp,CFConstants.TEMP_MAX);

        AfterTimer afterTimer = AfterTimer.mkValue((control >> 5) & 0x03);
        PowerLevel powerLevel = PowerLevel.mkValue((control >> 2) & 0x03);
        TimerStart timerStart = TimerStart.mkValue(control & 0x03);

        int hours = entryBytes[32] & 0xff;
        int minutes = entryBytes[33] & 0xff;
        int seconds = entryBytes[34] & 0xff;
        Timer timer = new Timer(hours, minutes, seconds);

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
        byte[] entryBytes = new byte[CFConstants.ENTRY_LENGTH];

        // Write program name to buffer
        byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
        int nlen = nameBytes.length;
        if (nlen > CFConstants.PROGRAM_NAME_LENGTH)
        {
            throw new CFException(String.format("Program name %s too long (max %d)", name,CFConstants.PROGRAM_NAME_LENGTH));
        }
        for (int i=0; i<=CFConstants.PROGRAM_NAME_LENGTH; i++)
        {
            entryBytes[i] = (i < nlen ? nameBytes[i] : 0);
        }

        // Write temperature to buffer
        if (temperature > CFConstants.TEMP_MOD)
        {
            entryBytes[30] = (byte) (temperature - CFConstants.TEMP_MOD);
            entryBytes[31] = (byte) 0x80;
        }
        else
        {
            entryBytes[30] = (byte)(temperature & 0xff);
        }

        // Write control values to buffer
        entryBytes[31] |= afterTimer.getValue() << 5;
        entryBytes[31] |= power.getValue() << 2;
        entryBytes[31] |= timerStart.getValue();

        // Write timer to buffer
        entryBytes[32] = (byte)(timer.hours());
        entryBytes[33] = (byte)(timer.minutes());
        entryBytes[34] = (byte)(timer.seconds());

        // Write checksum to buffer
        entryBytes[35] = Entry.checksum(entryBytes);

        // Write buffer to output
        Entry entry = new Entry(EntryType.ProgramEntry,entryBytes);
        out.writeEntry(entry);
    }


    /**
     * Find a byte value in an array of bytes.
     * @param item the byte to be found
     * @param arr the array to search
     * @param start where to start the search
     * @param len how many bytes to search
     * @return the offset of the found byte, or -1 if not found
     */
    public static int indexOfByte(int item, byte[] arr, int start, int len)
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
