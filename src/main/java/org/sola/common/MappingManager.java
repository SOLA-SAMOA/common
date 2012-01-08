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
package org.sola.common;

import java.util.ArrayList;
import java.util.List;
import org.dozer.DozerBeanMapper;
import org.dozer.DozerEventListener;
import org.dozer.Mapper;

/**
 * Singleton wrapper class for the Dozer Bean Mapper which can be used to provide
 * additional mapping configuration if required. 
 */
public class MappingManager {

    private DozerBeanMapper mapper;
    private static boolean initialized = false;

    private MappingManager() {
        mapper = new DozerBeanMapper();

        // Further configuration of Mapper can occur here...
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add(MappingManager.class.getResource("/dozerMappingConfig.xml").toString());
        mapper.setMappingFiles(mappingFiles);
    }

    /** @return The instantiated Dozer Bean Mapper with configuration applied. */
    public Mapper get() {
        return mapper;
    }

    /**
     * Adds a converter into the DozerMapper if the converter is not already 
     * registered. This method can only be called before once before the call to
     * getMapper(). Subsequent calls to this method are ignored. 
     * @param converter The custom converter to add.  
     */
    public static void setEventListener(DozerEventListener listener) {

        if (!initialized) {
            List listeners = new ArrayList<DozerEventListener>();
            listeners.add(listener);
            ((DozerBeanMapper) getMapper()).setEventListeners(listeners);
            initialized = true;
        }
    }

    /** @return Provides access to the instantiated Dozer Bean Mapper. */
    public static Mapper getMapper() {
        initialized = true;
        return MappingManagerHolder.INSTANCE.get();
    }

    private static class MappingManagerHolder {

        private static final MappingManager INSTANCE = new MappingManager();
    }
}