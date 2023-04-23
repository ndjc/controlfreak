package com.ncrossley.controlfreak;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * A 256 byte buffer for reading Breville Control Freak program files.
 * <p>
 * Control Freak program files consist of 8K bytes written out to a USB stick in 32 blocks of 256 bytes each.
 * Each 256 byte block contains 7 programs of 36 bytes each, plus a 4 byte filler.
 * The data is terminated at end of file, or with a block starting with 0xff.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public class CFInBuffer implements AutoCloseable
{
    private static final int BUFFER_LENGTH = 256;


    private InputStream	input;
    private byte[]		buffer = new byte[BUFFER_LENGTH];
    private int			limit = 0;
    private int			offset = BUFFER_LENGTH;


    /**
     * Construct a new CFInBuffer to read from a named file.
     * @param inputFile the file from which this CFInBuffer will read
     * @throws IOException if the file cannot be read
     */
    public CFInBuffer(String inputFile) throws IOException
    {
        input = Files.newInputStream(Paths.get(inputFile));
    }


    /**
     * Construct a new CFInBuffer to read from a given byte buffer.
     * @param inputBuffer the byte array from which this CFInBuffer will read
     */
    public CFInBuffer(byte[] inputBuffer)
    {
        input = new ByteArrayInputStream(inputBuffer);
    }


    private void fillBuffer() throws IOException, CFException
    {
        limit = input.read(buffer);
        offset = 0;

        if (limit >= 4)
        {
            for (int i=1; i<4; i++)
            {
                if (buffer[limit-i] != -1)
                {
                    throw new CFException("Unterminated buffer");
                }
            }
            limit -= 4;
        }
    }


    /**
     * Read a single program entry from the CFInBuffer.
     * @param entry the byte array into which the program entry is read
     * @return number of bytes read, or -1 if at end of file
     * @throws IOException if the input cannot be read
     * @throws CFException if the input is malformed
     */
    public int readEntry(byte[] entry) throws IOException, CFException
    {
        if (offset >= limit)
        {
            fillBuffer();
        }

        if (limit < 0 || buffer[offset] < 0)
        {
            return -1;
        }

        if (limit < entry.length)
        {
            throw new CFException(String.format("input file truncated - only %d bytes left",limit));
        }

        System.arraycopy(buffer, offset, entry, 0, entry.length);
        offset += entry.length;
        return entry.length;
    }


    @Override
    public void close() throws IOException
    {
        input.close();
    }
}
