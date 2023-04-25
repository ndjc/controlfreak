package com.ncrossley.controlfreak;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


/**
 * A 8kb buffer for writing Breville Control Freak program files.
 * <p>
 * Control Freak program files consist of 8K bytes written out to a USB stick in 32 blocks of 256 bytes each.
 * The first 4k (16 blocks) contains program entries; the second 4k contains alarm entries.
 * Each 256 byte block contains 7 entries of 36 bytes each, plus a 4 byte filler.
 * The data is terminated at end of file, or with a block starting with 0xff.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class CFOutBuffer extends CFBuffer implements AutoCloseable
{
    private OutputStream	output;
    private boolean   	    internal;


    /**
     * Construct a new CFOutBuffer writing to the named file.
     * @param outputFile the file to which this CFOutBuffer will write
     * @throws IOException if the file cannot be written
     */
    public CFOutBuffer(String outputFile) throws IOException
    {
        output = Files.newOutputStream(Paths.get(outputFile));
        Arrays.fill(buffer, (byte) 0xff);
        markReady();
    }


    /**
     * Construct a new CFOutBuffer writing to a hidden byte array.
     */
    public CFOutBuffer()
    {
        output = new ByteArrayOutputStream();
        Arrays.fill(buffer, (byte) 0xff);
        internal = true;
        markReady();
    }


    void writeBuffer() throws IOException
    {
        output.write(buffer);
        Arrays.fill(buffer, (byte) 0xff);
    }


    /**
     * Write a single entry (program or alarm) to the CFOutBuffer.
     * @param entry the entry to be written
     * @throws IOException if the output cannot be written
     * @throws CFException if the entry is malformed
     */
    public void writeEntry(Entry entry) throws IOException, CFException
    {
        BufferControls controls = entry.isProgramEntry() ? programControls : alarmControls;
        byte[] entryBytes = entry.getBytes();
        int length = entryBytes.length;

        if (controls.getOffset() + length > controls.getLimit())
        {
            controls.incrementOffset(4);
            controls.incrementLimit(BLOCK_LENGTH);
        }
        if (controls.getOffset() + length > controls.getLimit())
        {
            throw new CFException(String.format("Entry too long (%d)",length));
        }
        if (controls.getOffset() + length > (entry.isProgramEntry() ? ALARM_OFFSET : BUFFER_LENGTH))
        {
            throw new CFException("Too many programs to fit in one file");
        }

        System.arraycopy(entryBytes, 0, buffer, controls.getOffset(), length);
        controls.incrementOffset(length);
    }


    @Override
    public void close() throws IOException, CFException
    {
        if (programControls.getOffset() > 0 || alarmControls.getOffset() > 0)
        {
            writeBuffer();
        }
        output.close();
    }


    byte[] getBytes() throws CFException
    {
        if (internal)
        {
            return ((ByteArrayOutputStream)output).toByteArray();
        }
        else
        {
            throw new CFException("cannot convert external CFOutBuffer to byte array");
        }
    }
}

