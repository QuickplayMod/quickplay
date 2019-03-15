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
    URL requestEndpoint;

    /**
     * All parameters to be sent in the POST body
     */
    final HashMap<String, String> parameters = new HashMap<>();

    /**
     * Parent Google Analytics this request was created from/for
     */
    final GoogleAnalytics analytics;

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
     * Set whether this request's IP should be anonymous
     *
     * @param anonymizeIP Whether this request's IP should be anonymous
     * @return This
     * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#aip"
     */
    public AnalyticsRequest setAnonymizeIP(boolean anonymizeIP) {
        if (anonymizeIP)
            parameters.put("aip", "1");
        else
            parameters.remove("aip");
        return this;
    }

    /**
     * Set the user's screen resolution
     *
     * @param resolution Resolution of the screen, or null/empty string to remove
     * @return This
     */
    public AnalyticsRequest setScreenResolution(String resolution) {
        if (resolution == null || resolution.length() == 0)
            parameters.remove("sr");
        else
            parameters.put("sr", resolution);
        return this;
    }

    /**
     * Set the data source of this request
     * By default, the data source is "java-app"
     *
     * @param dataSource Data source, or null/empty string to remove
     * @return this
     * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#ds"
     */
    public AnalyticsRequest setDataSource(String dataSource) {
        if (dataSource == null || dataSource.length() == 0)
            parameters.remove("ds");
        else
            parameters.put("ds", dataSource);
        return this;
    }

    /**
     * Set the user's language
     *
     * @param language Language of the user, or null/empty string to remove
     * @return This
     */
    public AnalyticsRequest setLanguage(String language) {
        if (language == null || language.length() == 0)
            parameters.remove("ul");
        else
            parameters.put("ul", language);
        return this;
    }

    /**
     * Set the hostname of this page
     *
     * @param hostname Hostname, or null/empty string to remove
     * @return This
     */
    public AnalyticsRequest setHostname(String hostname) {
        if (hostname == null || hostname.length() == 0)
            parameters.remove("dh");
        else
            parameters.put("dh", hostname);
        return this;
    }

    /**
     * Set the path to the page
     *
     * @param page Path, or null/empty string to remove
     * @return This
     */
    public AnalyticsRequest setPage(String page) {
        if (page == null || page.length() == 0)
            parameters.remove("dp");
        else
            parameters.put("dp", page);
        return this;
    }

    /**
     * Set the title of the page
     *
     * @param title Title, or null/empty string to remove
     * @return This
     */
    public AnalyticsRequest setTitle(String title) {
        if (title == null || title.length() == 0)
            parameters.remove("dt");
        else
            parameters.put("dt", title);
        return this;
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
     * @return This
     */
    public AnalyticsRequest setUserAgent(String ua) {
        if (ua == null || ua.length() == 0)
            parameters.remove("ua");
        else
            parameters.put("ua", ua);
        return this;
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
