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

package eu.europeana.bootstrap;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Bootstrap the entire system, including the portal, resolver and cache servlet
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EuropeanaIntegration {

    public static void main(String... args) throws Exception {
        System.setProperty("solr.solr.home", "./bootstrap/src/test/solr/solr");
        int port = 8983;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
//        server.addHandler(new WebAppContext("./resolver/src/main/webapp", "/resolve"));
        server.addHandler(new WebAppContext("./bootstrap/src/test/resources/integration/0.3.1/cache.war", "/cache"));
        server.addHandler(new WebAppContext("./bootstrap/src/test/resources/integration/0.3.1/portal.war", "/portal"));
        server.addHandler(new WebAppContext("./bootstrap/src/test/resources/integration/0.3.1/solr.war", "/solr"));
        server.addHandler(new WebAppContext("./bootstrap/src/test/resources/integration/0.3.1/dashboard.war", "/dashboard"));
        server.start();
    }
}