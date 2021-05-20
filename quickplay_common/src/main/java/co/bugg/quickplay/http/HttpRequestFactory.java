package co.bugg.quickplay.http;

import co.bugg.quickplay.Reference;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Factory for Quickplay HTTP requests
 */
public class HttpRequestFactory {

    public HttpClient httpClient;

    public HttpRequestFactory() {
        httpClient = newClient();
    }

    public HttpClient newClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setUserAgent("Minecraft Mod " + Reference.MOD_NAME + " - " + Reference.VERSION)
                .build();
    }
}
