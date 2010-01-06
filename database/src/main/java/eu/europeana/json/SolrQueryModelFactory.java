package eu.europeana.json;

import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
import org.apache.commons.httpclient.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SolrQueryModelFactory implements QueryModelFactory {
    private HttpClient httpClient;
    private String baseUrl;

    @Value("#{europeanaProperties['solr.selectUrl']}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Autowired
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public QueryModel createQueryModel() {
        SolrQueryModel queryModel = new SolrQueryModel();
        queryModel.setSolrBaseUrl(baseUrl);
        queryModel.setHttpClient(httpClient);
        return queryModel;
    }

}