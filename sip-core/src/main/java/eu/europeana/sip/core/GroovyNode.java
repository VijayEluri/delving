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

import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.xml.QName;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simpler adaptation of groovy.util.Node
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@SuppressWarnings("unchecked")
public class GroovyNode {

    private GroovyNode parent;

    private Object name;

    private Map<String, String> attributes;

    private Object value;

    public GroovyNode(GroovyNode parent, Object name) {
        this(parent, name, new GroovyNodeList());
    }

    public GroovyNode(GroovyNode parent, Object name, Object value) {
        this(parent, name, new TreeMap<String, String>(), value);
    }

    public GroovyNode(GroovyNode parent, Object name, Map<String, String> attributes, Object value) {
        this.parent = parent;
        this.name = name;
        this.attributes = attributes;
        this.value = value;
        if (parent != null) {
            getParentList(parent).add(this);
        }
    }

    public String text() {
        if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof Collection) {
            Collection coll = (Collection) value;
            String previousText = null;
            StringBuffer buffer = null;
            for (Object child : coll) {
                if (child instanceof String) {
                    String childText = (String) child;
                    if (previousText == null) {
                        previousText = childText;
                    }
                    else {
                        if (buffer == null) {
                            buffer = new StringBuffer();
                            buffer.append(previousText);
                        }
                        buffer.append(childText);
                    }
                }
            }
            if (buffer != null) {
                return buffer.toString();
            }
            else {
                if (previousText != null) {
                    return previousText;
                }
            }
        }
        return "";
    }

    public List children() {
        if (value == null) {
            return new GroovyNodeList();
        }
        if (value instanceof List) {
            return (List) value;
        }
        // we're probably just a String
        GroovyNodeList result = new GroovyNodeList();
        result.add(value);
        return result;
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public Object name() {
        return name;
    }

    public Object value() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public GroovyNode parent() {
        return parent;
    }

    /**
     * Provides lookup of elements by non-namespaced name
     *
     * @param key the name (or shortcut key) of the node(s) of interest
     * @return the nodes which match key
     */
    public Object get(String key) {
        if (key != null && key.charAt(0) == '@') {
            String attributeName = key.substring(1);
            return attributes().get(attributeName);
        }
        if ("..".equals(key)) {
            return parent();
        }
        if ("*".equals(key)) {
            return children();
        }
        if ("**".equals(key)) {
            return depthFirst();
        }
        return getByName(key);
    }

    public Object multiply(Closure closure) {
        for (Object child : children()) {
            closure.call(child);
        }
        return null;
    }

    public String [] mod(String regex) { // operator %
        return text().split(regex);
    }

    public String toString() {
        return text();
    }

    // privates ===================================================================================

    private List<Object> getParentList(GroovyNode parent) {
        Object parentValue = parent.value();
        List<Object> parentList;
        if (parentValue instanceof List) {
            parentList = (List<Object>) parentValue;
        }
        else {
            parentList = new GroovyNodeList();
            parentList.add(parentValue);
            parent.setValue(parentList);
        }
        return parentList;
    }

    private GroovyNodeList getByName(String name) {
        GroovyNodeList answer = new GroovyNodeList();
        for (Object child : children()) {
            if (child instanceof GroovyNode) {
                GroovyNode childNode = (GroovyNode) child;
                Object childNodeName = childNode.name();
                if (childNodeName instanceof QName) {
                    QName qn = (QName) childNodeName;
                    if (qn.matches(name)) {
                        answer.add(childNode);
                    }
                }
                else if (name.equals(childNodeName)) {
                    answer.add(childNode);
                }
            }
        }
        return answer;
    }

    private List<Object> depthFirst() {
        List<Object> answer = new GroovyNodeList();
        answer.add(this);
        answer.addAll(depthFirstRest());
        return answer;
    }

    private List<Object> depthFirstRest() {
        List<Object> answer = new GroovyNodeList();
        for (Iterator iter = InvokerHelper.asIterator(value); iter.hasNext();) {
            Object child = iter.next();
            if (child instanceof GroovyNode) {
                GroovyNode childNode = (GroovyNode) child;
                List<Object> children = childNode.depthFirstRest();
                answer.add(childNode);
                answer.addAll(children);
            }
        }
        return answer;
    }

//    /**
//     * Provide a collection of all the nodes in the tree
//     * using a breadth-first traversal.
//     *
//     * @return the list of (breadth-first) ordered nodes
//     */
//    public List<Object> breadthFirst() {
//        List<Object> answer = new GroovyNodeList();
//        answer.add(this);
//        answer.addAll(breadthFirstRest());
//        return answer;
//    }
//    /**
//     * Provides lookup of elements by QName.
//     *
//     * @param name the QName of interest
//     * @return the nodes matching name
//     */
//    private List<Object> breadthFirstRest() {
//        List<Object> answer = new GroovyNodeList();
//        List<Object> nextLevelChildren = getDirectChildren();
//        while (!nextLevelChildren.isEmpty()) {
//            List<Object> working = new GroovyNodeList(nextLevelChildren);
//            nextLevelChildren = new GroovyNodeList();
//            for (Object aWorking : working) {
//                GroovyNode childNode = (GroovyNode) aWorking;
//                answer.add(childNode);
//                List<Object> children = childNode.getDirectChildren();
//                nextLevelChildren.addAll(children);
//            }
//        }
//        return answer;
//    }
//
//    public GroovyNodeList getAt(QName name) {
//        GroovyNodeList answer = new GroovyNodeList();
//        for (Object child : children()) {
//            if (child instanceof GroovyNode) {
//                GroovyNode childNode = (GroovyNode) child;
//                Object childNodeName = childNode.name();
//                if (name.matches(childNodeName)) {
//                    answer.add(childNode);
//                }
//            }
//        }
//        return answer;
//    }
//
//    private List<Object> getDirectChildren() {
//        List<Object> answer = new GroovyNodeList();
//        for (Iterator iter = InvokerHelper.asIterator(value); iter.hasNext();) {
//            Object child = iter.next();
//            if (child instanceof GroovyNode) {
//                GroovyNode childNode = (GroovyNode) child;
//                answer.add(childNode);
//            }
//        }
//        return answer;
//    }
//
//    public boolean remove(GroovyNode child) {
//        child.parent = null;
//        return getParentList(this).remove(child);
//    }
//
//    public GroovyNode appendNode(Object name, Map<String, String> attributes) {
//        return new GroovyNode(this, name, attributes);
//    }
//
//    public GroovyNode appendNode(Object name) {
//        return new GroovyNode(this, name);
//    }
//
//    public GroovyNode appendNode(Object name, Object value) {
//        return new GroovyNode(this, name, value);
//    }
//
//    public GroovyNode appendNode(Object name, Map<String, String> attributes, Object value) {
//        return new GroovyNode(this, name, attributes, value);
//    }
//
//    public Iterator iterator() {
//        return children().iterator();
//    }
//
//    public List<String> names() {
//        List<String> names = new ArrayList<String>();
//        for (Object o : children()) {
//            names.add(((GroovyNode) o).name().toString());
//        }
//        return names;
//    }



    protected static void setMetaClass(final MetaClass metaClass, Class nodeClass) {
        final MetaClass newMetaClass = new DelegatingMetaClass(metaClass) {
            @Override
            public Object getAttribute(final Object object, final String attribute) {
                GroovyNode n = (GroovyNode) object;
                return n.get("@" + attribute);
            }

            @Override
            public void setAttribute(final Object object, final String attribute, final Object newValue) {
                GroovyNode n = (GroovyNode) object;
                n.attributes().put(attribute, (String) newValue);
            }

            @Override
            public Object getProperty(Object object, String property) {
                if (object instanceof GroovyNode) {
                    GroovyNode n = (GroovyNode) object;
                    return n.get(property);
                }
                return super.getProperty(object, property);
            }

            @Override
            public void setProperty(Object object, String property, Object newValue) {
                if (property.startsWith("@")) {
                    String attribute = property.substring(1);
                    GroovyNode n = (GroovyNode) object;
                    n.attributes().put(attribute, (String)newValue);
                    return;
                }
                delegate.setProperty(object, property, newValue);
            }

        };
        GroovySystem.getMetaClassRegistry().setMetaClass(nodeClass, newMetaClass);
    }

    static {
        setMetaClass(GroovySystem.getMetaClassRegistry().getMetaClass(GroovyNode.class), GroovyNode.class);
    }

}
