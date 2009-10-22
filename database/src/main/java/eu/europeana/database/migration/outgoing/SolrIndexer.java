/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.database.migration.outgoing;

import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.query.ESERecord;

import java.util.List;

/**
 * An interface that lets us send commands to Solr
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface SolrIndexer {

    /**
     * Send a number of records to the index in one request.
     *
     * @param recordList a list of record objects, defined below
     * @return true if it worked
     */
    
    boolean indexRecordList(List<Record> recordList);

    /**
     * Reindex a single europeanaId
     *
     * @param europeanaId which one to reindex
     * @return true if it worked
     */

    boolean indexSingleRecord(EuropeanaId europeanaId);

    /**
     * Delete an entire collection from the index
     *
     * @param collectionName what is it called?
     * @return true if it worked
     */
    
    boolean deleteCollectionByName(String collectionName);

    /**
     * For indexing record lists, combine the id with the record's fields
     */
    
    public class Record {
        private EuropeanaId europeanaId;
        private ESERecord eseRecord;

        public Record(EuropeanaId europeanaId, ESERecord eseRecord) {
            this.europeanaId = europeanaId;
            this.eseRecord = eseRecord;
        }

        public EuropeanaId getEuropeanaId() {
            return europeanaId;
        }

        public ESERecord getEseRecord() {
            return eseRecord;
        }
    }
}