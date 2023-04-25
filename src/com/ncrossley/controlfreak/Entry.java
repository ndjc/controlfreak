package com.ncrossley.controlfreak;

/**
 * Represents a single entry, for either a program or an alarm.
 * An entry is encoded as 36 bytes - 35 bytes of data plus a single byte checksum.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class Entry
{
    /** The length of an entry - program or alarm. */
    public static final int ENTRY_LENGTH = 36;


    private EntryType entryType;
    private byte[] entryBytes = new byte[ENTRY_LENGTH];


    /**
     * Construct a new Entry, verifying the entry length and the checksum.
     * @param entryType the type of entry
     * @param buffer the byte array containing the entry
     * @throws CFException if the entry is the wrong length or the checksum is incorrect
     */
    public Entry(EntryType entryType, byte[] buffer) throws CFException
    {
        int len = buffer.length;
        if (len != ENTRY_LENGTH)
        {
            throw new CFException(String.format("Entry was %d bytes long, should have been %d", len, ENTRY_LENGTH));
        }

        int checkSum = checksum(entryBytes);
        if (checkSum != (entryBytes[35] & 0xff))
        {
            throw new CFException(String.format("Checksum does not match (%02x != %02x)",checkSum,entryBytes[35]));
        }

        this.entryType = entryType;
        System.arraycopy(buffer, 0, entryBytes, 0, ENTRY_LENGTH);
    }


    /**
     * Test if this entry is for a program or an alarm.
     * @return true if this is a program entry
     */
    public boolean isProgramEntry()
    {
        return entryType == EntryType.ProgramEntry;
    }


    /**
     * Calculate the checksum for an entry.
     * @param entry the byte array containing the entry
     * @return the checksum
     */
    public static byte checksum(byte[] entry)
    {
        int checkSum = 0;
        for (int i=0; i < ENTRY_LENGTH-1; i++)
        {
            checkSum += entry[i] & 0xff;
        }
        return (byte) (checkSum & 0xff);
    }


    /**
     * Get the byte array for this entry.
     * @return a copy of the bytes for this entry
     */
    public byte[] getBytes()
    {
        return entryBytes.clone();
    }
}
