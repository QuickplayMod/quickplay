package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;

/**
 * Factory for creating instances of {@link GoogleAnalytics}
 */
public class GoogleAnalyticsFactory {

    /**
     * Create an instance of {@link GoogleAnalytics}
     *
     * @param trackingId Tracking ID of the Google Analytics property
     * @param clientId   Unique ID of this client
     * @param debug      Whether this Google Analytics instance should be in debug mode. See {@link GoogleAnalytics#debug}
     * @return A new instance of {@link GoogleAnalytics}
     */
    public static GoogleAnalytics create(String trackingId, String clientId, boolean debug) {
        try {
            return new GoogleAnalytics()
                    .setTrackingId(trackingId)
                    .setClientId(clientId)
                    .setDebug(debug);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
