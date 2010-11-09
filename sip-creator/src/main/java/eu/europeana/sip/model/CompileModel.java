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

package eu.europeana.sip.model;

import eu.delving.core.metadata.FieldMapping;
import eu.delving.core.metadata.MappingModel;
import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.RecordMapping;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCode;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This model is behind the scenario with input data, groovy code, and output record
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class CompileModel implements SipModel.ParseListener, MappingModel.Listener {
    private Logger log = Logger.getLogger(getClass());
    public final static int COMPILE_DELAY = 500;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private RecordMapping recordMapping;
    private MetadataRecord metadataRecord;
    private Document inputDocument = new PlainDocument();
    private Document codeDocument = new PlainDocument();
    private Document outputDocument = new PlainDocument();
    private CompileTimer compileTimer = new CompileTimer();
    private MetadataModel metadataModel;
    private Type type;
    private ToolCode toolCode;
    private RecordValidator recordValidator;
    private String selectedPath;
    private String editedCode;

    public enum Type {
        RECORD,
        FIELD
    }

    public enum State {
        UNCOMPILED,
        PRISTINE,
        EDITED,
        ERROR,
        COMMITTED
    }

    public CompileModel(Type type, MetadataModel metadataModel, ToolCode toolCode) {
        this.type = type;
        this.metadataModel = metadataModel;
        this.toolCode = toolCode;
    }

    @Override
    public void mappingChanged(RecordMapping recordMapping) {
        if (this.recordMapping != recordMapping) {
            log.info("New record mapping, selected path eliminated");
            this.selectedPath = null;
        }
        this.recordMapping = recordMapping;
        this.editedCode = null;
        SwingUtilities.invokeLater(new DocumentSetter(codeDocument, getDisplayCode()));
        notifyStateChange(State.PRISTINE);
        compileSoon();
    }

    public void setSelectedPath(String selectedPath) {
        this.selectedPath = selectedPath;
        log.info("Selected path "+selectedPath);
        SwingUtilities.invokeLater(new DocumentSetter(codeDocument, getDisplayCode()));
        notifyStateChange(State.PRISTINE);
        compileSoon();
    }

    public FieldMapping getSelectedFieldMapping() {
        if (selectedPath == null) {
            return null;
        }
        return recordMapping.getFieldMapping(selectedPath);
    }

    public void setRecordValidator(RecordValidator recordValidator) {
        this.recordValidator = recordValidator;
    }

    public void refreshCode() {
        SwingUtilities.invokeLater(new DocumentSetter(codeDocument, getDisplayCode()));
        compileSoon();
    }

    public void compileSoon() {
        compileTimer.triggerSoon();
    }

    public void setCode(String code) {
        if (selectedPath != null) {
            FieldMapping fieldMapping = recordMapping.getFieldMapping(selectedPath);
            if (fieldMapping != null) {
                if (!fieldMapping.codeLooksLike(code)) {
                    editedCode = code;
                    log.info("Code looks different");
                    notifyStateChange(State.EDITED);
                }
                else {
                    editedCode = null;
                    log.info("Code looks the same");
                    notifyStateChange(State.PRISTINE);
                }
            }
            else {
                log.warn("Field mapping not found for "+selectedPath);
            }
            compileSoon();
        }
        else {
            log.info("setCode with no selected path");
        }
    }

    @Override
    public void updatedRecord(MetadataRecord metadataRecord) {
        this.metadataRecord = metadataRecord;
        if (metadataRecord == null) {
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, "No input"));
            SwingUtilities.invokeLater(new DocumentSetter(outputDocument, ""));
        }
        else {
            updateInputDocument(metadataRecord);
            compileSoon();
        }
    }

    public Document getInputDocument() {
        return inputDocument;
    }

    public Document getCodeDocument() {
        return codeDocument;
    }

    public Document getOutputDocument() {
        return outputDocument;
    }

    public String toString() {
        return type.toString();
    }

    // === privates

    private String getDisplayCode() {
        switch (type) {
            case RECORD:
                return recordMapping.toDisplayCode(metadataModel.getRecordDefinition());
            case FIELD:
                if (selectedPath == null) {
                    return "// no code";
                }
                else {
                    return recordMapping.toDisplayCode(metadataModel.getRecordDefinition(), selectedPath);
                }
            default:
                throw new RuntimeException();
        }
    }

    private String getCompileCode() {
        switch (type) {
            case RECORD:
                return recordMapping.toCompileCode(metadataModel.getRecordDefinition());
            case FIELD:
                if (selectedPath == null) {
                    return "print 'nothing selected'";
                }
                else {
                    return recordMapping.toCompileCode(metadataModel.getRecordDefinition(), selectedPath);
                }
            default:
                throw new RuntimeException();
        }
    }

    private String getCompileCode(String editedCode) {
        if (type == Type.RECORD) {
            throw new RuntimeException();
        }
        if (selectedPath == null) {
            return "print 'nothing selected'";
        }
        else {
            return recordMapping.toCompileCode(metadataModel.getRecordDefinition(), selectedPath, editedCode);
        }
    }

    private void updateInputDocument(MetadataRecord metadataRecord) {
        if (metadataRecord != null) {
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, metadataRecord.toString()));
        }
        else {
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, "No Input"));
        }
    }

    private class CompilationRunner implements Runnable {

        @Override
        public void run() {
            if (metadataRecord == null) {
                return;
            }
            String mappingCode;
            if (editedCode == null) {
                mappingCode = getCompileCode();
                log.info("Edited code null, so get existing code");
            }
            else {
                mappingCode = getCompileCode(editedCode);
                log.info("Edited code used");
            }
            MappingRunner mappingRunner = new MappingRunner(toolCode.getCode() + mappingCode);
            try {
                String output = mappingRunner.runMapping(metadataRecord);
                if (recordValidator != null) {
                    List<String> problems = new ArrayList<String>();
                    String validated = recordValidator.validate(output, problems);
                    if (problems.isEmpty()) {
                        compilationComplete(validated);
                    }
                    else {
                        throw new RecordValidationException(metadataRecord, problems);
                    }
                }
                else {
                    compilationComplete(output);
                    if (editedCode == null) {
                        notifyStateChange(State.PRISTINE);
                    }
                    else {
                        FieldMapping fieldMapping = recordMapping.getFieldMapping(selectedPath);
                        if (fieldMapping != null) {
                            fieldMapping.setCode(editedCode);
                            notifyStateChange(State.COMMITTED);
                            editedCode = null;
                            notifyStateChange(State.PRISTINE);
                        }
                        else {
                            notifyStateChange(State.EDITED);
                        }
                    }
                }
            }
            catch (MappingException e) {
                compilationComplete(e.getMessage());
                notifyStateChange(State.ERROR);
            }
            catch (RecordValidationException e) {
                compilationComplete(e.toString());
                notifyStateChange(State.ERROR);
            }
        }

        private void compilationComplete(final String result) {
            SwingUtilities.invokeLater(new DocumentSetter(outputDocument, result));
        }

        public String toString() {
            return type.toString();
        }
    }

    private class DocumentSetter implements Runnable {

        private Document document;
        private String content;

        private DocumentSetter(Document document, String content) {
            this.document = document;
            this.content = content;
        }

        @Override
        public void run() {
            int docLength = document.getLength();
            try {
                document.remove(0, docLength);
                document.insertString(0, content, null);
            }
            catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class CompileTimer implements ActionListener {
        private Timer timer = new Timer(COMPILE_DELAY, this);

        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            executor.execute(new CompilationRunner());
        }

        public void triggerSoon() {
            timer.restart();
        }
    }

    private void notifyStateChange(State state) {
        for (Listener listener : listeners) {
            listener.stateChanged(state);
        }
    }

    private static void checkSwingThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Swing thread");
        }
    }

    public interface Listener {
        void stateChanged(State state);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
}