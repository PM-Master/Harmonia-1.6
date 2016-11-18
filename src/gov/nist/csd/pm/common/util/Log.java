package gov.nist.csd.pm.common.util;

/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This class implements a simple general purpose logger that shows the
 * filename and line number for each log entry as well as date/time and 
 * DEBUG/INFO/WARNING/ERROR enumeration. It is similar to
 * other loggers like Log4J but much simpler to use and configure.
 * 
 * @author steveq@nist.gov
 */
public class Log {

	private SimpleDateFormat format = null;
	private FileWriter writer = null;
	private boolean logToConsole = false;
	private Level userLevel = null;
	private File logFile = null;
	private boolean isClosed = true;

	public enum Level {

		DEBUG(0), INFO(1), WARNING(2), ERROR(3);

		private int priority;

		private Level(int priority) {
			this.priority = priority;
		}

		private int getPriority() {
			return priority;
		}
	}

	public synchronized static String formatElapsed(long millis) {
		final long hr = TimeUnit.MILLISECONDS.toHours(millis);
		final long min = TimeUnit.MILLISECONDS.toMinutes(millis
				- TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(millis
				- TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(millis
				- TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
				- TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}
	
	public Log(Log.Level userLevel, boolean logToConsole) {
		this.userLevel = userLevel;
		this.logToConsole = logToConsole;
		isClosed = false;
		format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ");
	}

	public Log(String logFilePath, Log.Level userLevel, boolean logToConsole) {
		if (logFilePath != null) {
			try {
				logFile = new File(logFilePath);
				if (!logFile.exists()) {
					logFile.createNewFile();
				} 
				writer = new FileWriter(logFile, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// TODO: An exception should be thrown here.
			System.err.println("Log file path is null");
		}
		this.userLevel = userLevel;
		this.logToConsole = logToConsole;
		isClosed = false;
		format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ");
	}

	public synchronized void close() {
		if (isClosed) {
			return;
		}
		try {
			writer.close();
			isClosed = true;
		} catch (final IOException e) {
			System.err.println("Error trying to close app logger: "
					+ e.getMessage());
			isClosed = false;
		}
	}
	
	public synchronized void debugStackCall(String message) {
		writeStackCall(Level.DEBUG, message);
	}

	public synchronized void debug(String message) {
		writeMessage(Level.DEBUG, message, 2);
	}
	
	public synchronized void debug(String message, int stackPosition) {
		writeMessage(Level.DEBUG, message, stackPosition);
	}

	public synchronized void error(String message) {
		writeMessage(Level.ERROR, message, 2);
	}
	
	public synchronized void error(String message, int stackPosition) {
		writeMessage(Level.ERROR, message, stackPosition);
	}

	public synchronized void info(String message) {
		writeMessage(Level.INFO, message, 2);
	}
	
	public synchronized void info(String message, int stackPosition) {
		writeMessage(Level.INFO, message, stackPosition);
	}

	public synchronized void warn(String message) {
		writeMessage(Level.WARNING, message, 2);
	}
	
	public synchronized void warn(String message, int stackPosition) {
		writeMessage(Level.WARNING, message, stackPosition);
	}

	public synchronized boolean isClosed() {
		return isClosed;
	}

	/** Write the log message.
	 * 
	 * @param level
	 * @param message
	 * @param stackPosition The calling class/method position in the stack trace. A 
	 * stackPosition of 0 is class/method where the write method is called
	 * below. A stack position of 2 is the class/method where Log.writeMessage()
	 * is called. The higher the stack position, the further away the calling
	 * class/method from this writeMessage() method.
	 */
	private synchronized void writeMessage(Level level, String message,
			int stackPosition) {
		Date date = null;
		String formattedDate = null;
		String logData = null;
		if (level.getPriority() >= userLevel.getPriority()) {
			date = new Date();
			formattedDate = format.format(date);
			StackTraceElement stackTraceElement = new Exception().getStackTrace()[stackPosition];
			logData = stackTraceElement.getClassName() + "/" + stackTraceElement.getMethodName() + ":" + stackTraceElement.getLineNumber();

			if (writer != null) {
				try {
					writer.write(formattedDate + " " + logData + "\n");
					writer.write("[" + level.name() + "] " + message + "\n");
					writer.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (logToConsole) {
				if (level == Level.ERROR) {
					System.err.print(formattedDate + " " + logData + "\n");
					System.err.print("[" + level.name() + "] " + message + "\n");
				} else {
					System.out.print(formattedDate + " " + logData + "\n");
					System.out.print("[" + level.name() + "] " + message + "\n");
				}
			}
		}

		logData = null;
		formattedDate = null;
		date = null;
	}
	
	
	private synchronized void writeStackCall(Level level, String message) {
		Date date = null;
		String formattedDate = null;
		String logData = "";
		if (level.getPriority() >= userLevel.getPriority()) {
			date = new Date();
			formattedDate = format.format(date);
			StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
			// Skip stack elements for this method and class (so start at 2)
			for (int i = 2; i < stackTraceElements.length; i++) {
				StackTraceElement stackTraceElement = stackTraceElements[i];
				logData += stackTraceElement.getClassName() + "/" + stackTraceElement.getMethodName() + ":" + stackTraceElement.getLineNumber() + "\n";
			}

			if (writer != null) {
				try {
					writer.write("----------- TRACE START -----------");
					writer.write("[" + level.name() + "] " + message + ":\n");
					writer.write(formattedDate + " " + logData + "\n");
					writer.write("----------- TRACE END -----------");
					writer.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (logToConsole) {
				if (level == Level.ERROR) {
					System.err.println("----------- TRACE START -----------");
					System.err.print("[" + level.name() + "] " + message + "\n");
					System.err.print(formattedDate + " " + logData + "\n");
					System.err.println("----------- TRACE END -----------");
				} else {
					System.out.println("----------- TRACE START -----------");
					System.out.print("[" + level.name() + "] " + message + "\n");
					System.out.print(formattedDate + " " + logData + "\n");
					System.out.println("----------- TRACE END -----------");
				}
			}
		}

		logData = null;
		formattedDate = null;
		date = null;
	}
}
