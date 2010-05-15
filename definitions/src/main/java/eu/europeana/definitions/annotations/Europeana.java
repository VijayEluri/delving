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

package eu.europeana.definitions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is the annotation which describes the europeana aspects of a
 * field in one of the beans being used.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Europeana {

    /**
     * Is this field one of the facets?
     *
     * @return true if it is to be a facet
     */

    boolean facet() default false;

    /**
     * A prefix for a facet, must be unique
     *
     * @return a string prefix
     */

    String facetPrefix() default "";

    /**
     * This field will appear in the brief doc rendering
     *
     * @return true if it will
     */

    boolean briefDoc() default false;

    /**
     * This field will appear in the full doc rendering
     *
     * @return true if it will
     */

    boolean fullDoc() default true;

    /**
     * This field is hidden
     *
     * @return true if it should not appear
     */

    boolean hidden() default false;

    /**
     * Is this the europeana id to use
     *
     * @return true if it is
     */

    boolean id() default false;

    /**
     * Is this an object to which the record refers?
     *
     * @return true if it is
     */

    boolean object() default false;

    /**
     * Is this the europeana type?
     *
     * @return true if it is
     */

    boolean type() default false;

    /**
     * Is this field mappable (Source Field to Europeana Field). The are quite a few fields that are created through
     * Solr copyfields and are therefore not directly mappable during the mapping face.
     *
     * Note: CopyFields can never be mappable
     *
     * @return true if it is
     */
    boolean mappable() default false;

    /**
     * There are some fields that are added by the Europeana System during the IngestionPhase based on meta-information
     * provided with the DataSet during submission.
     *
     * These fields are therefore unmappable and must be kept separated during ESE validation and only used during ESE+
     * validation.
     *
     * When EDM is adopted as the internal datamodel the same will apply. These fields need to be kept separate during
     * initial import validation and only be validated during the validation of the Archival Information Packages.
     *
     *
     * @return true if it is
     */

    boolean importAddition() default false;

    /**
     *  The annotated fields can be valid at different levels in the application. The ValidationLevel will be used to
     * create a Data Model Validator to be used at various stages of ingestion.
     *
     * The validator should also be used in the Sip-Creator and could be used in external applications. 
     *
     * @return the validation level of a certain field
     */

    ValidationLevel validation() default ValidationLevel.ESE_OPTIONAL;

    /**
     * The converter is the name of the groovy method in ToolCode.groovy which is to be applied to the
     * values of this field when it is normalized.
     *
     * @return the name of the converter method in ToolCode.groovy
     */

    String converter() default "";
}
