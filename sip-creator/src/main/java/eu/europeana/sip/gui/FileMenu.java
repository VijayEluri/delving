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

import eu.europeana.sip.model.SipModel;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class FileMenu extends JMenu {
    private Component parent;
    private SipModel sipModel;
    private SelectListener selectListener;

    public interface SelectListener {
        boolean selectInputFile(File inputFile);
        void selectDataSet(String spec);
    }

    public FileMenu(Component parent, SipModel sipModel, SelectListener selectListener) {
        super("File");
        this.parent = parent;
        this.sipModel = sipModel;
        this.selectListener = selectListener;
        refresh();
    }

    private class LoadNewFileAction extends AbstractAction {
        private JFileChooser chooser = new JFileChooser("XML File");

        private LoadNewFileAction(File directory) {
            super("Open File from " + directory.getAbsolutePath());
            chooser.setCurrentDirectory(directory);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".xml") || file.getName().endsWith(".xml.gz");
                }

                @Override
                public String getDescription() {
                    return "XML Files";
                }
            });
            chooser.setMultiSelectionEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int choiceMade = chooser.showOpenDialog(parent);
            if (choiceMade == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                sipModel.addRecentDirectory(file);
                selectListener.selectInputFile(file);
                refresh();
            }
        }
    }

    private class LoadDataSetAction extends AbstractAction {
        private String spec;

        private LoadDataSetAction(String spec) {
            super(String.format("Load Data Set %s", spec));
            this.spec = spec;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            selectListener.selectDataSet(spec);
        }
    }

    private void refresh() {
        removeAll();
        add(new LoadNewFileAction(new File("/")));
        List<String> directoryList = sipModel.getRecentDirectories();
        for (String directory : directoryList) {
            add(new LoadNewFileAction(new File(directory)));
        }
        addSeparator();
        for (String spec : sipModel.getFileStore().getDataSetSpecs()) {
            add(new LoadDataSetAction(spec));
        }
    }
}
