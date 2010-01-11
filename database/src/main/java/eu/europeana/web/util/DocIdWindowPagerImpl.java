package eu.europeana.web.util;

import eu.europeana.beans.IdBean;
import eu.europeana.query.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DocIdWindowPagerImpl implements DocIdWindowPager {

    private DocIdWindow docIdWindow;
    private boolean hasNext;
    private String nextUri;
    private int nextInt;
    private boolean hasPrevious;
    private String previousUri;
    private int previousInt;
    private String fullDocUri;
    private String queryStringForPaging;
    private String startPage;
    private String query;
    private int fullDocUriInt;
    private String returnToResults;
    private String pageId;
    private String tab;


    public DocIdWindowPagerImpl(String uri, HttpServletRequest request, QueryModel queryModel) throws EuropeanaQueryException, UnsupportedEncodingException {
        fullDocUri = uri;
        query = request.getParameter("query");
        startPage = request.getParameter("startPage");
        if (startPage == null) {
            startPage = "1";
        }
        tab = request.getParameter("tab");
        if (tab == null) {
            tab = "all";
        }
        QueryConstraints queryConstraints = new QueryConstraints(request.getParameterValues(QueryConstraints.PARAM_KEY));
        queryStringForPaging = createQueryStringForPaging(query, queryConstraints);
        queryModel.setResponseType(ResponseType.DOC_ID_WINDOW);
        queryModel.setQueryConstraints(queryConstraints);
        String startParam = request.getParameter("start");
        pageId = request.getParameter("pageId");
        if (pageId != null) {
            returnToResults = createReturnPage(query, startPage, pageId, tab, request);
        }
        if (startParam != null) {
            fullDocUriInt = Integer.valueOf(startParam);
        }
        int startRow = fullDocUriInt;
        hasPrevious = fullDocUriInt > 1;
        if (hasPrevious) {
            startRow -= 2;
        }
        queryModel.setStartRow(startRow);
        queryModel.setQueryString(query);
        ResultModel resultModel = queryModel.fetchResult();
        docIdWindow = resultModel.getDocIdWindow();
        List<String> ids = docIdWindow.getIds();
        hasNext = docIdWindow.getOffset() + 1 < docIdWindow.getHitCount();
        if (fullDocUriInt > docIdWindow.getHitCount() || docIdWindow.getIds().size() < 2) {
            hasPrevious = false;
            hasNext = false;
        }
        if (hasPrevious) {
            previousInt = fullDocUriInt - 1;
            previousUri = ids.get(0);
        }
        if (hasNext) {
            nextInt = fullDocUriInt + 1;
            nextUri = ids.get(2);
        }
    }

    // todo implement contstructor
    // Warning completely untested
    public DocIdWindowPagerImpl(Map<String, String[]> params, SolrQuery solrQuery, CommonsHttpSolrServer solrServer) throws Exception {
        if (params.get("uri") == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString()); // Expected uri query parameter
        }
        fullDocUri = params.get("uri")[0];
        startPage = params.get("startPage")[0];
        if (startPage == null) {
            startPage = "1";
        }
        tab = params.get("tab")[0];
        if (tab == null) {
            tab = "all";
        }
        solrQuery.setFields("europeana_uri");
        String startParam = params.get("start")[0];
        pageId = params.get("pageId")[0];
//        if (pageId != null) {
        // todo uncomment and implemnent createReturnPage with params instead of request
//            returnToResults = createReturnPage(query, startPage, pageId, tab, request);
//        }
        if (startParam != null) {
            fullDocUriInt = Integer.valueOf(startParam);
        }
        int startRow = fullDocUriInt;
        hasPrevious = fullDocUriInt > 1;
        if (hasPrevious) {
            startRow -= 2;
        }
        solrQuery.setStart(startRow);
        solrQuery.setRows(3);
        // Fetch results from server
        QueryResponse queryResponse = solrServer.query(solrQuery);
        // fetch beans
        List<IdBean> list = queryResponse.getBeans(IdBean.class);
        // populate this DocIdWindowPager object
        int offSet = Integer.parseInt(queryResponse.getHeader().get("start").toString());
        int hitCount = Integer.parseInt(queryResponse.getHeader().get("numFound").toString());
        hasNext = offSet + 1 < hitCount;
        if (fullDocUriInt > hitCount|| list.size() < 2) {
            hasPrevious = false;
            hasNext = false;
        }
        if (hasPrevious) {
            previousInt = fullDocUriInt - 1;
            previousUri = list.get(0).getEuropeanaUri();
        }
        if (hasNext) {
            nextInt = fullDocUriInt + 1;
            nextUri = list.get(2).getEuropeanaUri();
        }
    }

    private String createReturnPage(String query, String startPage, String pageId, String tab, HttpServletRequest request) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        if (pageId.equalsIgnoreCase("bd")) {
            builder.append("brief-doc.html?");
            builder.append("query=").append(URLEncoder.encode(query, "utf-8"));
            // todo: add fq parameters
        }
        else if (pageId.equalsIgnoreCase("yg")) {
            builder.append("year-grid.html?");
            if (query.length() > 4) {
                String userQueryString = query.replaceFirst("^\\d{4}", "").trim();
                builder.append("query=").append(URLEncoder.encode(userQueryString, "utf-8")).append("&");
            }
            builder.append("bq=").append(URLEncoder.encode(query, "utf-8"));
        }
        builder.append("&start=").append(startPage);
        String view = request.getParameter("view");
        if (view == null) {
            view = "table";
        }
        builder.append("&view=").append(view);
        builder.append("&tab=").append(tab);
        return builder.toString();
    }

    private String createQueryStringForPaging(String query, QueryConstraints queryConstraints) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        queryString.append("query=").append(URLEncoder.encode(query, "utf-8"));
        if (queryConstraints != null) {
            queryString.append(queryConstraints.toQueryString());
        }
        queryString.append("&startPage=").append(startPage);
        return queryString.toString();
    }

    @Override
    public DocIdWindow getDocIdWindow() {
        return docIdWindow;
    }

    @Override
    public boolean isNext() {
        return hasNext;
    }

    @Override
    public boolean isPrevious() {
        return hasPrevious;
    }

    @Override
    public String getQueryStringForPaging() {
        return queryStringForPaging;
    }

    @Override
    public String getFullDocUri() {
        return fullDocUri;
    }

    @Override
    public int getFullDocUriInt() {
        return fullDocUriInt;
    }

    @Override
    public String getNextUri() {
        return nextUri;
    }

    @Override
    public int getNextInt() {
        return nextInt;
    }

    @Override
    public String getPreviousUri() {
        return previousUri;
    }

    @Override
    public int getPreviousInt() {
        return previousInt;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public String getReturnToResults() {
        return returnToResults;
    }

    @Override
    public String getPageId() {
        return pageId;
    }

    @Override
    public String getTab() {
        return tab;
    }

    @Override
    public String toString() {
        Map<String, String> elementMap = new LinkedHashMap<String, String>();
        elementMap.put("query", query);
        elementMap.put("queryStringForPaging", queryStringForPaging);
        elementMap.put("fullDocUri", fullDocUri);
        elementMap.put("fullDocInt", String.valueOf(fullDocUriInt));
        elementMap.put("fullDocStart", String.valueOf(docIdWindow.getOffset()));
        elementMap.put("hitCount", String.valueOf(docIdWindow.getHitCount()));
        elementMap.put("isPrevious", String.valueOf(hasPrevious));
        elementMap.put("previousInt", String.valueOf(previousInt));
        elementMap.put("previousUri", String.valueOf(previousUri));
        elementMap.put("isNext", String.valueOf(hasNext));
        elementMap.put("nextInt", String.valueOf(nextInt));
        elementMap.put("nextUri", String.valueOf(nextUri));
        elementMap.put("returnToResults", returnToResults);

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : elementMap.entrySet()) {
            builder.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n");
        }
        return builder.toString();
    }
}