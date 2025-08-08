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

package org.qubership.automation.pc.comparator.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A comparator implementation for validating XML documents against an XSD schema.
 *
 * <p>
 * This comparator checks the expected and actual XML strings for compliance with the provided XSD schema.
 * It supports configuration flags to optionally skip validation of the expected (ER) document.
 *
 * <p>
 * Validation errors and warnings are captured
 * and returned as {@link DiffMessage} objects with appropriate result types.
 *
 * <p>
 * Typical use cases include:
 * <ul>
 *   <li>Schema compliance testing of XML payloads</li>
 *   <li>Detailed error reporting for invalid or non-conforming XML structures</li>
 * </ul>
 *
 * @see AbstractComparator
 * @see DiffMessage
 */
public class XsdComparator extends AbstractComparator {

    public static final String XSD_FILE = "xsdFile";
    public static final String SKIP_ER_VALIDATION = "skipER";
    private List<DiffMessage> diffMessages = new ArrayList<>();

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        List<String> xsdRows = configuration.getParameters(XSD_FILE); /* Not 'File'. It's a content of xsd-schema */
        if (xsdRows == null || xsdRows.isEmpty()) {
            throw new ComparatorException("Rule \"xsdFile\" must be set. "
                    + "The rule value must contain xsd-template.", 20002);
        }
        String xsdFile = StringUtils.join(xsdRows, '\n');
        final Schema schema = getSchema(new StreamSource(new StringReader(xsdFile)));
        if (!configuration.getBooleanParameter(SKIP_ER_VALIDATION, false)) {
            validate(er, schema, true, 0);
        }
        validate(ar, schema, false, diffMessages.size());
        return diffMessages;
    }

    private void validate(String xml, Schema xsd, boolean isControl, int startCounter) throws ComparatorException {
        final Validator validator = xsd.newValidator();
        validator.setErrorHandler(new ReportErrorHandler(diffMessages, isControl, startCounter));

        /* Need to remove all dust before "<?xml..." but we can't use XmlHelpers.cleanXml -
        it collapses all content to single line */
        int i = xml.indexOf("<?xml");
        if (i > 0) {
            xml = xml.substring(i);
        }

        Source xmlFile = new StreamSource(new StringReader(xml));
        try {
            validator.validate(xmlFile);
        } catch (IOException e) {
            throw new ComparatorException(String.format("Failed to validate xml (%s)!", isControl ? "ER" : "AR"), e);
        } catch (SAXException e) {
            /* Commented; This exception is returned in diffmessage and will be reported throught highlighter */
            // throw new ComparatorException(String.format("Failed to validate xml (%s)!",isControl? "er":"ar"), e);
        }
    }

    private Schema getSchema(Source xsdschema) throws ComparatorException {
        try {
            return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdschema);
        } catch (SAXException e) {
            throw new ComparatorException(String.format("Failed to parse xsd-schema:\n%s", xsdschema.toString()), e);
        }
    }

    public static class ReportErrorHandler implements ErrorHandler {
        private final List<DiffMessage> diffMessages;
        private final boolean isControl;
        int diffCounter = 0;

        public ReportErrorHandler(List<DiffMessage> diffMessages, boolean isControl, int startCounter) {
            this.diffMessages = diffMessages;
            this.isControl = isControl;
            this.diffCounter = startCounter;
        }

        public void warning(SAXParseException ex) {
            report(ex, "Warning");
        }

        public void error(SAXParseException ex) {
            report(ex, "Error");
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            report(ex, "Fatal Error");
            //throw ex; // Previous behaviour
        }

        private void report(SAXParseException ex, String messageType) {
            final DiffMessage diffMessage = new DiffMessage();
            switch (messageType) {
                case "Warning":
                    diffMessage.setResult(ResultType.SIMILAR);
                    break;
                case "Error":
                    diffMessage.setResult(ResultType.MODIFIED);
                    break;
                case "Fatal Error":
                    diffMessage.setResult(ResultType.ERROR);
                    break;
                default:
                    // Unrecognized message type — handle as needed
                    break;
            }
            final String info = formatDiffMessage(ex.getLineNumber(), ex.getColumnNumber());
            if (isControl) {
                diffMessage.setExpected(info);
                diffMessage.setActual("");
            } else {
                diffMessage.setExpected("");
                diffMessage.setActual(info);
            }
            diffMessage.setOrderId(++diffCounter);
            diffMessage.setDescription(messageType + ": " + ex.getMessage());
            diffMessages.add(diffMessage);
        }

        /* Format diff-info for future parse in highlighter. 
            Format is   
                <lineNumber>-<columnNumber>-<messageType>\n<message>
            messageType : SIMILAR (SaxParse: Warning) | MODIFIED (SaxParse: Error) | ERROR (SaxParse: Fatal Error) 
        */
        private String formatDiffMessage(int lineNumber, int columnNumber) {
            return String.format("%d-%d", lineNumber, columnNumber);
        }
    }
}
