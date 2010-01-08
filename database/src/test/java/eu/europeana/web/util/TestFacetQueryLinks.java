/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
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

package eu.europeana.web.util;

import eu.europeana.query.Facet;
import eu.europeana.query.FacetCount;
import eu.europeana.query.FacetType;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test the FacetQueryLinks
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestFacetQueryLinks {

    @Test
    public void facetQueryLinks() throws Exception {
        List<FacetField> facets = new ArrayList<FacetField>();
        FacetField facet = new FacetField("LANGUAGE");
        facet.add("en", 1);
        facet.add("de", 1);
        facet.add("nl", 1);
        facets.add(facet);
        facet = new FacetField("YEAR");
        facet.add("1980", 1);
        facet.add("1981", 1);
        facet.add("1982", 1);
        facets.add(facet);
        SolrQuery query = new SolrQuery();
        query.addFacetField("LANGUAGE", "YEAR");
        query.addFacetQuery("LANGUAGE:de");
        query.addFacetQuery("LANGUAGE:nl");
        query.addFacetQuery("YEAR:1980");
        List<FacetQueryLinks> facetLinks = FacetQueryLinks.createDecoratedFacets(query, facets);
        System.out.println();
        String[] expect = new String[]{
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=LANGUAGE:en'>en</a> (add)",
                "<a href='&qf=LANGUAGE:nl&qf=YEAR:1980'>de</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=YEAR:1980'>nl</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl'>1980</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=YEAR:1981'>1981</a> (add)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=YEAR:1982'>1982</a> (add)",
        };
        int index = 0;
        for (FacetQueryLinks facetLink : facetLinks) {
            for (FacetQueryLinks.FacetCountLink link : facetLink.getLinks()) {
                System.out.println("\"" + link + "\",");
                assertEquals(expect[index++], link.toString());
            }
        }
    }

    private class FacetImpl implements Facet {
        private FacetType facetType;
        private List<FacetCount> counts = new ArrayList<FacetCount>();

        private FacetImpl(FacetType facetType) {
            this.facetType = facetType;
        }

        public FacetType getType() {
            return facetType;
        }

        public List<FacetCount> getCounts() {
            return counts;
        }
    }

    private class FacetCountImpl implements FacetCount {
        private String value;

        private FacetCountImpl(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public Integer getCount() {
            return 0;
        }
    }


}