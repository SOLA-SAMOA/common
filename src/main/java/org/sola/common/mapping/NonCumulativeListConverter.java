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

import java.util.List;
import org.apache.commons.lang.ClassUtils;
import org.dozer.DozerConverter;
import org.dozer.Mapper;
import org.dozer.MapperAware;
import org.sola.common.SOLAException;
import org.sola.common.messaging.ServiceMessage;

/**
 * Non cumulative converter that correctly matches source items to destination items while
 * performing list translation. Fixes an issue with the Dozer bean mapper framework. <p>To configure
 * this converter, the dozer configuration file needs to have field level mappings set up as
 * follows.
 * <pre> {@code
 * <mapping>
 *  <class-a>org.sola.services.boundary.transferobjects.administrative.BaUnitTO</class-a>
 *  <class-b>org.sola.services.ejb.administrative.repository.entities.BaUnit</class-b>
 *      <field custom-converter="org.sola.common.mapping.NonCumulativeListConverter"
 *          custom-converter-param="org.sola.services.ejb.administrative.repository.entities.Rrr,
 *          org.sola.services.boundary.transferobjects.administrative.RrrTO">
 *          <a>rrrList</a>
 *          <b>rrrList</b>
 *      </field>
 * </mapping>}</pre></p> Note that due to Java type erasure, it is necessary to specify the from and
 * to list class types as parameters to the converter to ensure the converter correctly generates
 * the correct list classes. <p>Fix for Lighthouse ticket #178</p>
 */
public class NonCumulativeListConverter extends DozerConverter<List, List> implements MapperAware {

    private Mapper mapper;

    /**
     * Registers the converter for the List Class type.
     */
    public NonCumulativeListConverter() {
        super(List.class, List.class);
    }

    /**
     * Converts a string class name into a class type. Uses ClassUtils to create the class as
     * Class.forName may not be able to locate the class. 
     *
     * @param className The name of the class
     * @return The class for the className or an exception if the className is invalid.
     */
    private Class<?> getClassFromName(String className) {
        Class<?> result = null;
        if (className != null && !className.trim().isEmpty()) {
            try {
                result = ClassUtils.getClass(className);
            } catch (ClassNotFoundException ex) {
                throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                        new Object[]{"Unable to locate class " + className, ex});
            }
        }
        return result;
    }

    /**
     * Triggered by Dozer Bean Mapper to translate between two lists. This converter is configured
     * using Dozer field converters.
     *
     * @param source The list from the object that is the source of the translation
     * @param destination The list from the object that is the destination of the translation
     * @return The destination list with the source list converted into it.
     */
    @Override
    public List convertTo(List source, List destination) {
        // Dozer only calls convertFrom because the source and destination are the same type.
        // This method is implemented to satisfy the DozerConverter abstract methods. 
        return convertFrom(source, destination);
    }

    /**
     * Triggered by Dozer Bean Mapper to translate between two lists. This converter is configured
     * using Dozer field converters.
     *
     * @param source The list from the object that is the source of the translation
     * @param destination The list from the object that is the destination of the translation
     * @return The destination list with the source list converted into it.
     */
    @Override
    public List convertFrom(List source, List destination) {
        String[] listClasses = this.getParameter().split(",");
        Class<?> destClass = getClassFromName(listClasses[0].trim());
        Class<?> listClass2 = null;
        if (listClasses.length > 1) {
            listClass2 = getClassFromName(listClasses[1].trim());
        }
        if (source != null && source.size() > 0) {
            destClass = source.get(0).getClass() == destClass ? listClass2 : destClass;
            destination = MappingUtility.translateList(source, destination, destClass, mapper);
        }
        return destination;
    }

    /**
     * Implemented to support he MapperAware interface. Must be set before triggering the
     * {@linkplain #translateList(java.util.List, java.util.List, java.lang.Class, java.lang.Class) translateList}
     * method
     *
     * @param mapper The Mapper to use.
     */
    @Override
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }
}
