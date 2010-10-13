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
import eu.europeana.core.querymodel.query.FullDoc;
import eu.europeana.definitions.annotations.Europeana;
import eu.europeana.definitions.annotations.Solr;
import eu.europeana.definitions.domain.*;
import org.apache.commons.lang.WordUtils;
import org.apache.solr.client.solrj.beans.Field;

import java.util.ArrayList;
import java.util.List;

import static eu.europeana.core.querymodel.beans.BeanUtil.returnArrayOrElse;
import static eu.europeana.core.querymodel.beans.BeanUtil.returnStringOrElse;
import static eu.europeana.definitions.annotations.FieldCategory.*;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class FullBean extends BriefBean implements FullDoc {

    // Europeana namespace
    @Europeana(requiredGroup = "europeana:type", type = true, enumClass = DocType.class)
    @Solr(prefix = "europeana", localName = "type", multivalued = false, fieldType = "string", toCopyField = {"TYPE"})
    @Field("europeana_type")
    String europeanaType;

    @Europeana(category = ESE_PLUS)
    @Solr(prefix = "europeana", localName = "userTag", toCopyField = {"text", "USERTAGS"})
    @Field("europeana_userTag")
    String[] europeanaUserTag;

    @Europeana(category = ESE_PLUS, requiredGroup = "europeana:language", constant = true, enumClass = Language.class)
    @Solr(prefix = "europeana", localName = "language", fieldType = "string", toCopyField = {"text", "LANGUAGE"})
    @Field("europeana_language")
    String[] europeanaLanguage;

    @Europeana(category = ESE_PLUS, requiredGroup = "europeana:country", constant = true, enumClass = Country.class)
    @Solr(prefix = "europeana", localName = "country")
    @Field("europeana_country")
    String[] europeanaCountry;

    // todo find out what this field is
    @Europeana(category = ESE_PLUS)
    @Solr(prefix = "europeana", localName = "source")
    @Field("europeana_source")
    String[] europeanaSource;

    @Europeana(requiredGroup = "europeana:isShownAt or europeana:isShownBy", url = true)
    @Solr(prefix = "europeana", localName = "isShownAt", fieldType = "string", toCopyField = {"text"})
    @Field("europeana_isShownAt")
    String[] europeanaisShownAt;

    @Europeana(requiredGroup = "europeana:isShownAt or europeana:isShownBy", url = true)
    @Solr(prefix = "europeana", localName = "isShownBy", fieldType = "string", toCopyField = {"text"})
    @Field("europeana_isShownBy")
    String[] europeanaisShownBy;

    @Europeana(category = ESE_PLUS)
    @Solr(prefix = "europeana", localName = "year", fieldType = "string", toCopyField = {"text", "YEAR"})
    @Field("europeana_year")
    String[] europeanaYear;

    @Europeana(category = ESE_PLUS)
    @Solr(prefix = "europeana", localName = "hasObject", fieldType = "boolean") // todo: make it required
    @Field("europeana_hasObject")
    boolean europeanahasObject; // todo: change this to europeanaHasObject (capitalization!)

    @Europeana(category = ESE_PLUS, requiredGroup = "europeana:provider", constant = true)
    @Solr(prefix = "europeana", localName = "provider", toCopyField = {"PROVIDER"})
    @Field("europeana_provider")
    String[] europeanaProvider;

    @Europeana(category = ESE_PLUS, requiredGroup = "europeana:dataProvider", constant = true)
    @Solr(prefix = "europeana", localName = "dataProvider", multivalued = false)
    @Field("europeana_provider")
    String[] europeanaDataProvider;

    @Europeana(category = ESE_PLUS, requiredGroup = "europeana:rights", constant = true)
    @Solr(prefix = "europeana", localName = "rights", multivalued = false)
    @Field("europeana_rights")
    String[] europeanaRights;

    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "coll", briefDoc = true)
    @Solr(fieldType = "string")
    @Field("COLLECTION")
    String[] collection;

    // Dublin Core / ESE fields
    @Europeana
    @Solr(prefix = "dc", localName = "coverage", toCopyField = {"text", "what", "subject"})
    @Field("dc_coverage")
    String[] dcCoverage;

    @Europeana
    @Solr(prefix = "dc", localName = "contributor", toCopyField = {"text", "who", "creator"})
    @Field("dc_contributor")
    String[] dcContributor;

    @Europeana
    @Solr(prefix = "dc", localName = "description", toCopyField = {"text", "description"})
    @Field("dc_description")
    String[] dcDescription;

    @Europeana
    @Solr(prefix = "dc", localName = "creator", toCopyField = {"text", "who", "creator"})
    @Field("dc_creator")
    String[] dcCreator;

    @Europeana(category = ESE, converter = "extractYear")
    @Solr(prefix = "dc", localName = "date", toCopyField = {"text", "when", "date"})
    @Field("dc_date")
    String[] dcDate;

    @Europeana
    @Solr(prefix = "dc", localName = "format", toCopyField = {"text"})
    @Field("dc_format")
    String[] dcFormat;

    @Europeana
    @Solr(prefix = "dc", localName = "identifier", toCopyField = {"text", "identifier"})
    @Field("dc_identifier")
    String[] dcIdentifier;

    @Europeana
    @Solr(prefix = "dc", localName = "language", toCopyField = {"text"})
    @Field("dc_language")
    String[] dcLanguage;

    @Europeana
    @Solr(prefix = "dc", localName = "publisher", toCopyField = {"text"})
    @Field("dc_publisher")
    String[] dcPublisher;

    @Europeana
    @Solr(prefix = "dc", localName = "relation", toCopyField = {"text", "relation"})
    @Field("dc_relation")
    String[] dcRelation;

    @Europeana
    @Solr(prefix = "dc", localName = "rights", toCopyField = {"text"})
    @Field("dc_rights")
    String[] dcRights;

    @Europeana
    @Solr(prefix = "dc", localName = "source", toCopyField = {"text"})
    @Field("dc_source")
    String[] dcSource;

    @Europeana
    @Solr(prefix = "dc", localName = "subject", toCopyField = {"text", "what", "subject"})
    @Field("dc_subject")
    String[] dcSubject;

    @Europeana
    @Solr(prefix = "dc", localName = "title", toCopyField = {"text"})
    @Field("dc_title")
    String[] dcTitle;

    @Europeana
    @Solr(prefix = "dc", localName = "type", toCopyField = {"text"})
    @Field("dc_type")
    String[] dcType;

    @Field("DCTYPE")
    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "dctype") // removed facetPrefix = "type",
    @Solr(localName = "dctype", fieldType = "string")
    String[] DCTYPE;

    // Dublin Core Terms extended / ESE fields
    @Europeana
    @Solr(prefix = "dcterms", localName = "alternative", toCopyField = {"text"})
    @Field("dcterms_alternative")
    String[] dctermsAlternative;

    @Europeana
    @Solr(prefix = "dcterms", localName = "created", toCopyField = {"text", "when", "date"})
    @Field("dcterms_created")
    String[] dctermsCreated;

    @Europeana
    @Solr(prefix = "dcterms", localName = "conformsTo", toCopyField = {"text"})
    @Field("dcterms_conformsTo")
    String[] dctermsConformsTo;

    @Europeana
    @Solr(prefix = "dcterms", localName = "extent", toCopyField = {"text", "format"})
    @Field("dcterms_extent")
    String[] dctermsExtent;

    @Europeana
    @Solr(prefix = "dcterms", localName = "hasFormat", toCopyField = {"text", "relation"})
    @Field("dcterms_hasFormat")
    String[] dctermsHasFormat;

    @Europeana
    @Solr(prefix = "dcterms", localName = "hasPart", toCopyField = {"text", "relation"})
    @Field("dcterms_hasPart")
    String[] dctermsHasPart;

    @Europeana
    @Solr(prefix = "dcterms", localName = "hasVersion", toCopyField = {"text", "relation"})
    @Field("dcterms_hasVersion")
    String[] dctermsHasVersion;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isFormatOf", toCopyField = {"text"})
    @Field("dcterms_isFormatOf")
    String[] dctermsIsFormatOf;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isPartOf", toCopyField = {"text"})
    @Field("dcterms_isPartOf")
    String[] dctermsIsPartOf;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isReferencedBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isReferencedBy")
    String[] dctermsIsReferencedBy;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isReplacedBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isReplacedBy")
    String[] dctermsIsReplacedBy;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isRequiredBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isRequiredBy")
    String[] dctermsIsRequiredBy;

    @Europeana
    @Solr(prefix = "dcterms", localName = "issued", toCopyField = {"text", "date"})
    @Field("dcterms_issued")
    String[] dctermsIssued;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isVersionOf", toCopyField = {"text"})
    @Field("dcterms_isVersionOf")
    String[] dctermsIsVersionOf;

    @Europeana
    @Solr(prefix = "dcterms", localName = "medium", toCopyField = {"text", "format"})
    @Field("dcterms_medium")
    String[] dctermsMedium;

    @Europeana
    @Solr(prefix = "dcterms", localName = "provenance", toCopyField = {"text"})
    @Field("dcterms_provenance")
    String[] dctermsProvenance;

    @Europeana
    @Solr(prefix = "dcterms", localName = "references", toCopyField = {"text"})
    @Field("dcterms_references")
    String[] dctermsReferences;

    @Europeana
    @Solr(prefix = "dcterms", localName = "replaces", toCopyField = {"text", "relation"})
    @Field("dcterms_replaces")
    String[] dctermsReplaces;

    @Europeana
    @Solr(prefix = "dcterms", localName = "requires", toCopyField = {"text", "relation"})
    @Field("dcterms_requires")
    String[] dctermsRequires;

    @Europeana
    @Solr(prefix = "dcterms", localName = "spatial", toCopyField = {"text", "where", "location", "subject"})
    @Field("dcterms_spatial")
    String[] dctermsSpatial;

    @Europeana
    @Solr(prefix = "dcterms", localName = "tableOfContents", toCopyField = {"text", "description"})
    @Field("dcterms_tableOfContents")
    String[] dctermsTableOfContents;

    @Europeana
    @Solr(prefix = "dcterms", localName = "temporal", toCopyField = {"text", "what", "subject"})
    @Field("dcterms_temporal")
    String[] dctermsTemporal;

    @Override
    public String getId() {
        return europeanaUri;
    }


    // todo: review these (also: what about querymodel.beans.*)

    @Europeana(category = ICN, converter = "extractYear")
    @Solr(prefix = "icn", localName = "creatorYearOfBirth", multivalued = false)
    @Field("icn_creatorYearOfBirth")
    String[] creatorYearOfBirth;

    @Europeana(category = ICN, enumClass = Technique.class, valueMapped = true)
    @Solr(prefix = "icn", localName = "technique")
    @Field("icn_technique")
    String[] technique;

    @Europeana(category = ICN)
    @Solr(prefix = "icn", localName = "material")
    @Field("icn_material")
    String[] material;

    @Europeana(category = ICN)
    @Solr(prefix = "icn", localName = "location")
    @Field("icn_location")
    String[] location;

    @Europeana(category = ICN)
    @Solr(prefix = "icn", localName = "province")
    @Field("icn_province")
    String[] province;

    @Europeana(category = ICN)
    @Solr(prefix = "icn", localName = "collectionPart")
    @Field("icn_collectionPart")
    String[] collectionPart;

    @Europeana(category = ICN_RESEARCH, enumClass = AcquisitionType.class, valueMapped = true)
    @Solr(prefix = "icn", localName = "acquisitionMeans")
    @Field("icn_acquisitionMeans")
    String[] acquisitionMeans;

    @Europeana(category = ICN_RESEARCH, enumClass = CollectionDisplayType.class)
    @Solr(prefix = "icn", localName = "collectionType", multivalued = false)
    @Field("icn_collectionType")
    String[] collectionType;

    @Europeana(category = ICN_RESEARCH)
    @Solr(prefix = "icn", localName = "acquisitionYear", multivalued = false)
    @Field("icn_acquisitionYear")
    String[] acquisitionYear;

    @Europeana(category = ICN_RESEARCH)
    @Solr(prefix = "icn", localName = "purchasePrice", multivalued = false)
    @Field("icn_purchasePrice")
    String[] purchasePrice;

    @Europeana(category = ICN_RESEARCH)
    @Solr(prefix = "icn", localName = "acquiredWithHelpFrom")
    @Field("icn_acquiredWithHelpFrom")
    String[] acquiredWithHelpFrom;

    @Europeana(category = ICN_RESEARCH)
    @Solr(prefix = "icn", localName = "physicalState")
    @Field("icn_physicalState")
    String[] physicalState;


    // todo: review above


    @Override
    public String[] getThumbnails() {
        return returnArrayOrElse(europeanaObject);
    }

    @Override
    public String[] getEuropeanaIsShownAt() {
        return returnArrayOrElse(europeanaisShownAt);
    }

    @Override
    public String[] getEuropeanaIsShownBy() {
        return returnArrayOrElse(europeanaisShownBy);
    }

    @Override
    public String[] getEuropeanaUserTag() {
        return returnArrayOrElse(europeanaUserTag);
    }

    @Override
    public Boolean getEuropeanaHasObject() {
        return europeanahasObject;
    }

    @Override
    public String[] getEuropeanaCountry() {
        final String[] countryArr = returnArrayOrElse(europeanaCountry, country);
        List<String> upperCasedCountries = new ArrayList<String>();
        for (String country : countryArr) {
            upperCasedCountries.add(WordUtils.capitalizeFully(country));
        }
        return upperCasedCountries.toArray(new String[upperCasedCountries.size()]);
    }

    @Override
    public String[] getEuropeanaProvider() {
        return returnArrayOrElse(europeanaProvider, provider);
    }

    @Override
    public String[] getEuropeanaSource() {
        return returnArrayOrElse(europeanaSource);
    }

    @Override
    public DocType getEuropeanaType() {
        return DocType.get(docType);
    }

    @Override
    public String[] getEuropeanaLanguage() {
        return returnArrayOrElse(europeanaLanguage, language);
    }

    @Override
    public String[] getEuropeanaYear() {
        return returnArrayOrElse(europeanaYear);
    }

    // DCTERMS fields

    @Override
    public String[] getDcTermsAlternative() {
        return returnArrayOrElse(dctermsAlternative);
    }

    @Override
    public String[] getDcTermsConformsTo() {
        return returnArrayOrElse(dctermsConformsTo);
    }

    @Override
    public String[] getDcTermsCreated() {
        return returnArrayOrElse(dctermsCreated);
    }

    @Override
    public String[] getDcTermsExtent() {
        return returnArrayOrElse(dctermsExtent);
    }

    @Override
    public String[] getDcTermsHasFormat() {
        return returnArrayOrElse(dctermsHasFormat);
    }

    @Override
    public String[] getDcTermsHasPart() {
        return returnArrayOrElse(dctermsHasPart);
    }

    @Override
    public String[] getDcTermsHasVersion() {
        return returnArrayOrElse(dctermsHasVersion);
    }

    @Override
    public String[] getDcTermsIsFormatOf() {
        return returnArrayOrElse(dctermsIsFormatOf);
    }

    @Override
    public String[] getDcTermsIsPartOf() {
        return returnArrayOrElse(dctermsIsPartOf);
    }

    @Override
    public String[] getDcTermsIsReferencedBy() {
        return returnArrayOrElse(dctermsIsReferencedBy);
    }

    @Override
    public String[] getDcTermsIsReplacedBy() {
        return returnArrayOrElse(dctermsIsReplacedBy);
    }

    @Override
    public String[] getDcTermsIsRequiredBy() {
        return returnArrayOrElse(dctermsIsRequiredBy);
    }

    @Override
    public String[] getDcTermsIssued() {
        return returnArrayOrElse(dctermsIssued);
    }

    @Override
    public String[] getDcTermsIsVersionOf() {
        return returnArrayOrElse(dctermsIsVersionOf);
    }

    @Override
    public String[] getDcTermsMedium() {
        return returnArrayOrElse(dctermsMedium);
    }

    @Override
    public String[] getDcTermsProvenance() {
        return returnArrayOrElse(dctermsProvenance);
    }

    @Override
    public String[] getDcTermsReferences() {
        return returnArrayOrElse(dctermsReferences);
    }

    @Override
    public String[] getDcTermsReplaces() {
        return returnArrayOrElse(dctermsReplaces);
    }

    @Override
    public String[] getDcTermsRequires() {
        return returnArrayOrElse(dctermsRequires);
    }

    @Override
    public String[] getDcTermsSpatial() {
        return returnArrayOrElse(dctermsSpatial);
    }

    @Override
    public String[] getDcTermsTableOfContents() {
        return returnArrayOrElse(dctermsTableOfContents);
    }

    @Override
    public String[] getDcTermsTemporal() {
        return returnArrayOrElse(dctermsTemporal);
    }

    @Override
    public String[] getDcContributor() {
        return returnArrayOrElse(dcContributor);
    }

    @Override
    public String[] getDcCoverage() {
        return returnArrayOrElse(dcCoverage);
    }

    @Override
    public String[] getDcCreator() {
        return returnArrayOrElse(dcCreator);
    }

    @Override
    public String[] getDcDate() {
        return returnArrayOrElse(dcDate);
    }

    @Override
    public String[] getDcDescription() {
        return returnArrayOrElse(dcDescription);
    }

    @Override
    public String[] getDcFormat() {
        return returnArrayOrElse(dcFormat);
    }

    @Override
    public String[] getDcIdentifier() {
        return returnArrayOrElse(dcIdentifier);
    }

    @Override
    public String[] getDcLanguage() {
        return returnArrayOrElse(dcLanguage);
    }

    @Override
    public String[] getDcPublisher() {
        return returnArrayOrElse(dcPublisher);
    }

    @Override
    public String[] getDcRelation() {
        return returnArrayOrElse(dcRelation);
    }

    @Override
    public String[] getDcRights() {
        return returnArrayOrElse(dcRights);
    }

    @Override
    public String[] getDcSource() {
        return returnArrayOrElse(dcSource);
    }

    @Override
    public String[] getDcSubject() {
        return returnArrayOrElse(dcSubject);
    }

    @Override
    public String[] getDcTitle() {
        return returnArrayOrElse(dcTitle);
    }

    @Override
    public String[] getDcType() {
        return returnArrayOrElse(dcType);
    }

    @Override
    public BriefDoc getBriefDoc() {
        return null;
    }

    @Override
    public String getEuropeanaCollectionName() {
        return returnStringOrElse(europeanaCollectionName);
    }

    @Override
    public String getEuropeanaCollectionTitle() {
        return returnStringOrElse(europeanaCollectionTitle);
    }

    @Override
    public String[] getCreatorYearOfBirth() {
        return returnArrayOrElse(creatorYearOfBirth);
    }

    @Override
    public String[] getTechnique() {
        return returnArrayOrElse(technique);
    }

    @Override
    public String[] getMaterial() {
        return returnArrayOrElse(material);
    }

    @Override
    public String[] getLocation() {
        return returnArrayOrElse(location);
    }

    @Override
    public String[] getProvince() {
        return returnArrayOrElse(province);
    }

    @Override
    public String[] getCollectionPart() {
        return returnArrayOrElse(collectionPart);
    }

    @Override
    public String[] getAcquisitionMeans() {
        return returnArrayOrElse(acquisitionMeans);
    }

    @Override
    public String[] getAcquisitionYear() {
        return returnArrayOrElse(acquisitionYear);
    }

    @Override
    public String[] getPurchasePrice() {
        return returnArrayOrElse(purchasePrice);
    }

    @Override
    public String[] getAcquiredWithHelpFrom() {
        return returnArrayOrElse(acquiredWithHelpFrom);
    }

    @Override
    public String[] getPhysicalState() {
        return returnArrayOrElse(physicalState);
    }

    @Override
    public String[] getCollectionType() {
        return returnArrayOrElse(collectionType);
    }
}

