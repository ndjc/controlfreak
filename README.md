# Breville Control °Freak® Program Handler

This program reads and writes Breville Control Freak programs and saved alarms,
either from and to FA1 files or from and to a text representation.

The command line program reads a given set of files, merging all the
Control Freak programs and alarms into alphabetical order of name, eliminating duplicates.

The program displays on the standard output a textual representation of the sorted list,
first programs and then alarms,
and optionally writes an FA1 file and/or a text file containing the textual representation.

The text representation accepted for input is the same as presented on output - that is,
a sequence of lines, one program per line, with one of the three following formats:

>  program name | temperature in ºF | power level | hh:mm:ss | start control | after timer
>
>  program name | temperature in ºF | power level | 'off' or 'no timer'
>
> alarm name | temperature in ºF

where:

  * Power level is Low, Medium or High
  * Start control is At Beginning, At Set Temperature, or At Prompt
  * After timer control is Continue, Stop, Keep Warm, or Repeat

For example:

> Butter (Clarified)         | 240 |   Slow |  0:20:00 | At Set Temperature | Keep Warm<br>
> Carrots (Caramelize)       | 300 | Medium | no timer<br>
> Boiling | 212

The command line arguments are names of text or FA1 files to be read,
and optionally output specs:

  * `-o|-output filename` specifies the name of a binary output file
  * `-t|-text filename`   specifies the name of a text output file
  * `-ns`                do not sort the programs or alarms (but duplicates are still removed, and alarms written after programs)

