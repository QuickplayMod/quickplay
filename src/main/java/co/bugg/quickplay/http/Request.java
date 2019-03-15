package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.http.response.WebResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Basic web request
 */
public class Request {

    /**
     * Apache HttpComponents object for the request
     */
    public HttpRequestBase apacheRequestObj;
    /**
     * Factory this request was born in
     */
    public HttpRequestFactory factory;

    /**
     * Constructor
     *
     * @param apacheRequestObj Apache HttpComponents object
     * @param factory          Parent factory
     */
    public Request(HttpRequestBase apacheRequestObj, HttpRequestFactory factory) {
        this.apacheRequestObj = apacheRequestObj;
        this.factory = factory;
    }

    /**
     * Execute the request
     *
     * @return response from the requested page
     */
    public WebResponse execute() {

        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) factory.httpClient.execute(apacheRequestObj)) {
            int responseCode = httpResponse.getStatusLine().getStatusCode();

            // If the response code is a successful one
            if (200 <= responseCode && responseCode < 300) {
                // Get the response from the buffer & read it into a Response object
                StringWriter writer = new StringWriter();

                InputStream stream = httpResponse.getEntity().getContent();
                IOUtils.copy(stream, writer, StandardCharsets.UTF_8);

                stream.close();

                httpResponse.close();
                return WebResponse.fromJson(writer.toString());
            }

            httpResponse.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Don't send error report if the issue arose from an /exception request. Otherwise it'll probably create an infinite loop.
            if (!apacheRequestObj.getURI().toString().endsWith("/exception"))
                Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        // Probably response code is unsuccessful
        return null;
    }
}
