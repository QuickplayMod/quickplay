package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;

/**
 * "timing" requests
 *
 * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide#usertiming"
 */
public class TimingRequest extends AnalyticsRequest {

    /**
     * Constructor
     *
     * @param analytics          Parent analytics
     * @param timingCategory     The category this timing belongs to
     * @param timingVariableName The name of this timing value
     * @param time               The timing for this request in milliseconds
     * @throws MalformedURLException Internal error, malformed endpoint URL
     */
    TimingRequest(GoogleAnalytics analytics, String timingCategory, String timingVariableName, int time) throws MalformedURLException {
        super(RequestType.TIMING, analytics);

        if (timingCategory == null || timingCategory.length() == 0)
            throw new IllegalArgumentException("timingCategory cannot be null and must be at least 1 character in length.");
        if (timingVariableName == null || timingVariableName.length() == 0)
            throw new IllegalArgumentException("timingVariableName cannot be null and must be at least 1 character in length.");
        if (time < 0)
            throw new IllegalArgumentException("time must be greater than or equal to 0.");

        parameters.put("utc", timingCategory);
        parameters.put("utv", timingVariableName);
        parameters.put("utt", String.valueOf(time));
    }

    /**
     * Set the timing label for this request
     *
     * @param label New label, or null/empty string to remove
     * @return This
     */
    public TimingRequest setTimingLabel(String label) {
        if (label == null || label.length() == 0)
            parameters.remove("utl");
        else
            parameters.put("utl", label);
        return this;
    }

    /**
     * Set the time taken to load the page in milliseconds
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setPageLoadTime(int time) {
        parameters.put("plt", String.valueOf(time));
        return this;
    }

    /**
     * Set the time it took to do a DNS lookup in milliseconds
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setDNSTime(int time) {
        parameters.put("dns", String.valueOf(time));
        return this;
    }

    /**
     * Set the time it took to download this page
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setDownloadTime(int time) {
        parameters.put("pdt", String.valueOf(time));
        return this;
    }

    /**
     * Set the time it took for any redirects
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setResponseTime(int time) {
        parameters.put("rrt", String.valueOf(time));
        return this;
    }

    /**
     * Set the amount of time it took to create a TCP connection
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setTCPConnectTime(int time) {
        parameters.put("tcp", String.valueOf(time));
        return this;
    }

    /**
     * Set the amount of time it took for the server to respond
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setServerResponseTime(int time) {
        parameters.put("srt", String.valueOf(time));
        return this;
    }

    /**
     * Set the amount of time it took for <code>Document.readyState</code> to equal <code>interactive</code> in browser context
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setDOMInteractiveTime(int time) {
        parameters.put("dit", String.valueOf(time));
        return this;
    }

    /**
     * Set the amount of time it took for the content to load (<code>DOMContentLoaded</code> event in browser context)
     *
     * @param time Time in milliseconds
     * @return This
     */
    public TimingRequest setContentLoadTime(int time) {
        parameters.put("clt", String.valueOf(time));
        return this;
    }
}
