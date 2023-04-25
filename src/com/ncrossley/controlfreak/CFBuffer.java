package com.ncrossley.controlfreak;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * A general Control Freak buffer class, used for either input or output.
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
//CSOFF: VisibilityModifier
public class CFBuffer
{
    /** The length of the Control Freak program & alarm file. */
    protected static final int	BUFFER_LENGTH	= 8192;

    /** The offset within the Control Freak file of the alarm area. */
    protected static final int	ALARM_OFFSET	= 4096;

    /** The length of each sub-block within the program and alarm file. */
    protected static final int	BLOCK_LENGTH	= 256;

    /** The buffer holding the Control Freak program and alarm data. */
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    protected byte[]			buffer			= new byte[BUFFER_LENGTH];

    /** Markers indicating where the next program goes in the buffer. */
    protected BufferControls	programControls;

    /** Markers indicating where the next alarm goes in the buffer. */
    protected BufferControls	alarmControls;


    /** Definition of markers indicating where entries are read from or written to the buffer. */
    protected static final class BufferControls
    {
        private int	offset;
        private int	limit;

        BufferControls(int offset, int limit)
        {
            this.offset = offset;
            this.limit = limit;
        }


        int getOffset()
        {
            return offset;
        }


        void setOffset(int offset)
        {
            this.offset = offset;
        }


        int getLimit()
        {
            return limit;
        }


        void setLimit(int limit)
        {
            this.limit = limit;
        }


        void incrementOffset(int i)
        {
            offset += i;
        }


        void incrementLimit(int i)
        {
            limit += i;
        }
    }


    /**
     * Construct a new CFBuffer.
     */
    public CFBuffer()
    {
        initControls();
    }


    /**
     * Initialise the buffer controls.
     */
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    private final void initControls()
    {
        programControls = new BufferControls(0, 0);
        alarmControls = new BufferControls(ALARM_OFFSET, ALARM_OFFSET);
    }


    /**
     * Set the buffer controls to indicate a buffer ready for reading or writing.
     */
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    protected final void markReady()
    {
        programControls.offset = 0;
        programControls.limit = BLOCK_LENGTH - 4;
        alarmControls.offset = ALARM_OFFSET;
        alarmControls.limit = ALARM_OFFSET + BLOCK_LENGTH - 4;
    }


    /**
     * Set the buffer controls to indicate an empty buffer for reading.
     */
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    protected final void markEmpty()
    {
        programControls.offset = 0;
        programControls.limit = 0;
        alarmControls.offset = ALARM_OFFSET;
        alarmControls.limit = ALARM_OFFSET;
    }
}
