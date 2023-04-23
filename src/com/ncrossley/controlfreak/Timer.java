package com.ncrossley.controlfreak;

import java.util.Scanner;

/**
 * The cook time for a Control Freak program.
 * The range of the overall timer is from 1 second to 72 hours, 0 minutes, 0 seconds.
 * @param hours the number of hours for which the timer runs (0-72)
 * @param minutes the number of minutes for which the timer runs (0-59)
 * @param seconds the number of seconds for which the timer runs (0-59)
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
record Timer(int hours, int minutes, int seconds) implements Comparable<Timer>
{
    /**
     * Validate fields for new Timer.
     * @param hours hours, 0 to 72
     * @param minutes minutes, 0 to 59
     * @param seconds seconds, 0 to 59
     */
    Timer(int hours, int minutes, int seconds)
    {
        if (hours < 0 || hours > 72
                || (hours == 72 && (minutes != 0 || seconds != 0))
                || minutes < 0 || minutes > 59
                || seconds < 0 || seconds > 59)
        {
            throw new IllegalArgumentException("timer out of range");
        }
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }


    /**
     * Is the timer off?
     * @return true iff the timer is off
     */
    public boolean isOff()
    {
        return hours == 0 && minutes == 0 && seconds == 0;
    }


    @Override
    public String toString()
    {
        return isOff() ? "off" : String.format("%2d:%02d:%02d", hours, minutes, seconds);
    }


    /**
     * Parse a time string into a Timer.
     * @param s the time string to be parsed
     * @return the Timer value
     */
    public static Timer mkValue(String s)
    {
        if (s.equals("off") || s.equals("no timer"))
        {
            return new Timer(0,0,0);
        }
        else
        {
            try (Scanner scanner = new Scanner(s))
            {
                scanner.useDelimiter(":");
                return new Timer(scanner.nextInt(),scanner.nextInt(),scanner.nextInt());
            }
        }
    }


    @Override
    public int compareTo(Timer o)
    {
        int c;
        if ((c=Integer.compare(hours,o.hours)) != 0)
        {
            return c;
        }
        else if ((c=Integer.compare(minutes,o.minutes)) != 0)
        {
            return c;
        }
        else
        {
            return Integer.compare(seconds,o.seconds);
        }
    }
}
