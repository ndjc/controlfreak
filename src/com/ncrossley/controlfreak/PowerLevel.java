package com.ncrossley.controlfreak;

import java.util.Locale;

/**
 * Describes the heating power level of the Control Freak.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
enum PowerLevel
{
    Slow(0),
    Medium(1),
    Fast(2),
    Max(3);


    private int value;


    /**
     * Construct a new PowerLevel.
     * @param i the enum value
     */
    PowerLevel(int i)
    {
        value = i;
    }


    /**
     * Find a PowerLevel value from an integer.
     * @param value the int value of the PowerLevel
     * @return the enum value representing the given PowerLevel value
     * @throws CFException if the int value does not correspond to a PowerLevel value
     */
    public static PowerLevel mkValue(int value) throws CFException
    {
        return switch (value)
        {
        case 0  -> Slow;
        case 1  -> Medium;
        case 2  -> Fast;
        case 3  -> Max;
        default -> throw new CFException(String.format("Invalid power level value %d", value));
        };
    }


    /**
     * Find a PowerLevel value from a string.
     * @param name the string name of the PowerLevel
     * @return the enum value representing the given PowerLevel value
     * @throws CFException if the string does not correspond to a power level
     */
    public static PowerLevel mkValue(String name) throws CFException
    {
        return switch (name.toLowerCase(Locale.ENGLISH))
        {
        case "medium"      -> Medium;
        case "high","fast" -> Fast;
        case "max"         -> Max;
        case "low","slow"  -> Slow;
        default -> throw new CFException(String.format("Invalid power level %s", name));
        };
    }


    /**
     * Get the integer value for this PowerLevel.
     * @return the integer value for this PowerLevel
     */
    public int getValue()
    {
        return value;
    }
}
