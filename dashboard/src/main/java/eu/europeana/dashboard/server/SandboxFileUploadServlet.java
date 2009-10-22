package eu.europeana.dashboard.server;

import eu.europeana.dashboard.server.incoming.ImportRepository;

/**
 * a file upload servlet for non-normalized sandbox files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SandboxFileUploadServlet extends FileUploadServlet {
    private static final long serialVersionUID = -7966505842467718930L;

    protected ImportRepository getRepository() {
        return HostedModeServiceLoader.getSandboxImportRepository();
    }
}