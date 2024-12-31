package com.ncrossley.controlfreak;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public final class CFInBuffer extends CFBuffer implements AutoCloseable
{
    private InputStream	input;


    /**
     * Construct a new CFInBuffer to read from a named file.
     * @param inputFile the file from which this CFInBuffer will read
     * @throws IOException if the file cannot be read
     */
    public  CFInBuffer(String inputFile) throws IOException
    {
        input = Files.newInputStream(Paths.get(inputFile));
        markEmpty();
    }


    /**
     * Construct a new CFInBuffer to read from a given byte buffer.
     * @param inputBuffer the byte array from which this CFInBuffer will read
     */
    public CFInBuffer(byte[] inputBuffer)
    {
        input = new ByteArrayInputStream(inputBuffer);
        markEmpty();
    }


    private void fillBuffer() throws IOException, CFException
    {
        int len = input.read(buffer);
        if  (len != BUFFER_LENGTH)
        {
            throw new CFException(
                String.format(".FA1 file must be exactly %d bytes long",BUFFER_LENGTH));
        }
        markReady();
}


    /**
     * Read a single program or alarm entry from the CFInBuffer.
     * @param entryType the type of the entry to be read
     * @return the entry read, or null if at end of file
     * @throws IOException if the input cannot be read
     * @throws CFException if the input is malformed
     */
    public Entry readEntry(EntryType entryType) throws IOException, CFException
    {
        BufferControls controls = entryType ==  EntryType.ProgramEntry ? programControls : alarmControls;
        if (controls.getOffset() >= controls.getLimit())
        {
            fillBuffer();
        }
        if (controls.getOffset() >= BUFFER_LENGTH || buffer[controls.getOffset()] < 0)
        {
            return null;
        }

        Entry entry = new Entry(entryType,
            Arrays.copyOfRange(buffer,controls.getOffset(),controls.getOffset()+CFConstants.ENTRY_LENGTH));
        controls.incrementOffset(CFConstants.ENTRY_LENGTH);

        // Skip over block end padding
        if (controls.getOffset() + CFConstants.ENTRY_LENGTH > controls.getLimit())
        {
            controls.incrementOffset(4);
            controls.incrementLimit(BLOCK_LENGTH);
        }

        return entry;
    }


    @Override
    public void close() throws IOException
    {
        input.close();
    }
}
