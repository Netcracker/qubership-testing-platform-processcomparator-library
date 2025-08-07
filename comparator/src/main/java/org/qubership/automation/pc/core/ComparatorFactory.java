/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.automation.pc.core;

import java.util.HashMap;
import java.util.Map;

import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.exceptions.ComparatorNotFoundException;
import org.qubership.automation.pc.core.exceptions.FactoryInstatiationException;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.interfaces.IComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for providing {@link IComparator} implementations based on {@link DataContentType}.
 * <p>
 * This class follows a singleton pattern to maintain a cache of comparator mappings and
 * instantiate them on demand. Comparators are mapped via their fully-qualified class names
 * and loaded dynamically using reflection.
 * <p>
 * <strong>Supported DataContentTypes:</strong>
 * <ul>
 *   <li>PRIMITIVES → PrimitivesComparator</li>
 *   <li>XML → XmlComparator</li>
 *   <li>MASKED_XML → MaskedXmlComparator</li>
 *   <li>TASK_LIST → TaskListComparator</li>
 *   <li>PLAIN_TEXT → PlainTextComparator</li>
 *   <li>FULL_TEXT → FullTextComparator</li>
 *   <li>JSON → JsonComparator</li>
 *   <li>XSD → XsdComparator</li>
 *   <li>TABLE → TableComparator</li>
 *   <li>CSV → CsvComparator</li>
 * </ul>
 * <p>
 * In case the comparator class is not found or cannot be instantiated, appropriate custom exceptions
 * are thrown: {@link ComparatorNotFoundException} and {@link FactoryInstatiationException}.
 * <p>
 *
 * @see IComparator
 * @see DataContentType
 * @see ComparatorNotFoundException
 * @see FactoryInstatiationException
 */
public class ComparatorFactory {
    
    private final Logger log = LoggerFactory.getLogger(ComparatorFactory.class);
    private final Map<DataContentType,ComparatorInfo> comparators = new HashMap<>();
    
    public ComparatorFactory() {
        fillComparators();
    }
    
    private void fillComparators() {
        comparators.put(DataContentType.PRIMITIVES,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.PrimitivesComparator"));
        comparators.put(DataContentType.XML,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.XmlComparator"));
        comparators.put(DataContentType.MASKED_XML,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.MaskedXmlComparator"));
        comparators.put(DataContentType.TASK_LIST,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.TaskListComparator"));
        comparators.put(DataContentType.PLAIN_TEXT,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.PlainTextComparator"));
        comparators.put(DataContentType.FULL_TEXT,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.FullTextComparator"));
        comparators.put(DataContentType.JSON,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.JsonComparator"));
        comparators.put(DataContentType.XSD,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.XsdComparator"));
        comparators.put(DataContentType.TABLE,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.TableComparator"));
        comparators.put(DataContentType.CSV,
                new ComparatorInfo("org.qubership.automation.pc.comparator.impl.CsvComparator"));
    }
    
    private IComparator getComparatorByContentType(DataContentType contentType)
            throws FactoryInstatiationException, ComparatorNotFoundException {
        if (comparators.containsKey(contentType)) {
            try {
                Class<?> comparatorClass = comparators.get(contentType).getComparatorClass(); 
                IComparator comparator = (IComparator) comparatorClass.newInstance();
                return comparator;
            } catch (InstantiationException | IllegalAccessException ex) {
                log.error(ResponseMessages.msg(20103, comparators.get(contentType).path));
                throw new FactoryInstatiationException(ResponseMessages.msg(20103, comparators.get(contentType).path));
            } catch (ClassNotFoundException ex) {
                log.error(ResponseMessages.msg(20104,contentType.toString()));
                throw new ComparatorNotFoundException(ResponseMessages.msg(20104,contentType.toString()));
            }
        } else {
            log.error(ResponseMessages.msg(20104,contentType.toString()));
            throw new ComparatorNotFoundException(ResponseMessages.msg(20104,contentType.toString()));
        }
    }
    
    private static ComparatorFactory _instance;
    
    public static ComparatorFactory getInstance() {
        if (_instance == null) {
            _instance = new ComparatorFactory();
        }
        
        return _instance;
    }
    
    public static IComparator getComparator(DataContentType contentType)
            throws FactoryInstatiationException, ComparatorNotFoundException {
        return getInstance().getComparatorByContentType(contentType);
    }
    
    private class ComparatorInfo {
        public String path;
        public Class<?> comparatorClass;
        
        public ComparatorInfo() {
        }

        public ComparatorInfo(String path) {
            this.path = path;
        }

        public Class<?> getComparatorClass() throws ClassNotFoundException {
            if (this.comparatorClass == null) {
                if (this.path == null) {
                    return null;
                } else {
                    this.comparatorClass = Class.forName(this.path);
                }
            }
            return this.comparatorClass;
        }
    }
}
