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

import eu.europeana.sip.model.CompileModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Show the current parsed record, and allow for moving to next, and rewinding
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordPanel extends JPanel {
    private JButton nextButton = new JButton("Next");

    public RecordPanel(SipModel sipModel, CompileModel compileModel) {
        super(new BorderLayout());
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Input Record", scroll(createRecordView(compileModel)));
        tabs.addTab("Search", new RecordSearchPanel(sipModel, new Runnable() {
            @Override
            public void run() {
                tabs.setSelectedIndex(0);
            }
        }));
        add(tabs);
        setPreferredSize(new Dimension(240, 500));
        wireUp();
    }

    private JEditorPane createRecordView(CompileModel compileModel) {
        final JEditorPane recordView = new JEditorPane();
        recordView.setContentType("text/html");
        recordView.setDocument(compileModel.getInputDocument());
        recordView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        recordView.setCaretPosition(0);
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
            }
        });
        recordView.setEditable(false);
        return recordView;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(240, 300));
        return scroll;
    }

    private void wireUp() {
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                sipModel.nextRecord();
            }
        });
    }
}