package com.ncrossley.controlfreak;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Scanner;

/**
 * Represents a single alarm for the Breville Control Freak.
 * <p>
 * The binary representation of an alarm is as follows:
 * Bytes 00-19 contain the null-terminated name.
 * Bytes 20-29 are unused.
 * Byte  30    corresponds to the temperature in Fahrenheit modulo 256
 * Byte  31    control byte
 * 			   bit 7: if set, add 255 to the temperature to (for temperatures from 256-482)
 *             the other bits are unused
 *
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 *
 * @param name the alarm name (at most 20 ASCII bytes)
 * @param temperature in ºF
 */
record Alarm(
    String     name,
    int        temperature) implements Comparable<Alarm>
{
    /**
     * Construct a new immutable Alarm.
     *
     * @param name the program name
     * @param temperature the temperature in ºF
     */
    Alarm(String name, int temperature)
    {
        if (name.length() > CFConstants.ALARM_NAME_LENGTH)
        {
            throw new IllegalArgumentException(String.format("alarm name %s too long",name));
        }
        if (!name.matches("[A-Za-z 0-9]+"))
        {
            throw new IllegalArgumentException(String.format("invalid characters in name %s",name));
        }
        if (temperature < CFConstants.TEMP_MIN || temperature > CFConstants.TEMP_MAX)
        {
            throw new IllegalArgumentException("temperature out of range");
        }
        this.name = name;
        this.temperature = temperature;
    }


    @Override
    public int compareTo(Alarm o)
    {
        int c;
        if ((c=name.compareTo(o.name)) != 0)
        {
            return c;
        }
        else
        {
            return Integer.compare(temperature,o.temperature);
        }
    }


    @Override
    public String toString()
    {
        return String.format("%-26s | %3d", name, temperature);
    }


    /**
     * Scan a string, parsing it into a Control Freak alarm.
     * The expected syntax is that produced by the @link #toString() method.
     * @param s the string to be parsed
     * @throws CFException if the string is malformed
     * @return the parsed alarm
     */
    public static Alarm mkAlarm(String s) throws CFException
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

            return new Alarm(theName,theTemperature);
        }
    }


    /**
     * Read and parse the hex representation of an alarm from the given buffer.
     * @param buffer the buffer containing a hex alarm
     * @return the parsed alarm
     * @throws CFException if the hex representation in the buffer is malformed
     * @throws IOException if the input cannot be read
     */
    public static Alarm readAlarm(CFInBuffer buffer) throws CFException, IOException
    {
        Entry entry = buffer.readEntry(EntryType.AlarmEntry);
        if (entry == null) return null;
        byte[] entryBytes = entry.getBytes();

        int len = entryBytes.length;
        if (len != CFConstants.ENTRY_LENGTH)
        {
            throw new CFException(String.format("Alarm entry was %d bytes long, should have been %d", len, CFConstants.ENTRY_LENGTH));
        }

        int nameEnd = Program.indexOfByte(0,entryBytes,0,CFConstants.ALARM_NAME_LENGTH);
        if (nameEnd == 0)
        {
            throw new CFException(String.format("Alarm name blank in entry %s",HexFormat.of().withUpperCase().formatHex(entryBytes)));
        }

        if (nameEnd < 0)
        {
            nameEnd = CFConstants.ALARM_NAME_LENGTH;
        }
        String alarmName = new String(entryBytes, 0, nameEnd, StandardCharsets.US_ASCII);

        int temp = entryBytes[30] & 0xff;
        int control = entryBytes[31] & 0xff;
        if ((control & 0x80) != 0) temp += CFConstants.TEMP_MOD;
        temp = Integer.min(temp,CFConstants.TEMP_MAX);

       return new Alarm(alarmName,temp);
    }


    /**
     * Write the hex representation of an alarm to a given buffer.
     * @param out the buffer to which the hex representation is written
     * @throws CFException if the alarm is malformed
     */
    public void writeAlarm(CFOutBuffer out) throws CFException
    {
        byte[] entryBytes = new byte[CFConstants.ENTRY_LENGTH];

        // Write program name to buffer
        byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
        int nlen = nameBytes.length;
        if (nlen > CFConstants.ALARM_NAME_LENGTH)
        {
            throw new CFException(String.format("Alarm name %s too long (max %d)", name,CFConstants.PROGRAM_NAME_LENGTH));
        }
        for (int i=0; i<=CFConstants.ALARM_NAME_LENGTH; i++)
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

        // Write checksum to buffer
        entryBytes[35] = Entry.checksum(entryBytes);

        // Write buffer to output
        Entry entry = new Entry(EntryType.AlarmEntry,entryBytes);
        out.writeEntry(entry);
    }

}
