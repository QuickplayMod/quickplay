package co.bugg.quickplay.http;

import co.bugg.quickplay.Reference;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestFactory {

    public HttpClient httpClient;

    public HttpRequestFactory() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setUserAgent("Minecraft Mod " + Reference.MOD_NAME + " - " + Reference.VERSION)
                .build();
    }

    /**
     * Basic web request to the AnimatedCrosshair web request API
     * Should be executed in its own thread!
     * @param endpoint Endpoint to request to
     * @param params GET parameters to request with
     * @return Response from the website
     */
    public Request newRequest(String endpoint, HashMap<String, String> params) {

        try {
            URIBuilder builder = new URIBuilder("https://bugg.co/mods/quickplay/" + endpoint);

            // Add all the parameters
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }

            // Execute the request
            HttpGet get = new HttpGet(builder.toString());

            return new Request(get, this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }
}
