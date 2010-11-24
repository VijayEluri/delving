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

import eu.delving.metadata.Path;
import eu.delving.sip.FileStore;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormalizationPanel extends JPanel {
    private SipModel sipModel;
    private JCheckBox discardInvalidBox = new JCheckBox("Discard Invalid Records");
    private JCheckBox storeNormalizedBox = new JCheckBox("Store Normalized XML");
    private JButton normalizeButton = new JButton("Normalize");
    private JButton abortButton = new JButton("Abort");
    private JLabel normalizeMessageLabel = new JLabel("?", JLabel.CENTER);

    public NormalizationPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;
        gbc.weightx = 0.333;
        gbc.weighty = 0.99;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        add(new RecordPanel(sipModel, sipModel.getRecordCompileModel()), gbc);
        gbc.gridx++;
        add(createCodePanel(), gbc);
        gbc.gridx++;
        add(createOutputPanel(), gbc);
        gbc.weighty = 0.01;
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy++;
        add(createNormalizePanel(), gbc);
        wireUp();
    }

    private JPanel createCodePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        JTextArea area = new JTextArea(sipModel.getRecordCompileModel().getCodeDocument());
        area.setEditable(false);
        p.add(scroll(area));
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Record"));
        JTextArea area = new JTextArea(sipModel.getRecordCompileModel().getOutputDocument());
        area.setEditable(false);
        p.add(scroll(area));
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 800));
        return scroll;
    }

    private JPanel createNormalizePanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(createNormalizeCenter(), BorderLayout.CENTER);
        p.add(createNormalizeEast(), BorderLayout.EAST);
        return p;
    }

    private JPanel createNormalizeCenter() {
        JProgressBar progressBar = new JProgressBar(sipModel.getNormalizeProgress());
        progressBar.setBorderPainted(true);
        progressBar.setStringPainted(true);
        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Progress"));
        p.add(progressBar);
        p.add(normalizeMessageLabel);
        return p;
    }

    private JPanel createNormalizeEast() {
        JPanel p = new JPanel(new GridLayout(2, 0, 5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Control"));
        p.add(discardInvalidBox);
        p.add(storeNormalizedBox);
        p.add(normalizeButton);
        p.add(abortButton);
        BoundedRangeModel m = sipModel.getNormalizeProgress();
        abortButton.setEnabled(m.getValue() > m.getMinimum() && m.getValue() < m.getMaximum());
        return p;
    }

    private void wireUp() {
        sipModel.getNormalizeProgress().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                BoundedRangeModel m = sipModel.getNormalizeProgress();
                abortButton.setEnabled(m.getValue() > m.getMinimum() && m.getValue() < m.getMaximum());
            }
        });
        normalizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.normalize(discardInvalidBox.isSelected(), storeNormalizedBox.isSelected());
            }
        });
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.abortNormalize();
            }
        });
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedDataSetStore(FileStore.DataSetStore store) {
            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
                normalizeMessageLabel.setText(message);
            }
        });
    }
}