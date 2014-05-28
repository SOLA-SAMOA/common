/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.common.logging;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;
import org.sola.common.DateUtility;

/**
 * Uses Java Utility Logging to log details to the SOLA Log file. The log file is located at
 * <user.home>/sola/logs. The name of the log file is <yyyyMMdd>.log or 
 * <mainApplicationClassName>_<yyyyMMdd>.log if a mainApplicationClass is specified using the
 * {@linkplain #initialize} method. The LogUtility will also record user preferences for the log 
 * level if the mainApplciationClass is specified. 
 * <p>
 * LogUtility should be used for client side or common logging. For service side logging, use 
 * {@linkplain org.sola.services.logging.LogUtility}. </p>
 * @author soladev
 */
public final class LogUtility {

    public static final String SOLA_COMMON_LOGGER = "org.sola.common";
    private static final String LOG_LEVEL_PREFERENCE = "logLevel";
    private final static Logger solaLog = Logger.getLogger(SOLA_COMMON_LOGGER);
    private static Class<?> mainClass = null;
    private static boolean logConfigured = false;

    /**
     * Configures a logger for the SOLA Log file. The SOLA Log file is located at
     * <user.home>/sola/logs. The default name for the log file is <yyyyMMdd>.log. If 
     * a mainApplicationClass is identified for the LogUtilty (using the {@linkplain #initialize} method), 
     * the name for the log file is <mainApplicationClassName>_<yyyyMMdd>.log
     */
    private static void configure() {

        String DATE_FORMAT_NOW = "yyyyMMdd";
        String formattedDate = DateUtility.simpleFormat(DATE_FORMAT_NOW);
        String logFileDir = System.getProperty("user.home") + "/sola/logs/";
        String logFile = logFileDir + formattedDate + ".log";
        if (mainClass != null) {
            logFile = logFileDir + mainClass.getSimpleName() + "_" + formattedDate + ".log";
        }

        try {
            // Check if the directory to hold the log file exists. If not, create it. 
            // Unfortuanately the FileHander class will fail with a Unable to obtain Lock error
            // if the directory does not already exist. 
            File d = new File(logFileDir);
            if (!d.exists()) {
                d.mkdirs();
            }
            FileHandler fh = new FileHandler(logFile, true);
            fh.setFormatter(new SimpleFormatter());
            solaLog.addHandler(fh);

            // Determine the appropriate level to set for the Logger by checking if the user
            // has any level preferences. 
            Level levelPref = Level.INFO;
            if (mainClass != null) {
                Preferences prefs = Preferences.userNodeForPackage(mainClass);
                String lev = prefs.get(LOG_LEVEL_PREFERENCE, Level.INFO.toString());
                levelPref = Level.parse(lev);
            }

            // Only log the config message if the log is turned on. 
            if (!levelPref.equals(Level.OFF)) {
                solaLog.setLevel(Level.INFO);
                solaLog.log(Level.INFO, "*** SOLA Log Configured ***");
            }
            solaLog.setLevel(levelPref);

        } catch (Exception ex) {
            // Output a println message with the eror as an exception may cause an inifinte loop
            // when an attempt is made to log it. 
            System.err.println("Unable to initalize logging. Error: " + getStackTraceAsString(ex));
        }
        logConfigured = true;
    }

    /**
     * Helper function that configures the sola log before returning it 
     * @return 
     */
    private static Logger getSolaLog() {
        if (!logConfigured) {
            configure();
        }
        return solaLog;
    }

    /**
     * Logs an INFO level message to the SOLA Log file.
     * @param msg 
     */
    public static void log(String msg) {
        log(msg, Level.INFO);
    }

    /**
     * Logs a message with the specified log level to the SOLA Log File. Note that by default
     * only INFO, WARNING and SEVERE messages will be logged. To log messages of lower level
     * set the log level using the setLogLevel method first. 
     * @param msg
     * @param level 
     */
    public static void log(String msg, Level level) {
        Logger logger = getSolaLog();
        if (logger != null) {
            logger.log(level, msg);
        }
    }

    /**
     * Logs a message along with the stack trace details from the exception as a SEVERE message. 
     * @param msg
     * @param ex 
     */
    public static void log(String msg, Exception ex) {
        msg = msg + System.getProperty("line.separator") + getStackTraceAsString(ex);
        log(msg, Level.SEVERE);
    }

    /**
     * Sets the log level to the specified value and logs a message into the log file indicating
     * the log level has changed. Also configures the new log level as a user preference if a 
     * mainApplicationClass has been setup using the {@linkplain #initialize} method. 
     * @param logLevel 
     */
    public static void setLogLevel(Level logLevel) {
        Logger logger = getSolaLog();
        if (logger != null && !logger.getLevel().equals(logLevel)) {
            // Set the log level to INFO and log an info message that the log level is about
            // to change. 
            logger.setLevel(Level.INFO);
            log("*** " + SOLA_COMMON_LOGGER + " log level set to " + logLevel.toString() + " ***");
            logger.setLevel(logLevel);
            if (mainClass != null) {
                Preferences prefs = Preferences.userNodeForPackage(mainClass);
                prefs.put(LOG_LEVEL_PREFERENCE, logLevel.toString());
            }
        }
    }

    /**
     * Retrieves the current log level
     * @return 
     */
    public static Level getLogLevel() {
        Logger logger = getSolaLog();
        Level logLevel = null;
        if (logger != null) {
            logLevel = logger.getLevel();
        }
        return logLevel;
    }

    /**
     * The main application class is used to control the name of the log file as well as the
     * the location to store user preferences for the log level.
     * @param mainLoggerClass The main application class
     */
    public static void initialize(Class<?> mainApplicationClass) {
        mainClass = mainApplicationClass;
    }

    /**
     * Formats the stacktrace for an exception into a string
     * @param t The throwable exception
     * @return The stacktrace of the exception formatted as a string
     */
    public static String getStackTraceAsString(Exception ex) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        return result.toString();
    }
}
