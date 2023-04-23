package com.ncrossley.controlfreak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Main program to decode/encode/merge Breville Control Freak files.
 * Bytes 00-19 contain the null-terminated name.
 * Bytes 20-29 are unused.
 * Byte  30    corresponds to the temperature in Fahrenheit modulo 256
 * Byte  31    control byte
 * 			   bit 7: if set, add 255 to the temperature to (for temperatures from 256-482)
 *             bits 5-6: action after timer: Continue, Stop, Keep Warm, Repeat
 *             bit 4: unused
 *             bits 2-3: power level (Low, Medium, High, Max)
 *             bits 0-1: Timer start control (At Beginning, At Set Temperature, At Prompt)
 * Byte 32     corresponds to the hour timer value (0-71, or 72 if both minutes and seconds are 0)
 * Byte 33     corresponds to the minute timer value (0-59).
 * Byte 34     corresponds to the second timer value (0-59).
 * Byte 35     is a checksum of all previous bytes.
 *
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
public final class CFMain
{
    private Set<Program> programs;
    private String       outfile;
    private String       textfile;
    private boolean      sort = true;


    private CFMain()
    {
        programs = new LinkedHashSet<>();
    }


    /**
     * Reads Breville Control Freak programs, either from FA1 files or from a text representation.
     * Merges all such programs into alphabetical order of program name, eliminating duplicates.
     * Writes out a textual representation of the sorted list,
     * and optionally writes an FA1 file and/or a text file containg the textual representation.
     *
     * The text representation accepted for input is the same as presented on output - that is,
     * a sequence of lines, one program per line, with one of the two following formats:
     * program name | temperature in ºF | power level | hh:mm:ss | start control | after timer
     * program name | temperature in ºF | power level | 'off' or 'no timer'
     *
     * Power level is Low, Medium or High
     * Start control is At Beginning, At Set Temperature, or At Prompt
     * After timer control is Continue, Stop, Keep Warm, or Repeat
     *
     * @param args names of text or FA1 files to be read, and optionally output specs,
     * where -o|-output specifies the name of a binary output file,
     * and -t|-text specifies the name of a text output file.
     */
    @SuppressFBWarnings("IMC_IMMATURE_CLASS_PRINTSTACKTRACE")
    public static void main(String... args)
    {
        CFMain main = new CFMain();
        try
        {
            main.processArgs(args);
        }
        catch (IOException | CFException e)
        {
            e.printStackTrace();
        }
    }


    private void processArgs(String... args) throws IOException, CFException
    {
        int i = 0;
        while (i<args.length)
        {
            String arg = args[i];
            if (arg.equals("-o") || arg.equals("-output"))
            {
                // Processing directive for binary .FA1 output file

                if (++i < args.length)
                {
                    outfile = args[i];
                }
            }
            else if (arg.equals("-t") || arg.equals("-text"))
            {
                // Processing directive for human readable text output file

                if (++i < args.length)
                {
                    textfile = args[i];
                }
            }
            else if (arg.equals("-ns"))
            {
                // Processing directive to suppress sorting of programs

                sort = false;
            }
            else if (arg.endsWith(".FA1") || arg.endsWith(".fa1"))
            {
                // Binary .FA1 program input file

                try (CFInBuffer buffer = new CFInBuffer(arg))
                {
                    Program program;
                    while ((program = Program.readProgram(buffer)) != null)
                    {
                        if (!programs.add(program))
                        {
                            System.err.printf("Skipping duplicate program %s%n",program);
                        }
                    }
                }
            }
            else
            {
                // For all other arguments, try to parse a text input file

                List<String> allLines = Files.readAllLines(Paths.get(arg));

                for (String line : allLines)
                {
                    Program program = Program.mkProgram(line);
                    if (!programs.add(program))
                    {
                        System.err.printf("Skipping duplicate program %s%n",program);
                    }
                }
            }
            i++;
        }

        // Sort all programs if required
        if (sort)
        {
            programs = new TreeSet<>(programs);
        }

        printPrograms();
        writePrograms();
    }


    /**
     * Print a human-readable textual representation of the programs.
     * @throws FileNotFoundException if the output file cannot be created
     */
    private void printPrograms() throws FileNotFoundException
    {
        for (Program program : programs)
        {
            System.out.println(program);
        }

        if (textfile != null)
        {
            try (PrintStream ps = new PrintStream(textfile))
            {
                for (Program program : programs)
                {
                    ps.println(program);
                }
            }
        }
    }


    /**
     * Write the binary .FA1 file containing all input programs.
     * @throws IOException if the file cannot be written
     * @throws CFException if the programs cannot be converted to binary,
     * or if there are too many programs
     */
    private void writePrograms() throws IOException, CFException
    {
        if (outfile != null)
        {
            try (CFOutBuffer out = new CFOutBuffer(outfile))
            {
                for (Program program : programs)
                {
                    program.writeProgram(out);
                }
            }
        }
    }
}
