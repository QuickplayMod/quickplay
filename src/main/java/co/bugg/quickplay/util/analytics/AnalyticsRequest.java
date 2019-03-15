package co.bugg.quickplay.util.analytics;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Basic request for Google Analytics
 */
public class AnalyticsRequest {

    /**
     * URL to the endpoint this request is being sent to
     */
    private URL requestEndpoint;

    /**
     * All parameters to be sent in the POST body
     */
    final HashMap<String, String> parameters = new HashMap<>();

    /**
     * Parent Google Analytics this request was created from/for
     */
    private final GoogleAnalytics analytics;

    /**
     * Constructor
     *
     * @param requestType the type of request this is. No default parameters are added if this is null
     * @param analytics   The parent analytics
     * @throws MalformedURLException Internal error; the URL set is invalid
     */
    AnalyticsRequest(RequestType requestType, GoogleAnalytics analytics) throws MalformedURLException {
        this.analytics = analytics;

        if (analytics.debug) {
            requestEndpoint = new URL("https://www.google-analytics.com/debug/collect");
        } else {
            requestEndpoint = new URL("https://www.google-analytics.com/collect");
        }

        // Only add basic parameters if requestType is provided, otherwise assume that will be done later
        if (requestType != null) {
            // Protocol version
            parameters.put("v", "1");
            // Tracking ID
            parameters.put("tid", analytics.trackingId);
            // Client ID
            parameters.put("cid", analytics.clientId);
            // Request Type
            parameters.put("t", requestType.getName());
        }
    }

    /**
     * Send this request to Google
     * All requests are POST with content-type form-urlencoded
     *
     * @throws IOException Error sending data, or malformed data
     * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide"
     */
    public void send() throws IOException {

        final HttpURLConnection connection = (HttpURLConnection) requestEndpoint.openConnection();

        connection.setRequestMethod("POST");

        // Build the post body
        final StringBuilder urlEncodedBodyBuilder = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> iter = parameters.entrySet().iterator(); iter.hasNext(); ) {
            final Map.Entry<String, String> map = iter.next();
            urlEncodedBodyBuilder
                    .append(map.getKey())
                    .append("=")
                    .append(URLEncoder.encode(map.getValue(), "UTF-8"));

            if (iter.hasNext())
                urlEncodedBodyBuilder.append("&");
        }
        final String urlEncodedBody = urlEncodedBodyBuilder.toString();

        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(urlEncodedBody.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        final DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(urlEncodedBody);
        out.flush();
        out.close();

        if (analytics.debug) {
            BufferedReader reader;
            if (connection.getResponseCode() == 200)
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            else
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            System.out.println(builder.toString());
        }

        if (connection.getResponseCode() != 200)
            throw new IOException("Response code not equal to 200! Illegal request. Response: " + connection.getResponseCode());

    }

    /**
     * Set the user's screen resolution
     *
     * @param resolution Resolution of the screen, or null/empty string to remove
     */
    public void setScreenResolution(String resolution) {
        if (resolution == null || resolution.length() == 0)
            parameters.remove("sr");
        else
            parameters.put("sr", resolution);
    }

    /**
     * Set the user's language
     *
     * @param language Language of the user, or null/empty string to remove
     */
    public void setLanguage(String language) {
        if (language == null || language.length() == 0)
            parameters.remove("ul");
        else
            parameters.put("ul", language);
    }

    /**
     * Set the session control
     * START = force new session
     * END = force close session
     *
     * @param sessionControl Session control value
     * @return This
     * @see SessionControl
     */
    public AnalyticsRequest setSessionControl(SessionControl sessionControl) {
        if (sessionControl == null)
            parameters.remove("sc");
        else
            parameters.put("sc", sessionControl.toString());
        return this;
    }

    /**
     * Set the user agent override
     *
     * @param ua New user agent, or null/empty string to remove
     */
    public void setUserAgent(String ua) {
        if (ua == null || ua.length() == 0)
            parameters.remove("ua");
        else
            parameters.put("ua", ua);
    }

    /**
     * All the supported types of Analytics requests
     */
    public enum RequestType {
        EVENT("event"),
        EXCEPTION("exception"),
        PAGEVIEW("pageview"),
        SCREENVIEW("screenview"),
        TIMING("timing");

        private String name;

        RequestType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Valid values for Session control
     */
    public enum SessionControl {
        // Start a new session
        START("start"),
        // Force end the current session
        END("end");

        private String text;

        SessionControl(String text) {
            this.text = text;
        }

        public String toString() {
            return text;
        }
    }
}
