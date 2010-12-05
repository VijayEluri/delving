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

import eu.delving.metadata.FieldMapping;
import eu.europeana.sip.model.CompileModel;
import eu.europeana.sip.model.FieldMappingListModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
    private JButton dictionaryButton = new JButton("Edit Dictionary");
    private JList mappingList;

    public RefinementPanel(SipModel sipModel) {
        super(new BorderLayout());
        this.sipModel = sipModel;
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(createLeftSide());
        split.setRightComponent(createRightSide());
        split.setDividerLocation(0.5);
        add(split, BorderLayout.CENTER);
        wireUp();
    }

    private JPanel createLeftSide() {
        JPanel p = new JPanel(new BorderLayout(5,5));
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
        dictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DictionaryDialog dialog = new DictionaryDialog(
                        (Frame)SwingUtilities.getWindowAncestor(RefinementPanel.this),
                        sipModel.getFieldCompileModel().getSelectedFieldMapping()
                );
                dialog.setVisible(true);
            }
        });
        mappingList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    sipModel.getFieldCompileModel().setSelectedPath(fieldMapping.getFieldDefinition().path.toString());
                    dictionaryButton.setEnabled(fieldMapping.valueMap != null);
                    removeMappingButton.setEnabled(true);
                }
                else {
                    sipModel.getFieldCompileModel().setSelectedPath(null);
                    dictionaryButton.setEnabled(false);
                    removeMappingButton.setEnabled(false);
                }
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
        JScrollPane scroll = new JScrollPane(groovyCodeArea);
        p.add(scroll, BorderLayout.CENTER);
        p.add(dictionaryButton, BorderLayout.SOUTH);
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