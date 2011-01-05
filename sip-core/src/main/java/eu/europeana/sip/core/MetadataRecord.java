/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.core;

import com.ctc.wstx.exc.WstxParsingException;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Something to hold the groovy node and turn it into a string
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetadataRecord {
    private GroovyNode rootNode;
    private int recordNumber;

    private MetadataRecord(GroovyNode rootNode, int recordNumber) {
        this.rootNode = rootNode;
        this.recordNumber = recordNumber;
    }

    public GroovyNode getRootNode() {
        return rootNode;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public List<MetadataVariable> getVariables() {
        List<MetadataVariable> variables = new ArrayList<MetadataVariable>();
        getVariables(rootNode, variables);
        return variables;
    }

    private void getVariables(GroovyNode groovyNode, List<MetadataVariable> variables) {
        if (groovyNode.value() instanceof GroovyList) {
            GroovyList list = (GroovyList) groovyNode.value();
            for (Object member : list) {
                GroovyNode childNode = (GroovyNode) member;
                getVariables(childNode, variables);
            }
        }
        else {
            List<GroovyNode> path = new ArrayList<GroovyNode>();
            GroovyNode walk = groovyNode;
            while (walk != null) {
                path.add(walk);
                walk = walk.parent();
            }
            Collections.reverse(path);
            StringBuilder out = new StringBuilder();
            Iterator<GroovyNode> nodeWalk = path.iterator();
            while (nodeWalk.hasNext()) {
                String nodeName = nodeWalk.next().name();
                out.append(nodeName);
                if (nodeWalk.hasNext()) {
                    out.append('.');
                }
            }
            String variableName = out.toString();
            variables.add(new MetadataVariable(variableName, (String) groovyNode.value()));
        }
    }

    public String toHtml() {
        StringBuilder out = new StringBuilder("<html><table border=1 width=100%>");
        out.append(String.format("<tr><th colspan=2>Record %d</th></tr>\n", recordNumber));
        for (MetadataVariable variable : getVariables()) {
            out.append(String.format("<tr><td width=40%%>%s</td><td width=60%%><strong>%s</strong></td></tr>\n", variable.getName(), variable.getValue()));
        }
        out.append("</table><html>");
        return out.toString();
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Record #").append(recordNumber).append('\n');
        for (MetadataVariable variable : getVariables()) {
            out.append(variable.toString()).append('\n');
        }
        return out.toString();
    }

    public static class Factory {
        private XMLInputFactory2 inputFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
        private Map<String, String> namespaces;

        public Factory(Map<String, String> namespaces) {
            this.namespaces = namespaces;
            inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            inputFactory.configureForSpeed();
        }

        public MetadataRecord fromGroovyNode(GroovyNode rootNode, int recordNumber) {
            return new MetadataRecord(rootNode, recordNumber);
        }

        public MetadataRecord fromXml(String recordContents) throws XMLStreamException {
            String recordString = createCompleteRecordString(recordContents);
            try {
                Reader reader = new StringReader(recordString);
                XMLStreamReader2 input = (XMLStreamReader2) inputFactory.createXMLStreamReader(reader);
                GroovyNode node = null;
                StringBuilder value = new StringBuilder();
                while (true) {
                    switch (input.getEventType()) {
                        case XMLEvent.START_DOCUMENT:
                            break;
                        case XMLEvent.START_ELEMENT:
                            node = (node == null)? new GroovyNode("input") : new GroovyNode(node, input.getNamespaceURI(), input.getLocalName(), input.getPrefix());
                            if (input.getAttributeCount() > 0) {
                                for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                                    QName attributeName = input.getAttributeName(walk);
                                    node.attributes().put(attributeName.getLocalPart(), input.getAttributeValue(walk));
                                }
                            }
                            value.setLength(0);
                            break;
                        case XMLEvent.CHARACTERS:
                        case XMLEvent.CDATA:
                            value.append(input.getText());
                            break;
                        case XMLEvent.END_ELEMENT:
                            if (node != null) {
                                node = node.parent();
                                String valueString = value.toString().replaceAll("\n", " ").replaceAll(" +", " ").trim();
                                value.setLength(0);
                                if (valueString.length() > 0) {
                                    node.setValue(valueString);
                                }
                                if (node.parent() == null) {
                                    return new MetadataRecord(node, -1);
                                }
                            }
                            break;
                        case XMLEvent.END_DOCUMENT: {
                            break;
                        }
                    }
                    if (!input.hasNext()) {
                        break;
                    }
                    input.next();
                }
            }
            catch (WstxParsingException e) {
                throw new XMLStreamException("Problem parsing record:\n" + recordString, e);
            }
            throw new XMLStreamException("Unexpected end while parsing");
        }

        private String createCompleteRecordString(String xmlRecord) {
            StringBuilder out = new StringBuilder("<?xml version=\"1.0\"?>\n");
            out.append("<record");
            for (Map.Entry<String, String> namespace : namespaces.entrySet()) {
                out.append(String.format(" xmlns:%s=\"%s\"", namespace.getKey(), namespace.getValue()));
            }
            out.append(">");
            out.append(xmlRecord);
            out.append("</record>");
            return out.toString();
        }
    }

}
