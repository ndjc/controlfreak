package com.ncrossley.controlfreak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Main program to decode/encode/merge Breville Control Freak files.
 *
 * Copyright (c) 2023 Nick Crossley. Licensed under the MIT license - see
 * LICENSE.txt.
 */
public final class CFMain
{
	private static final int	MAX_PROGRAMS	= 80;

	private Set<Program>		programs;
	private Set<Alarm>			alarms;
	private String				outfile;
	private String				textfile;
	private boolean				sort			= true;


	private CFMain()
	{
		programs = new LinkedHashSet<>();
		alarms = new LinkedHashSet<>();
	}


	/**
	 * Reads Breville Control Freak programs and alarms, either from FA1 files
	 * or from a text representation. Merges all such programs into alphabetical
	 * order of program name, eliminating duplicates. Writes out a textual
	 * representation of the sorted list, and optionally writes an FA1 file
	 * and/or a text file containing the textual representation.
	 *
	 * The text representation accepted for input is the same as presented on
	 * output - that is, a sequence of lines, one program or alarm per line,
	 * with one of the three following formats: program name | temperature in ºF
	 * | power level | hh:mm:ss | start control | after timer program name |
	 * temperature in ºF | power level | 'off' or 'no timer' alarm name |
	 * temperature in ºF
	 *
	 * Power level is Low, Medium or High Start control is At Beginning, At Set
	 * Temperature, or At Prompt After timer control is Continue, Stop, Keep
	 * Warm, or Repeat
	 *
	 * @param args names of text or FA1 files to be read, and optionally output
	 * specs, where -o|-output specifies the name of a binary output file, and
	 * -t|-text specifies the name of a text output file.
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
		boolean textOutProvided = false;
		boolean binOutProvided  = false;

		while (i < args.length)
		{
			String arg = args[i];
			if (arg.equals("-o") || arg.equals("-output"))
			{
				// Processing directive for binary .FA1 output file

				if (++i < args.length)
				{
					if (binOutProvided)
					{
						System.err.printf("Ignoring duplicate binary output spec %s%n", args[i]);
					}
					else
					{
						outfile = args[i];
						binOutProvided = true;
					}
				}
			}
			else if (arg.equals("-t") || arg.equals("-text"))
			{
				// Processing directive for human readable text output file

				if (++i < args.length)
				{
					if (textOutProvided)
					{
						System.err.printf("Ignoring duplicate text output spec %s%n", args[i]);
					}
					else
					{
						textfile = args[i];
						textOutProvided = true;
					}
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
							System.err.printf("Skipping duplicate program %s%n", program);
						}
					}

					Alarm alarm;
					while ((alarm = Alarm.readAlarm(buffer)) != null)
					{
						if (!alarms.add(alarm))
						{
							System.err.printf("Skipping duplicate alarm %s%n", alarm);
						}
					}
				}
			}
			else
			{
				// For all other arguments, try to parse a text input file

				List<String> allLines = Files.readAllLines(Paths.get(arg));

				Pattern p = Pattern.compile(".*\\|.*\\|.*");
				for (String line : allLines)
				{
					String trimmedLine = line.trim();

					if (trimmedLine.isEmpty())
					{
						// Skip blank lines
					}
					else if (p.matcher(trimmedLine).matches())
					{
						// A line with at least two vertical bars must be a
						// program
						Program program = Program.mkProgram(line);
						if (!programs.add(program))
						{
							System.err.printf("Skipping duplicate program %s%n", program);
						}
					}
					else
					{
						// Otherwise, try an alarm
						Alarm alarm = Alarm.mkAlarm(trimmedLine);
						if (!alarms.add(alarm))
						{
							System.err.printf("Skipping duplicate alarm %s%n", alarm);
						}
					}
				}
			}
			i++;
		}

		// Sort all programs if required
		if (sort)
		{
			programs = new TreeSet<>(programs);
			alarms = new TreeSet<>(alarms);
		}

		if (programs.isEmpty() && alarms.isEmpty())
		{
			System.err.println("No input programs or alarms specified, so no output has been written");
		}
		else
		{
			printAll();
			if (programs.size() > MAX_PROGRAMS || alarms.size() > MAX_PROGRAMS)
			{
				System.err.printf("Too many programs or alarms: "
					+ "you have %d programs and %d alarms, but the device can only take %d of either%n",
					programs.size(), alarms.size(), MAX_PROGRAMS);
				if (outfile != null)
				{
					System.err.println("No binary file has been written");
				}
			}
			else
			{
				writeAll();
			}
		}
	}


	/**
	 * Print a human-readable textual representation of the programs and alarms.
	 *
	 * @throws FileNotFoundException if the output file cannot be created
	 */
	private void printAll() throws FileNotFoundException
	{
		printer(System.out);

		if (textfile != null)
		{
			try (PrintStream ps = new PrintStream(textfile))
			{
				printer(ps);
			}
		}
	}


	private void printer(PrintStream printStream)
	{
		for (Program program : programs)
		{
			printStream.println(program);
		}

		if (!programs.isEmpty() && !alarms.isEmpty())
		{
			printStream.println();
		}

		for (Alarm alarm : alarms)
		{
			printStream.println(alarm);
		}
	}


	/**
	 * Write the binary .FA1 file containing all input programs.
	 *
	 * @throws IOException if the file cannot be written
	 * @throws CFException if the programs cannot be converted to binary, or if
	 * there are too many programs
	 */
	private void writeAll() throws IOException, CFException
	{
		if (outfile != null)
		{
			try (CFOutBuffer out = new CFOutBuffer(outfile))
			{
				for (Program program : programs)
				{
					program.writeProgram(out);
				}
				for (Alarm alarm : alarms)
				{
					alarm.writeAlarm(out);
				}
			}
		}
	}
}
