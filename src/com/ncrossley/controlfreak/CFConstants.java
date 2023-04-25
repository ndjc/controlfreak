package com.ncrossley.controlfreak;

/**
 * This class holds various constants used by the Control Freak Handler.
 *
 * @author ndjc Copyright (c) 2023 Nick Crossley. Licensed under the MIT license
 * - see LICENSE.txt.
 */
public final class CFConstants
{
    /** The length of an entry in the binary file. */
    public static final int	ENTRY_LENGTH		= 36;

    /**
     * The maximum number of each type of entry (program or alarm) in a single
     * file.
     */
    public static final int	MAX_ENTRIES			= 16 * 7;


    /** The maximum length of an alarm name. */
    public static final int	ALARM_NAME_LENGTH	= 20;

    /** The maximum length of a program name. */
    public static final int	PROGRAM_NAME_LENGTH	= 26;

    /**
     * The minimum temperature supported by the Breville Control Freak.
     */
    public static final int	TEMP_MIN			= 0;

    /**
     * The maximum temperature supported by the Breville Control Freak.
     */
    public static final int	TEMP_MAX			= 482;

    /**
     * The longest cooking time supported by the Breville Control Freak.
     */
    public static final int	MAX_HOURS			= 72;

    // Constants exposed to test code
    static final int		TEMP_MOD			= 255;


    private CFConstants()
    {
        // No instantiation
    }
}
