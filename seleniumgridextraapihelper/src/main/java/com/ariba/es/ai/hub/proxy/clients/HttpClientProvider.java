package com.ariba.es.ai.hub.proxy.clients;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Alexey Nikolaenko alexey@tcherezov.com
 *         Date: 21/09/2015
 */
public class HttpClientProvider {

    public CloseableHttpClient provide() {
        return HttpClients.createDefault();
    }
}
