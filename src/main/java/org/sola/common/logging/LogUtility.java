/**
 * ******************************************************************************************
 * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
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
import org.sola.common.DateUtility;

/**
 * Uses Java Util Logging to log details to the SOLA Log file. This 
 * LogUtility should be used for client side or common logging. For service side logging, use 
 * {@linkplain org.sola.services.logging.LogUtility}. 
 * @author soladev
 */
public final class LogUtility {

    private final static Logger solaLog = Logger.getLogger("org.sola.common");
    private static boolean logConfigured = false;

    /**
     * Configures a logger for the SOLA Log file. The SOLA Log file is located at
     * <user.home>/sola/logs. 
     */
    private static void configure() {

        String DATE_FORMAT_NOW = "yyyyMMdd";
        String formattedDate = DateUtility.simpleFormat(DATE_FORMAT_NOW);
        String logFileDir = System.getProperty("user.home") + "/sola/logs/";
        String logFile = logFileDir + formattedDate + ".log";

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
            // Modify the log level if required for debugging purposes. 
            solaLog.setLevel(Level.INFO);
            solaLog.log(Level.INFO, "*** SOLA Log Configured ***"); 

        } catch (Exception ex) {
            // Output a println message with the eror as an exception may cause an inifinte loop
            // when an attempt is made to log it. 
            System.err.println("Unable to initalize logging. Error: " + getStackTraceAsString(ex));
        }
        logConfigured = true;
    }

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
     * update the configure method. 
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
