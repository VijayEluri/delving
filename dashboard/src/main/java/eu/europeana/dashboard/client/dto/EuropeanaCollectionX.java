/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * A domain object to correspond with the EuropeanaCollection
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EuropeanaCollectionX implements IsSerializable {
    private Long id;
    private String name;
    private String description;
    private String fileName;
    private String fileUserName;
    private Date collectionLastModified;
    private ImportFile.State fileState = ImportFile.State.NONEXISTENT;
    private CacheStateX cacheState = CacheStateX.EMPTY;
    private CollectionStateX collectionState = CollectionStateX.EMPTY;
    private int totalRecords;
    private int totalObjects;
    private int totalOrphans;
    private String importError;

    public EuropeanaCollectionX() {
    }

    public EuropeanaCollectionX(String name) {
        this.name = name;
    }

    public EuropeanaCollectionX(Long id, String name, String description, String fileName, Date collectionLastModified, String fileUserName, ImportFile.State fileState, CacheStateX cacheState, CollectionStateX collectionState, int totalRecords, int totalObjects, int totalOrphans, String importError) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fileName = fileName;
        this.collectionLastModified = collectionLastModified;
        this.fileUserName = fileUserName;
        this.fileState = fileState;
        this.cacheState = cacheState;
        this.collectionState = collectionState;
        this.totalRecords = totalRecords;
        this.totalObjects = totalObjects;
        this.totalOrphans = totalOrphans;
        this.importError = importError;
    }

    public EuropeanaCollectionX(Long id, String name, String description, String fileName, Date collectionLastModified, String fileUserName, String fileState, String cacheState, String collectionState, int totalRecords, int totalObjects, int totalOrphans, String importError) {
        this(
                id,
                name,
                description,
                fileName,
                collectionLastModified,
                fileUserName,
                ImportFile.State.valueOf(fileState),
                CacheStateX.valueOf(cacheState),
                CollectionStateX.valueOf(collectionState),
                totalRecords,
                totalObjects,
                totalOrphans,
                importError
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUserName() {
        return fileUserName;
    }

    public void setFileUserName(String fileUserName) {
        this.fileUserName = fileUserName;
    }

    public Date getCollectionLastModified() {
        return collectionLastModified;
    }

    public void setCollectionLastModified(Date collectionLastModified) {
        this.collectionLastModified = collectionLastModified;
    }

    public ImportFile.State getFileState() {
        return fileState;
    }

    public void setFileState(ImportFile.State fileState) {
        this.fileState = fileState;
    }

    public CacheStateX getCacheState() {
        return cacheState;
    }

    public void setCacheState(CacheStateX cacheState) {
        this.cacheState = cacheState;
    }

    public CollectionStateX getCollectionState() {
        return collectionState;
    }

    public void setCollectionState(CollectionStateX collectionState) {
        this.collectionState = collectionState;
    }

    public int getTotalObjects() {
        return totalObjects;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getTotalOrphans() {
        return totalOrphans;
    }

    public String getImportError() {
        return importError;
    }

    public String toString() {
        return "Collection("+name+")";
    }
}