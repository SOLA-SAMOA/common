/**
 * ******************************************************************************************
 * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
 * (FAO). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.common;

/**
 * Holds the list of constants that map to the configuration settings in the
 * system.setting table
 */
public class ConfigConstants {

    /**
     * Number of days before a users password expires. Calculated from the last
     * date the user changed their password. Recommended values are 60, 90 or
     * 180. If NULL (or settings is disabled), then no password expiry applies.
     * Default is 90 days. SOLA will prompt the user to change their password if
     * it is within 14 days of expiry so it is recommended to set it to a value
     * > 14. Changes to this value will be recognized immediately if the
     * SolaRealm in Glassfish is setup to use the system.active_users view for
     * username details. This setting value is used directly by the
     * system.user_pword_expiry view (and indirectly by the system.active_users
     * view).
     */
    public static final String PWORD_EXPIRY_DAYS = "pword-expiry-days";
    /**
     * tax-rate - The tax rate to use for financial calculations. Changes to
     * this setting will have immediate effect. Default 0.075.
     */
    public static final String TAX_RATE = "tax-rate";
    /**
     * network-scan-folder - The network folder location used to store scanned
     * images. Used by the Digital Archive Service to display new scanned
     * documents for attachment to applications. Also used by the
     * SharedFolderCleaner service to remove images from the scan folder. The
     * SOLA Services must be restarted before changes to this value take effect.
     * Default is <user home>/sola/scan
     */
    public static final String NETWORK_SCAN_FOLDER = "network-scan-folder";
    /**
     * clean-network-scan-folder - Flag (Y or N) to indicate if the network scan
     * folder should be periodically cleaned of old files (Y) or not (N). The
     * lifetime of the files in the scan folder can also be set. Changes to this
     * value will be detected at the next scheduled run of the
     * SharedFolderCleaner service (i.e. within 60 minutes). Default N. Used by
     * the SharedFolderCleaner service.
     */
    public static final String CLEAN_NETWORK_SCAN_FOLDER = "clean-network-scan-folder";
    /**
     * scanned-file-lifetime - The length of time in hours a file will be left
     * in the network scan folder before it is deleted by the
     * SharedFolderCleaner service. Note that the CLEAN_NETWORK_SCAN_FOLDER
     * setting must be Y for this setting to have any effect. Changes to this
     * value will be detected at the next scheduled run of the
     * SharedFolderCleaner service (i.e. within 60 minutes). Default is 720
     * hours (i.e. 30 days). Used by the SharedFolderCleaner service.
     */
    public static final String SCANNED_FILE_LIFETIME = "scanned-file-lifetime";
    /**
     * clean-network-scan-folder-poll-period - The length of time in minutes
     * between each scheduled run of the SharedFolderCleaner service. The SOLA
     * Services must be restarted before changes to this value take effect.
     * Default is 60 minutes. Used by the SharedFolderCleaner service.
     */
    public static final String CLEAN_NETWORK_SCAN_FOLDER_POLL_PERIOD = "clean-network-scan-folder-poll-period";
    /**
     * server-document-cache-folder - The folder the server document cache is
     * located. It is recommended that this is a different location to the
     * NETWORK_SCAN_FOLDER. The SOLA Services must be restarted before changes
     * to this value take effect. Default is <user home>/sola/cache/documents.
     * Used by the Digital Archive Service.
     */
    public static final String SERVER_DOCUMENT_CACHE_FOLDER = "server-document-cache-folder";
    /**
     * server-document-cache-max-size - The maximum size in MB of the server
     * document cache. The SOLA Services must be restarted before changes to
     * this value take effect. Default value is 500MB. Used by the Digital
     * Archive Service.
     */
    public static final String SERVER_DOCUMENT_CACHE_MAX_SIZE = "server-document-cache-max-size";
    /**
     * server-document-cache-resized - The maximum size in MB of the server
     * document cache after it is resized/maintained. The SOLA Services must be
     * restarted before changes to this value take effect. Default value is
     * 200MB. Used by the Digital Archive Service.
     */
    public static final String SERVER_DOCUMENT_CACHE_RESIZED = "server-document-cache-resized";
    /**
     * map-tolerance - The tolerance used while snapping geometries to each
     * other. If two points are within this distance they are considered being
     * in the same location. Default 0.01 of the map units.
     */
    public static final String MAP_TOLERANCE = "map-tolerance";
    /**
     * map-shift-tolernace-rural - The shift tolerance of boundary points used
     * in cadastre change in rural areas. Users will need to restart their
     * client applications if they want changes to this setting to take effect.
     * Default 20
     */
    public static final String MAP_SHIFT_TOLERANCE_RURAL = "map-shift-tolernace-rural";
    /**
     * map-shift-tolernace-urban - The shift tolerance of boundary points used
     * in cadastre change in urban areas. Users will need to restart their
     * client applications if they want changes to this setting to take effect.
     * Default 5
     */
    public static final String MAP_SHIFT_TOLERANCE_URBAN = "map-shift-tolernace-urban";
    /**
     * map-srid - The srid of the geographic data administered by the system.
     * Users will need to restart their client applications if they want changes
     * to this setting to take effect. Default is 32702 (i.e. New Zealand)
     */
    public static final String MAP_SRID = "map-srid";
    /**
     * map-west - The western most coordinate when displaying the initial map
     * extent. Users will need to restart their client applications if they want
     * changes to this setting to take effect.
     */
    public static final String MAP_WEST = "map-west";
    /**
     * map-east - The eastern most coordinate when displaying the initial map
     * extent. Users will need to restart their client applications if they want
     * changes to this setting to take effect.
     */
    public static final String MAP_EAST = "map-east";
    /**
     * map-south - The southern most coordinate when displaying the initial map
     * extent. Users will need to restart their client applications if they want
     * changes to this setting to take effect.
     */
    public static final String MAP_SOUTH = "map-south";
    /**
     * map-north - The northern most coordinate when displaying the initial map
     * extent. Users will need to restart their client applications if they want
     * changes to this setting to take effect.
     */
    public static final String MAP_NORTH = "map-north";
}
