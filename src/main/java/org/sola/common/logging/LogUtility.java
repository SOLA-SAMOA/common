/**
 * ******************************************************************************************
 * Copyright (C) 2011 - Food and Agriculture Organization of the United Nations (FAO).
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.sola.common.SOLAException;

/**
 *
 * @author dounnaah
 */
public final class LogUtility {

    private static Logger solaLog = null; 

    /**
     * Log files will be found in the working directory. 
     * For Glassfish this is the .../glassfish/domains/<domain name>/logs/sola_<date>.log
     * file. For the desktop client this is ...?
     */
    private static void configure() {

        
        String DATE_FORMAT_NOW = "yyyyMMdd";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String logName = "sola_" + sdf.format(cal.getTime()) + ".log";
        try{
        Appender a = new FileAppender(new PatternLayout("%-5p %d  %m%n"),
                System.getProperty("user.dir") + "/../logs/" + logName);

        BasicConfigurator.configure(a);
        solaLog = Logger.getLogger(LogUtility.class.getName());
        solaLog.setLevel(org.apache.log4j.Level.INFO);
        }
        catch(Exception ex)
        {
            // Throw a SOLA Runtime exception
          throw new SOLAException("Log Exception", ex);
        }
    }

    private static Logger getSolaLog() {
        if (solaLog == null) {
            configure();
        }
        return solaLog;
    }

    public static void log(String msg) {
        log(msg, Level.INFO);
    }

    public static void log(String msg, Level level) {
        Logger logger = getSolaLog();
        if (level == Level.FINER || level == Level.FINEST) {
            logger.trace(msg);
        } else if (level == Level.FINE) {
            logger.debug(msg);
        } else if (level == Level.INFO || level == Level.CONFIG) {
            logger.info(msg);
        } else if (level == Level.WARNING) {
            logger.warn(msg);
        } else if (level == Level.SEVERE) {
            logger.error(msg);
        } else {
            logger.info(msg);
        }
    }
    
    public static void log(String msg, Exception ex){
        getSolaLog().error(msg, ex);
        System.out.println("Error:\n" + msg);
        ex.printStackTrace();
    }
}
