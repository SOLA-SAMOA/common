/**
 * ******************************************************************************************
 * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
 * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import javax.swing.text.NumberFormatter;

public class NumberUtility {

    /**
     * Returns the rounded double number with a given precision.
     *
     * @param number Number to round
     * @param precision The precision of rounding
     * @return Rounded double with a given precision.
     */
    public static double roundDouble(double number, int precision) {
        int temp = (int) ((number * Math.pow(10, precision)));
        return (((double) temp) / Math.pow(10, precision));
    }

    /**
     * Formats a BigDecimal value representing an area in square metres into a string indicating the
     * area as metres squared (where area is less that 10,000) or hectares (where area is >= 10,000
     *
     * @param area The area in metres to format
     * @return The formated area or null if area is null
     */
    public static String formatAreaMetric(BigDecimal area) {
        String result = null;
        if (area != null && BigDecimal.ZERO.compareTo(area) < 0) {
            NumberFormatter areaFormatter = new NumberFormatter(DecimalFormat.getNumberInstance());
            try {
                if (area.compareTo(new BigDecimal("10000")) >= 0) {
                    // The area is >= 10,000 so format as hectares
                    area = area.divide(new BigDecimal("10000"));
                    area.setScale(3, RoundingMode.DOWN);
                    result = System.lineSeparator() + areaFormatter.valueToString(area) + "ha";
                } else {
                    // Format the area has metres squared
                    result = areaFormatter.valueToString(area) + "m" + (char) 178; // Superscript 2
                }
            } catch (ParseException psex) {
            }
        }
        return result;
    }

    /**
     * Formats a BigDecimal value representing an area in square meters an imperial area (i.e.
     * acres, roods and perches.
     *
     * @param area The area in meters to format
     * @return The formated area or null if area is null
     */
    public static String formatAreaImperial(BigDecimal areaDec) {
        String result = null;
        if (areaDec != null && BigDecimal.ZERO.compareTo(areaDec) < 0) {
            // Use a Double as the division math for BigDecimal is unreliable
            result = "";
            Double area = areaDec.doubleValue();
            NumberFormatter areaFormatter = new NumberFormatter(DecimalFormat.getNumberInstance());
            try {
                // 1a = 4046.8564 square metres
                Double acresTmp = new Double(area / 4046.8564);
                int acres = acresTmp.intValue();
                Double remainder = acresTmp - acres;
                // 4 roods to an acre
                Double roodsTmp = new Double(remainder * 4);
                int roods = roodsTmp.intValue();
                remainder = roodsTmp - roods;
                // 40 perches to a rood
                Double perches = remainder * 40;
                if (acres >= 1) {
                    result = areaFormatter.valueToString(acres) + "a";
                }
                if (acres >= 1 || roods >= 1) {
                    DecimalFormat f = new DecimalFormat("0r");
                    result = result + " " + f.format(roods);
                }
                DecimalFormat p = new DecimalFormat("00.0p");
                result = result + " " + p.format(perches);
                result.trim();

            } catch (ParseException psex) {
            }
        }
        return result;
    }
}
