/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.core.querymodel.beans;

import eu.europeana.core.querymodel.query.BriefDoc;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.definitions.annotations.Europeana;
import eu.europeana.definitions.annotations.Solr;
import org.apache.solr.client.solrj.beans.Field;

import static eu.europeana.core.querymodel.beans.BeanUtil.returnStringOrElse;
import static eu.europeana.definitions.annotations.FieldCategory.ESE_PLUS;
import static eu.europeana.definitions.annotations.FieldCategory.INDEX_TIME_ADDITION;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:16:46 AM       
 */

public class BriefBean extends IdBean implements BriefDoc {

    transient int index;

    @Europeana(category = ESE_PLUS, required = true)
    @Solr(prefix = "europeana", localName = "europeanaCollectionName", multivalued = false, required = true)
    @Field("europeana_collectionName")
    String[] europeanaCollectionName;

    @Field("PROVIDER")
    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "prov", briefDoc = true)
    @Solr(fieldType = "string")
    String[] provider;

    @Europeana(required = true, briefDoc = true, object = true, url = true)
    @Solr(prefix = "europeana", localName = "object")
    @Field("europeana_object")
    String[] europeanaObject;

    @Field("COUNTRY")
    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "coun")
    @Solr(fieldType = "string")
    String[] country;

    @Field("TYPE")
    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "type", briefDoc = true, type = true)
    @Solr(localName = "type", fieldType = "string")
    String[] docType;

    @Field("LANGUAGE")
    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "lang", briefDoc = true)
    @Solr(fieldType = "string")
    String[] language;

    @Field("YEAR")
    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "yr", briefDoc = true, converter="extractYear")
    @Solr(fieldType = "string")
    String[] year;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, briefDoc = true)
    @Solr()
    String[] title;

    @Field
    @Solr()
    @Europeana(category = INDEX_TIME_ADDITION, briefDoc = true)
    String[] creator;

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getId() {
        return europeanaUri;
    }

    @Override
    public String getTitle() {
        return returnStringOrElse(title);
    }

    @Override
    public String getThumbnail() {
        return returnStringOrElse(europeanaObject);
    }

    @Override
    public String getCreator() {
        return returnStringOrElse(creator);
    }

    @Override
    public String getYear() {
        return returnStringOrElse(year);
    }

    @Override
    public String getProvider() {
        return returnStringOrElse(provider);
    }

    @Override
    public String getLanguage() {
        return returnStringOrElse(language);
    }

    @Override
    public DocType getType() {
        return DocType.get(docType);
    }

}
