package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;

/**
 * "pageview" requests
 *
 * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide#page"
 */
public class PageviewRequest extends AnalyticsRequest {

    /**
     * Constructor
     *
     * @param analytics Parent analytics
     * @throws MalformedURLException Internal error, malformed endpoint URL
     */
    PageviewRequest(GoogleAnalytics analytics) throws MalformedURLException {
        super(RequestType.PAGEVIEW, analytics);
    }
}
