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

package eu.europeana.controller;

import eu.europeana.controller.util.DocIdWindowPager;
import eu.europeana.database.UserDao;
import eu.europeana.database.dao.DashboardDaoImpl;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.query.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Show an individual resolved document
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class FullDocController extends AbstractPortalController {
    private static final String URI_COUNTED = "uriCounted";
    private QueryModelFactory queryModelFactory;

    private UserDao userDao;
    private boolean countViewed;
    private DashboardDaoImpl dashboardDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setQueryModelFactory(QueryModelFactory queryModelFactory) {
        this.queryModelFactory = queryModelFactory;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("full-doc");
        String format = request.getParameter("format");
        if (format != null && format.equals("srw")) {
            model.setView("full-doc-srw");
            model.setContentType("text/xml;charset=UTF-8");
        }
        String uri = request.getParameter("uri");
        if (uri == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString()); // Expected uri query parameter
        }

        QueryModel queryModel = queryModelFactory.createQueryModel(QueryModelFactory.SearchType.SIMPLE);
        if (request.getParameter("query") != null && request.getParameter("start") != null) {
            model.put("pagination", new DocIdWindowPager(uri, request, queryModel));
        }
        queryModel.setResponseType(ResponseType.SINGLE_FULL_DOC);
        queryModel.setStartRow(0);
        queryModel.setQueryExpression(new UriExpression(uri));
        ResultModel resultModel = queryModel.fetchResult();
        if (resultModel.isMissingFullDoc()) {
            EuropeanaId id = dashboardDao.fetchEuropeanaId(uri);
            if (id != null && id.isOrphan()) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_REVOKED.toString());
            }
            else if (id != null && id.getCollection().getCollectionState() != CollectionState.ENABLED) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_INDEXED.toString());
            }
            else {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_FOUND.toString());
            }
        }
        model.put("result", resultModel);
        model.put("uri",uri);
        if (format != null && format.equalsIgnoreCase("labels")) {
            model.put("format", format);
        }
    }

    public void setDashboardDao(DashboardDaoImpl dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    private class UriExpression implements QueryExpression {
        private String uri;

        private UriExpression(String uri) {
            this.uri = uri;
        }

        public String getQueryString() {
            return getBackendQueryString();
        }

        public String getBackendQueryString() {
            return RecordField.EUROPEANA_URI.toFieldNameString()+":\""+uri+"\"";
        }

        public Type getType() {
            return Type.MORE_LIKE_THIS_QUERY;
        }

        public boolean isMoreLikeThis() {
            return true;
        }
    }
}