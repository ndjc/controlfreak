package com.ncrossley.controlfreak;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


/**
 * A 256 byte buffer for writing Breville Control Freak program files.
 * <p>
 * Control Freak program files consist of 8K bytes written out to a USB stick in 32 blocks of 256 bytes each.
 * Each 256 byte block contains 7 programs of 36 bytes each, plus a 4 byte filler.
 * The data is terminated at end of file, or with a block starting with 0xff.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class CFOutBuffer implements AutoCloseable
{
    /**
     * The maximum number of programs in a single file.
     */
    public static final int  MAX_PROGRAMS  = 32 * 7;

    private static final int BUFFER_LENGTH = 8192;
    private static final int BLOCK_LENGTH  = 256;

    private OutputStream	output;
    private byte[]			buffer = new byte[BUFFER_LENGTH];
    private int				limit = BLOCK_LENGTH-4;
    private int				offset = 0;
    private final boolean	internal;


    /**
     * Construct a new CFOutBuffer writing to the named file.
     * @param outputFile the file to which this CFOutBuffer will write
     * @throws IOException if the file cannot be written
     */
    public CFOutBuffer(String outputFile) throws IOException
    {
        output = Files.newOutputStream(Paths.get(outputFile));
        Arrays.fill(buffer, (byte) 0xff);
        internal = false;
    }


    /**
     * Construct a new CFOutBuffer writing to a hidden byte array.
     */
    public CFOutBuffer()
    {
        output = new ByteArrayOutputStream();
        Arrays.fill(buffer, (byte) 0xff);
        internal = true;
    }


    private void writeBuffer() throws IOException
    {
        output.write(buffer);
        offset = 0;
        limit = BLOCK_LENGTH-4;
        Arrays.fill(buffer, (byte) 0xff);
    }


    /**
     * Write a single program entry to the CFOutBuffer.
     * @param entry the byte array into which the program entry is written
     * @throws IOException if the output cannot be written
     * @throws CFException if the entry is malformed
     */
    public void writeEntry(byte[] entry) throws IOException, CFException
    {
        if (offset + entry.length > limit)
        {
            offset += 4;
            limit += BLOCK_LENGTH;
        }
        if (offset + entry.length > limit)
        {
            throw new CFException(String.format("Entry too long (%d)",entry.length));
        }
        if (offset + entry.length > BUFFER_LENGTH)
        {
            throw new CFException("Too many programs to fit in one file");
        }

        System.arraycopy(entry, 0, buffer, offset, entry.length);
        offset += entry.length;
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


    @Override
    public void close() throws IOException, CFException
    {
        if (offset > 0)
        {
            writeBuffer();
        }
        output.close();
    }
}
