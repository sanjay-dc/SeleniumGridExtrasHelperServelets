package com.ai.hub.proxy.clients;

/**
 * @author Alexey Nikolaenko alexey@tcherezov.com
 *         Date: 22/09/2015
 */
public class RequestForwardingClientProvider {
    public RequestForwardingClient provide(String host, int port, String api, String queryParameters) {
        return new RequestForwardingClient(host, port, api, queryParameters);
    }
}
