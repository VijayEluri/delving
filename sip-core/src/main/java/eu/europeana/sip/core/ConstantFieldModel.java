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

import eu.europeana.sip.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.definitions.annotations.EuropeanaField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hold a collection of global fields that can be used here and there.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ConstantFieldModel {
    private static final String PREFIX = "// ConstantField ";
    private List<FieldSpec> fields = new ArrayList<FieldSpec>();
    private Map<String, String> map = new TreeMap<String, String>();

    public ConstantFieldModel(AnnotationProcessor annotationProcessor) {
        fields.add(new FieldSpec("collectionId"));
        for (EuropeanaField field : annotationProcessor.getAllFields()) {
            if (field.europeana().constant()) {
                FieldSpec fieldSpec = new FieldSpec(field.getFieldNameString());
                fieldSpec.setEnumValues(field.getEnumValues());
                fields.add(fieldSpec);
            }
        }
    }

    public List<FieldSpec> getFields() {
        return fields;
    }

    public void clear() {
        Set<String> keySet = new TreeSet<String>(map.keySet());
        map.clear();
        for (String key : keySet) {
            fireUpdate(key, "");
        }
    }

    public void set(String field, String value) {
        String oldValue = map.get(field);
        if (oldValue == null || !oldValue.equals(value)) {
            if (value.isEmpty()) {
                map.remove(field);
            }
            else {
                map.put(field, value);
            }
            fireUpdate(field, value);
        }
    }

    public void fromMapping(List<String> mapping) {
        clear();
        for (String line : mapping) {
            if (line.startsWith(PREFIX)) {
                line = line.substring(PREFIX.length());
                int space = line.indexOf(" ");
                if (space > 0) {
                    String fieldName = line.substring(0, space);
                    String value = line.substring(space).trim();
                    set(fieldName, value);
                }
            }
        }
    }

    public String get(String fieldName) {
        String value = map.get(fieldName);
        if (value != null) {
            return value;
        }
        else {
            return "";
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (FieldSpec fieldSpec : fields) {
            String value = map.get(fieldSpec.getName());
            if (value == null) {
                value = "";
            }
            out.append(PREFIX).append(fieldSpec.getName()).append(' ').append(value).append('\n');
        }
        return out.toString();
    }

    public class FieldSpec {
        private String name;
        private Set<String> enumValues;

        public FieldSpec(String name) {
            this.name = name;
        }

        public void setEnumValues(Set<String> enumValues) {
            this.enumValues = enumValues;
        }

        public String getName() {
            return name;
        }

        public Set<String> getEnumValues() {
            return enumValues;
        }

        public String toString() {
            return name;
        }
    }

    // observable

    public interface Listener {
        void updated(String fieldName, String value);
    }

    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private void fireUpdate(String fieldName, String value) {
        for (Listener listener : listeners) {
            listener.updated(fieldName, value);
        }
    }


}
