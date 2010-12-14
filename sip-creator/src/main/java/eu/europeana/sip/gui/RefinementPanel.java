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

package eu.europeana.sip.gui;

import eu.delving.metadata.AnalysisTree;
import eu.delving.metadata.CodeGenerator;
import eu.delving.metadata.FieldMapping;
import eu.delving.metadata.FieldStatistics;
import eu.delving.metadata.Path;
import eu.delving.metadata.SourceVariable;
import eu.delving.sip.FileStore;
import eu.europeana.sip.model.CompileModel;
import eu.europeana.sip.model.FieldMappingListModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class RefinementPanel extends JPanel {
    private SipModel sipModel;
    private JTextArea groovyCodeArea;
    private JButton removeMappingButton = new JButton("Remove Selected Mapping");
    private JButton dictionaryCreate = new JButton("Create");
    private JButton dictionaryEdit = new JButton("Edit");
    private JButton dictionaryDelete = new JButton("Delete");
    private JList mappingList;
    private JDialog parent;

    public RefinementPanel(JDialog parent, SipModel sipModel) {
        super(new BorderLayout());
        this.parent = parent;
        this.sipModel = sipModel;
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(createLeftSide());
        split.setRightComponent(createRightSide());
        split.setDividerLocation(0.5);
        add(split, BorderLayout.CENTER);
        wireUp();
    }

    private JPanel createLeftSide() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(createFieldMappingListPanel(), BorderLayout.CENTER);
        p.add(removeMappingButton, BorderLayout.SOUTH);
        p.setPreferredSize(new Dimension(600, 800));
        return p;
    }

    private JPanel createRightSide() {
        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.add(new RecordPanel(sipModel, sipModel.getFieldCompileModel()));
        p.add(createGroovyPanel());
        p.add(createOutputPanel());
        p.setPreferredSize(new Dimension(600, 800));
        return p;
    }

    private JPanel createFieldMappingListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Field Mappings"));
        mappingList = new JList(sipModel.getFieldMappingListModel());
        mappingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mappingList.setCellRenderer(new FieldMappingListModel.CellRenderer());
        p.add(scroll(mappingList));
        return p;
    }

    private JPanel createGroovyPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        groovyCodeArea = new JTextArea(sipModel.getFieldCompileModel().getCodeDocument());
        groovyCodeArea.setToolTipText(
                "<html><h2>Operators</h2>" +
                        "<ul>" +
                        "<li><b>each</b>: the star operator runs the code in braces for every input entry, input * { }</li>" +
                        "<li><b>combined</b>: the star-string-star operator concatenates with a delimiter, input * '; ' * { }</li>" +
                        "<li><b>wordsplit</b>: the star-star operator splits inputs into words, input ** { ..it.. }</li>" +
                        "<li><b>chop</b>: the percent-string-star operator splits inputs on a regular expression, input % /;/ * { }</li>" +
                        "</ul>" +
                        "<h2>Notes</h2>" +
                        "<ul>" +
                        "<li>Inside the braces the '<b>it</b>' variable holds the contents</li>" +
                        "<li>it can easily be substituted like <b>&quot;</b>http://somewhere.com/images/${<b>it</b>}.jpg<b>&quot;</li>" +
                        "</ul>"
        );
        JScrollPane scroll = new JScrollPane(groovyCodeArea);
        p.add(scroll, BorderLayout.CENTER);
        p.add(createGroovySouth(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createGroovySouth() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Dictionary"));
        dictionaryCreate.setEnabled(false);
        dictionaryEdit.setEnabled(false);
        dictionaryDelete.setEnabled(false);
        p.add(dictionaryCreate);
        p.add(dictionaryEdit);
        p.add(dictionaryDelete);
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Record"));
        JTextArea outputArea = new JTextArea(sipModel.getFieldCompileModel().getOutputDocument());
        outputArea.setEditable(false);
        p.add(scroll(outputArea), BorderLayout.CENTER);
        p.add(new JLabel("Note: URLs can be launched by double-clicking them.", JLabel.CENTER), BorderLayout.SOUTH);
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 800));
        return scroll;
    }

    private void wireUp() {
        removeMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    sipModel.removeFieldMapping(fieldMapping);
                }
            }
        });
        dictionaryCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    CodeGenerator codeGenerator = new CodeGenerator();
                    SourceVariable sourceVariable = getSourceVariable(fieldMapping);
                    fieldMapping.createDictionary(sourceVariable.getNode().getStatistics().getHistogramValues());
                    codeGenerator.generateCodeFor(fieldMapping, sourceVariable, true);
                    setFieldMapping(fieldMapping);
                }
            }
        });
        dictionaryEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    DictionaryDialog dialog = new DictionaryDialog(parent, fieldMapping, new Runnable() {
                        @Override
                        public void run() {
                            setFieldMapping(fieldMapping);
                        }
                    });
                    dialog.setVisible(true);
                }
            }
        });
        dictionaryDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    int nonemptyEntries = 0;
                    for (String value : fieldMapping.dictionary.values()) {
                        if (!value.trim().isEmpty()) {
                            nonemptyEntries++;
                        }
                    }
                    if (nonemptyEntries > 0) {
                        int response = JOptionPane.showConfirmDialog(
                                parent,
                                String.format(
                                        "Are you sure that you want to discard the %d entries set?",
                                        nonemptyEntries
                                ),
                                "Delete Dictionary",
                                JOptionPane.OK_CANCEL_OPTION
                        );
                        if (response != JOptionPane.OK_OPTION) {
                            return;
                        }
                    }
                    fieldMapping.dictionary = null;
                    CodeGenerator codeGenerator = new CodeGenerator();
                    SourceVariable sourceVariable = getSourceVariable(fieldMapping);
                    codeGenerator.generateCodeFor(fieldMapping, sourceVariable, false);
                    setFieldMapping(fieldMapping);
                }
            }
        });
        mappingList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                setFieldMapping(fieldMapping);
            }
        });
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void updatedDataSetStore(FileStore.DataSetStore dataSetStore) {
            }

            @Override
            public void updatedStatistics(FieldStatistics fieldStatistics) {

            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
            }
        });
        sipModel.getFieldCompileModel().getCodeDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                sipModel.getFieldCompileModel().setCode(groovyCodeArea.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                sipModel.getFieldCompileModel().setCode(groovyCodeArea.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                sipModel.getFieldCompileModel().setCode(groovyCodeArea.getText());
            }
        });
        groovyCodeArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                sipModel.getRecordCompileModel().refreshCode(); // todo: somebody else do this?
            }
        });
        sipModel.getFieldCompileModel().addListener(new ModelStateListener());
    }

    private void setFieldMapping(FieldMapping fieldMapping) {
        if (fieldMapping != null) {
            sipModel.getFieldCompileModel().setSelectedPath(fieldMapping.getDefinition().path.toString());
            AnalysisTree.Node node = getNode(fieldMapping);
            if (node != null) {
                dictionaryCreate.setEnabled(fieldMapping.dictionary == null && CodeGenerator.isDictionaryPossible(fieldMapping.getDefinition(), node));
            }
            else {
                dictionaryCreate.setEnabled(false);
            }
            dictionaryEdit.setEnabled(fieldMapping.dictionary != null);
            dictionaryDelete.setEnabled(fieldMapping.dictionary != null);
            removeMappingButton.setEnabled(true);
        }
        else {
            sipModel.getFieldCompileModel().setSelectedPath(null);
            removeMappingButton.setEnabled(false);
            dictionaryCreate.setEnabled(false);
            dictionaryEdit.setEnabled(false);
            dictionaryDelete.setEnabled(false);
        }
    }

    private AnalysisTree.Node getNode(FieldMapping fieldMapping) {
        SourceVariable sourceVariable = getSourceVariable(fieldMapping);
        return sourceVariable != null ? sourceVariable.getNode() : null;
    }

    private SourceVariable getSourceVariable(FieldMapping fieldMapping) {
        List<String> variableNames = fieldMapping.getVariableNames();
        SourceVariable found = null;
        if (variableNames.size() == 1) {
            String variableName = variableNames.get(0);
            for (SourceVariable sourceVariable : sipModel.getVariables()) {
                if (sourceVariable.getVariableName().equals(variableName)) {
                    found = sourceVariable;
                }
            }
        }
        return found;
    }

    private class ModelStateListener implements CompileModel.Listener {

        @Override
        public void stateChanged(final CompileModel.State state) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    switch (state) {
                        case PRISTINE:
                        case UNCOMPILED:
                            groovyCodeArea.setBackground(new Color(1.0f, 1.0f, 1.0f));
                            break;
                        case EDITED:
                            groovyCodeArea.setBackground(new Color(1.0f, 1.0f, 0.9f));
                            break;
                        case ERROR:
                            groovyCodeArea.setBackground(new Color(1.0f, 0.9f, 0.9f));
                            break;
                        case COMMITTED:
                            groovyCodeArea.setBackground(new Color(0.9f, 1.0f, 0.9f));
                            break;
                    }
                }
            });
        }
    }
}