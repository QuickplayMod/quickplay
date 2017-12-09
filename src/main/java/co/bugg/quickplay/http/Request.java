package co.bugg.quickplay.http;

import co.bugg.quickplay.http.response.WebResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class Request {

    public HttpRequestBase request;
    public HttpRequestFactory factory;

    public Request(HttpRequestBase request, HttpRequestFactory factory) {
        this.request = request;
        this.factory = factory;
    }

    public WebResponse execute() {

        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) factory.httpClient.execute(request)) {
            int responseCode = httpResponse.getStatusLine().getStatusCode();

            // If the response code is a successful one
            if (200 <= responseCode && responseCode < 300) {
                // Get the response from the buffer & read it into a Response object
                StringWriter writer = new StringWriter();

                InputStream stream = httpResponse.getEntity().getContent();
                IOUtils.copy(stream, writer, StandardCharsets.UTF_8);

                stream.close();

                return WebResponse.fromJson(writer.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
