# Lyubishchev Time Statistic and Analysis Program

This project is able to help you to static and analysis your Lyubishchev time management table, and generate a report in form of markdown.



# How does the program go

1. Main program loads configuration from root folder
2. Main program loads report template from _Template/Report_ according to configuration
3. Main program scans all resouce placeholders in template
4. Main program loads all resources from _Resources/${satistics_period}_ and replace all resource placeholders
5. Main program scans replaced document for processing marks
6. Main program runs for processors one by one according to processing marks
7. Processors are using time table locating in _TimeTable/${statistic_period}_
8. Main program collects all results from processors
9. Main program replace all processing marks with results collected
10. Main program writes final document into _Reports/${statistic_period}_ folder

# Program Structure

### main.py

Main program, all processing steps will go through within it.

### configure.ini

All parameters and configurations are coded in.

### README.md

This document you are reading now.

### _processors folder

All processors that process data and other necessary steps.

### Reports folder

Where final report output

### Resources folder

Where the resources you will need. By default, there is only text replacements. However, you can edit the program and set up resources of your own.

### Templates folder

For storing templates of report, timetable and so on.

### TimeTable folder

Where your time records are stored.

