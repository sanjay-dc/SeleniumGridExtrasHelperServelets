package com.ariba.es.ai.hub.proxy.servlets;

import com.ariba.es.ai.hub.proxy.clients.RequestForwardingClient;
import com.ariba.es.ai.hub.proxy.clients.RequestForwardingClientProvider;
import com.ariba.es.ai.hub.proxy.sessions.SeleniumSessions;
import com.google.common.annotations.VisibleForTesting;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by i335366 on 23/11/16.
 */
public class SeleniumApiProxyServlet extends RegistryBasedServlet {
    private static final Logger LOGGER = Logger.getLogger(SeleniumApiProxyServlet.class.getName());

    @VisibleForTesting
    RequestForwardingClientProvider requestForwardingClientProvider;

    @SuppressWarnings("unused")
    public SeleniumApiProxyServlet() {
        this(null);
    }

    public SeleniumApiProxyServlet(Registry registry) {
        super(registry);
        requestForwardingClientProvider = new RequestForwardingClientProvider();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forwardRequest(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forwardRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forwardRequest(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forwardRequest(req, resp);
    }

    private void forwardRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestForwardingClient requestForwardingClient;
        try {
            requestForwardingClient = createExtensionClient(req.getPathInfo(),req.getQueryString());
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        try {
            requestForwardingClient.forwardRequest(req, resp);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception during request forwarding", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private RequestForwardingClient createExtensionClient(String path,String query) {
        LOGGER.info("Forwarding request with path: " + path);
        String sessionId = SeleniumSessions.getSessionIdFromPath(path);
        LOGGER.info("Retrieving remote host for session: " + sessionId);

        SeleniumSessions sessions = new SeleniumSessions(getRegistry());
        sessions.refreshTimeout(sessionId);

        URL remoteHost = sessions.getRemoteHostForSession(sessionId);
        String host = remoteHost.getHost();
        int port = 3000;
//        int port = remoteHost.getPort();
//        String api = "screenshot";
        String api = SeleniumSessions.getApiFromPath(path);
        String queryParameters = query;
        LOGGER.info("Remote host retrieved: " + host + ":" + port);
        return requestForwardingClientProvider.provide(host, port, api, queryParameters);
    }
}
