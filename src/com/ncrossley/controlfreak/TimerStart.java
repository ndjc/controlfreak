package com.ncrossley.controlfreak;

import java.util.Locale;

/**
 * Describes when the timer starts.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
enum TimerStart
{
    /**
     * The timer starts immediately.
     */
    AtBeginning(0),

    /**
     * The timer starts when the set temperature is reached.
     */
    AtSetTemperature(1),

    /**
     * The timer starts when the user presses the button.
     */
    AtPrompt(2);


    private int value;


    /**
     * Construct a new TimerStart.
     * @param i the enum value
     */
    TimerStart(int i)
    {
        value = i;
    }


    /**
     * Find a TimerStart value.
     *
     * @param value the int value of the TimerStart
     * @return the enum value representing the given TimerStart value
     * @throws CFException if the int value does not correspond to a TimerStart value
     */
    public static TimerStart mkValue(int value) throws CFException
    {
        return switch (value)
        {
        case 0  -> AtBeginning;
        case 1  -> AtSetTemperature;
        case 2  -> AtPrompt;
        default -> throw new CFException(String.format("Invalid timer start value %d", value));
        };
    }




    /**
     * Find a TimerStart value from a string.
     * @param name the string name of the TimerStart
     * @return the enum value representing the given TimerStart value
     * @throws CFException if the string does not correspond to a TimerStart value
     */
    public static TimerStart mkValue(String name) throws CFException
    {
        return switch (name.toLowerCase(Locale.ENGLISH))
        {
        case "atsettemperature", "at set temperature","at set","at temperature","set" -> AtSetTemperature;
        case "atprompt", "at prompt","prompt"                                         -> AtPrompt;
        case "atbeginning","at beginning","beginning","immediately"                   -> AtBeginning;
        default -> throw new CFException(String.format("Invalid timer start %s", name));
        };
    }


    /**
     * Get the integer value for this TimerStart.
     * @return the integer value for this TimerStart
     */
    public int getValue()
    {
        return value;
    }
}
