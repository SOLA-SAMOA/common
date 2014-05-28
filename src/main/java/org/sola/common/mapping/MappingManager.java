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
package org.sola.common.mapping;

import java.util.ArrayList;
import java.util.List;
import org.dozer.DozerBeanMapper;
import org.dozer.DozerEventListener;
import org.dozer.Mapper;

/**
 * Singleton wrapper class for the Dozer Bean Mapper which can be used to provide additional mapping
 * configuration if required.
 */
public class MappingManager {

    private DozerBeanMapper mapper;
    private static boolean initialized = false;
    private static final String DEFAULT_MAPPING_CONFIG = "/dozerMappingConfig.xml";

    private MappingManager() {
        mapper = new DozerBeanMapper();
    }

    /**
     * @return The instantiated Dozer Bean Mapper.
     */
    private Mapper get() {
        return mapper;
    }

    /**
     * @return Provides access to the instantiated Dozer Bean Mapper.
     * @see #getMapper(org.dozer.DozerEventListener)
     * @see #getMapper(org.dozer.DozerEventListener, java.lang.String)
     */
    public static Mapper getMapper() {
        return getMapper(null, null);
    }

    /**
     * Adds an event listener into the DozerMapper and returns the mapper. The listener will only be
     * configured the very first time this method is called. Subsequent calls will only return the
     * existing mapper.
     *
     * @param listener The listener to add.
     * @see #getMapper(org.dozer.DozerEventListener, java.lang.String)
     * @see #getMapper()
     */
    public static Mapper getMapper(DozerEventListener listener) {
        return getMapper(listener, null);
    }

    /**
     * Retrieves the mapper configured with the listener and custom mapping file. The configuration
     * will only occur the very first time the DozerBeanMapper is instantiated. Subsequent calls to
     * this method will only return the existing mapper.
     *
     * @param listener THe listener to configure on the mapper. Can be null
     * @param mappingFileURL The url of the dozer mapping config file to add.
     * @return The configured mapper
     */
    public static Mapper getMapper(DozerEventListener listener, String mappingFileURL) {
        DozerBeanMapper dozerMapper = ((DozerBeanMapper) MappingManagerHolder.INSTANCE.get());
        if (!initialized) {
            initialized = true;
            // Configure the mapping files
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.addAll(dozerMapper.getMappingFiles());
            mappingFiles.add(MappingManager.class.getResource(DEFAULT_MAPPING_CONFIG).toString());
            if (mappingFileURL != null && !mappingFileURL.trim().isEmpty()) {
                mappingFiles.add(mappingFileURL);
            }
            dozerMapper.setMappingFiles(mappingFiles);

            // Configure the Event Listeners
            List<DozerEventListener> listeners = new ArrayList<DozerEventListener>();
            listeners.addAll(dozerMapper.getEventListeners());
            if (listener != null) {
                listeners.add(listener);
            }
            dozerMapper.setEventListeners(listeners);
        }
        return dozerMapper;
    }

    private static class MappingManagerHolder {

        private static final MappingManager INSTANCE = new MappingManager();
    }
}
