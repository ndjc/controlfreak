package com.ncrossley.controlfreak;

import java.util.Locale;

/**
 * Describes the action taken by the Control Freak after the timer expires.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
enum AfterTimer
{
    ContinueCooking(0),

    StopCooking(1),

    KeepWarm(2),

    RepeatTimer(3);


    private int value;


    /**
     * Construct a new AfterTimer.
     * @param i the enum value
     */
    AfterTimer(int i)
    {
        value = i;
    }


    /**
     * Find an AfterTimer value.
     * @param value the int value of the AfterTimer
     * @return the enum value representing the given AfterTimer value
     * @throws CFException if the int value does not correspond to an AfterTimer value
     */
    public static AfterTimer mkValue(int value) throws CFException
    {
        return switch (value)
        {
        case 0  -> ContinueCooking;
        case 1  -> StopCooking;
        case 2  -> KeepWarm;
        case 3  -> RepeatTimer;
        default -> throw new CFException(String.format("Invalid after timer value %d", value));
        };
    }


    /**
     * Find an AfterTimer value from a string.
     * @param name the string name of the AfterTimer
     * @return the enum value representing the given AfterTimer value
     * @throws CFException if the string does not correspond to a AfterTimer value
     */
    public static AfterTimer mkValue(String name) throws CFException
    {
        return switch (name.toLowerCase(Locale.ENGLISH))
        {
        case "continuecooking", "continue cooking","continue" -> ContinueCooking;
        case "stopcooking", "stop cooking","stop"             -> StopCooking;
        case "keepwarm","keep warm","keep","warm"             -> KeepWarm;
        case "repeattimer","repeat timer","repeat"            -> RepeatTimer;
        default -> throw new CFException(String.format("Invalid after timer %s", name));
        };
    }



    /**
     * Get the integer value for this AfterTimer.
     * @return the integer value for this AfterTimer
     */
    public int getValue()
    {
        return value;
    }
}
