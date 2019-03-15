package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;
import java.util.UUID;

/**
 * Factory for creating instances of {@link GoogleAnalytics}
 */
public class GoogleAnalyticsFactory {

    /**
     * Create an instance of {@link GoogleAnalytics} with a randomized {@link GoogleAnalytics#clientId} and
     * {@link GoogleAnalytics#debug} set to false
     */
    public static GoogleAnalytics create(String trackingId, String appName, String appVersion) {
        return create(trackingId, appName, appVersion, false);
    }

    /**
     * Create an instance of {@link GoogleAnalytics} with {@link GoogleAnalytics#debug} set to false
     */
    public static GoogleAnalytics create(String trackingId, String clientId, String appName, String appVersion) {
        return create(trackingId, clientId, appName, appVersion, false);
    }

    /**
     * Create an instance of {@link GoogleAnalytics} with a randomized {@link GoogleAnalytics#clientId}
     */
    public static GoogleAnalytics create(String trackingId, String appName, String appVersion, boolean debug) {
        return create(trackingId, UUID.randomUUID().toString(), appName, appVersion, debug);
    }

    /**
     * Create an instance of {@link GoogleAnalytics}
     *
     * @param trackingId Tracking ID of the Google Analytics property
     * @param appName    Name of this Google Analytics application
     * @param appVersion Version of this Google Analytics application
     * @param clientId   Unique ID of this client
     * @param debug      Whether this Google Analytics instance should be in debug mode. See {@link GoogleAnalytics#debug}
     * @return A new instance of {@link GoogleAnalytics}
     */
    public static GoogleAnalytics create(String trackingId, String clientId, String appName, String appVersion, boolean debug) {
        try {
            return new GoogleAnalytics()
                    .setTrackingId(trackingId)
                    .setClientId(clientId)
                    .setDebug(debug)
                    .setApplicationName(appName)
                    .setApplicationVersion(appVersion);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
