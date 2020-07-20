package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;

/**
 * "screenview" requests
 * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide#screenView"
 */
public class ScreenviewRequest extends AnalyticsRequest {

    /**
     * Constructor
     *
     * @param analytics Parent analytics
     * @throws MalformedURLException Internal error, malformed endpoint URL
     */
    ScreenviewRequest(GoogleAnalytics analytics, String screenName) throws MalformedURLException {
        super(RequestType.SCREENVIEW, analytics);
        if(screenName == null || screenName.length() == 0) {
            throw new IllegalArgumentException("screenName cannot be null and must be at least 1 character in length.");
        }

        parameters.put("cd", screenName);
    }
}
