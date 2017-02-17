package com.ai.hub.proxy.servlets;

import com.google.common.collect.Sets;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSession;
import org.seleniumhq.jetty7.server.Server;
import org.seleniumhq.jetty7.servlet.ServletContextHandler;
import org.seleniumhq.jetty7.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by i335366 on 24/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumApiProxyServletTests {

    @Mock
    private Function mockedFunction;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Registry mockedRegistry;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestSession mockedSession;

    private int hubPort;
    private Server hubServer;
    private Server stubServer;

    @Before
    public void setUp() throws Exception {
        StubServlet stubServlet = new StubServlet(mockedFunction);
        SeleniumApiProxyServlet hubRequestsProxyingServlet = new SeleniumApiProxyServlet(mockedRegistry);

        hubServer = startServerForServlet(hubRequestsProxyingServlet, "/" + SeleniumApiProxyServlet.class.getSimpleName() + "/*");
        hubPort = hubServer.getConnectors()[0].getLocalPort();

        stubServer = startServerForServlet(stubServlet, "/extra/stubbyExtension/*");

        URL url = new URIBuilder("http://localhost:" + stubServer.getConnectors()[0].getLocalPort())
                .build()
                .toURL();

        //Mock that registry contains session with url to redirect to
        when(mockedRegistry.getActiveSessions()).thenReturn(Sets.newHashSet(mockedSession));
        when(mockedSession.getExternalKey().getKey()).thenReturn("session_id");
        when(mockedSession.getSlot().getProxy().getRemoteHost()).thenReturn(url);
    }

    @After
    public void tearDown() throws Exception {
        hubServer.stop();
        stubServer.stop();
    }

    @Test
    public void shouldRedirectGetRequestAndTrimPathParams() throws IOException {
        doAnswer(verifyRequestPath())
                .when(mockedFunction)
                .apply(any(HttpServletRequest.class), any(HttpServletResponse.class));

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/%s/session/%s/api/screenshot", hubPort,
                SeleniumApiProxyServlet.class.getSimpleName(), "session_id"));
        httpClient.execute(httpGet);

        verify(mockedFunction, times(1)).apply(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }


    private interface Function {
        void apply(HttpServletRequest req, HttpServletResponse resp);
    }

    private Answer verifyRequestPath() {
        return new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpServletRequest req = (HttpServletRequest) invocationOnMock.getArguments()[0];
                assertThat(req.getPathInfo(), is("/proper/get/path/params"));
                return null;
            }
        };
    }

    private static class StubServlet extends HttpServlet {

        private final Function function;

        public StubServlet(Function function) {
            this.function = function;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            function.apply(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            function.apply(req, resp);
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            function.apply(req, resp);
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            function.apply(req, resp);
        }
    }

    private Server startServerForServlet(HttpServlet servlet, String path) throws Exception {
        Server server = new Server(0);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(servlet), path);
        server.start();

        return server;
    }

}
