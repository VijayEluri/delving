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

import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.DataSetDetails;
import eu.europeana.sip.core.RecordRoot;
import eu.europeana.sip.model.AnalysisTree;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.QNameNode;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class AnalysisPanel extends JPanel {
    private static final String ELEMENTS_PROCESSED = "%d Elements Processed";
    private static final String RECORDS = "%d Records";
    private static final String PERFORM_ANALYSIS = "Analyze %s";
    private JButton selectRecordRootButton = new JButton("Select Record Root");
    private JButton selectUniqueElementButton = new JButton("Select Unique Element");
    private JLabel recordCountLabel = new JLabel(String.format(RECORDS, 0), JLabel.CENTER);
    private JButton analyzeButton = new JButton("Analyze");
    private JLabel elementCountLabel = new JLabel(String.format(ELEMENTS_PROCESSED, 0L), JLabel.CENTER);
    private JButton abortButton = new JButton("Abort");
    private ConstantFieldPanel constantFieldPanel;
    private JTree statisticsJTree;
    private SipModel sipModel;

    public AnalysisPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        this.constantFieldPanel = new ConstantFieldPanel(sipModel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0.5;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(createTreePanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(createStatisticsPanel(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        add(createVariablesPanel(), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(constantFieldPanel, gbc);

        gbc.weighty = 0.01;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(createAnalyzePanel(), gbc);
        wireUp();
    }

    private JPanel createTreePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Document Structure"));
        statisticsJTree = new JTree(sipModel.getAnalysisTreeModel());
        statisticsJTree.getModel().addTreeModelListener(new Expander());
        statisticsJTree.setCellRenderer(new AnalysisTreeCellRenderer());
        statisticsJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        p.add(scroll(statisticsJTree), BorderLayout.CENTER);
        JPanel bp = new JPanel(new GridLayout(1,0,10,10));
        selectRecordRootButton.setEnabled(false);
        selectRecordRootButton.setForeground(Color.RED);
        bp.add(selectRecordRootButton);
        selectUniqueElementButton.setForeground(Color.GREEN);
        selectUniqueElementButton.setEnabled(false);
        bp.add(selectUniqueElementButton);
        p.add(bp, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createStatisticsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Statistics"));
        JPanel tablePanel = new JPanel(new BorderLayout());
        JTable statsTable = new JTable(sipModel.getStatisticsTableModel(), createStatsColumnModel());
        statsTable.getTableHeader().setReorderingAllowed(false);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePanel.add(statsTable.getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(scroll(statsTable), BorderLayout.CENTER);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private DefaultTableColumnModel createStatsColumnModel() {
        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        columnModel.addColumn(new TableColumn(0));
        columnModel.getColumn(0).setHeaderValue("Percent");
        columnModel.getColumn(0).setMaxWidth(80);
        columnModel.addColumn(new TableColumn(1));
        columnModel.getColumn(1).setHeaderValue("Count");
        columnModel.getColumn(1).setMaxWidth(80);
        columnModel.addColumn(new TableColumn(2));
        columnModel.getColumn(2).setHeaderValue("Value");
        return columnModel;
    }

    private JPanel createVariablesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Source Fields"));
        JList list = new JList(sipModel.getVariablesListModel());
        p.add(scroll(list), BorderLayout.CENTER);
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 500));
        return scroll;
    }

    private JPanel createAnalyzePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Analysis Process"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 0.25;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        p.add(analyzeButton, gbc);
        gbc.gridx++;
        p.add(elementCountLabel, gbc);
        gbc.gridx++;
        p.add(recordCountLabel, gbc);
        abortButton.setEnabled(false);
        gbc.gridx++;
        p.add(abortButton, gbc);
        return p;
    }

    private void setElementsProcessed(long count) {
        elementCountLabel.setText(String.format(ELEMENTS_PROCESSED, count));
    }

    private void wireUp() {
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
                constantFieldPanel.refresh();
            }

            @Override
            public void updatedFileSet(FileSet fileSet) {
                setElementsProcessed(sipModel.getElementCount());
                analyzeButton.setText(String.format(PERFORM_ANALYSIS, fileSet.getName()));
                analyzeButton.setEnabled(true);
                abortButton.setEnabled(false);
            }

            @Override
            public void updatedDetails(DataSetDetails dataSetDetails) {
            }

            @Override
            public void updatedRecordRoot(RecordRoot recordRoot) {
                recordCountLabel.setText(String.format(RECORDS, recordRoot == null ? 0 : recordRoot.getRecordCount()));
            }

            @Override
            public void updatedConstantFieldModel(ConstantFieldModel constantFieldModel) {
                constantFieldPanel.refresh();
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
            }
        });
        statisticsJTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TreePath path = event.getPath();
                AnalysisTree.Node node = (AnalysisTree.Node) path.getLastPathComponent();
                selectRecordRootButton.setEnabled(node.couldBeRecordRoot());
                selectUniqueElementButton.setEnabled(!node.couldBeRecordRoot());
                sipModel.selectNode(node);
            }
        });
        selectRecordRootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = statisticsJTree.getSelectionPath();
                QNameNode node = (QNameNode) path.getLastPathComponent();
                RecordRoot recordRoot = new RecordRoot(node.getQName(), node.getStatistics().getTotal());
                sipModel.setRecordRoot(recordRoot);
            }
        });
        selectUniqueElementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = statisticsJTree.getSelectionPath();
                QNameNode node = (QNameNode) path.getLastPathComponent();
                sipModel.setUniqueElement(node.getQName());
            }
        });
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeButton.setEnabled(false);
                abortButton.setEnabled(true);
                sipModel.analyze(new SipModel.AnalysisListener() {

                    @Override
                    public void finished(boolean success) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                analyzeButton.setEnabled(true);
                                abortButton.setEnabled(false);
                                setElementsProcessed(sipModel.getElementCount());
                            }
                        });
                    }

                    @Override
                    public void analysisProgress(final long elementCount) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setElementsProcessed(elementCount);
                            }
                        });
                    }
                });
            }
        });
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.abortAnalyze();
                analyzeButton.setEnabled(true);
            }
        });
    }

    private class AnalysisTreeCellRenderer extends DefaultTreeCellRenderer {
        private Font normalFont, thickFont;

        @Override
        public Component getTreeCellRendererComponent(JTree jTree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(jTree, value, selected, expanded, leaf, row, hasFocus);
            AnalysisTree.Node node = (AnalysisTree.Node) value;
            label.setFont(node.getStatistics() != null ? getThickFont() : getNormalFont());
            if (node.isRecordRoot()) {
                label.setForeground(Color.RED);
            }
            else if (node.isUniqueElement()) {
                label.setForeground(Color.GREEN);
            }
            return label;
        }

        private Font getNormalFont() {
            if (normalFont == null) {
                normalFont = super.getFont();
            }
            return normalFont;
        }

        private Font getThickFont() {
            if (thickFont == null) {
                thickFont = new Font(getNormalFont().getFontName(), Font.BOLD, getNormalFont().getSize());
            }
            return thickFont;
        }
    }

    private class Expander implements TreeModelListener {

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    expandEmptyNodes((AnalysisTree.Node) statisticsJTree.getModel().getRoot());
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        private void expandEmptyNodes(AnalysisTree.Node node) {
            if (node.couldBeRecordRoot()) {
                TreePath path = node.getTreePath();
                statisticsJTree.expandPath(path);
            }
            for (AnalysisTree.Node childNode : node.getChildNodes()) {
                expandEmptyNodes(childNode);
            }
        }
    }
}
