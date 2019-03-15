package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;

/**
 * Base Google Analytics tracking class
 */
public class GoogleAnalytics {

    /**
     * Pattern {@link #trackingId} must match
     */
    private static final String trackingIdPattern = "(?:UA)-\\d{8}-\\d";

    /**
     * This instance's Google Analytics tracking ID
     */
    String trackingId;
    /**
     * The Client ID this Analytics instance is reporting for
     */
    String clientId;

    /**
     * Whether this instance of GoogleAnalytics should be in debug mode
     * Debug mode sends requests to the debug endpoint and outputs the result
     */
    boolean debug = false;

    /**
     * Default request that all Analytics requests extend off of
     */
    private AnalyticsRequest defaultRequest;

    /**
     * Package-private Constructor
     *
     * @see GoogleAnalyticsFactory for creating instances
     */
    GoogleAnalytics() throws MalformedURLException {
        defaultRequest = new AnalyticsRequest(null, this);
    }

    /**
     * Set the tracking ID of this Google Analytics instance
     * Must match {@link #trackingIdPattern}
     *
     * @param id Tracking ID
     * @return This
     */
    GoogleAnalytics setTrackingId(String id) {
        if (!id.matches(trackingIdPattern))
            throw new IllegalArgumentException("Tracking ID invalid format! Regex: " + trackingIdPattern);

        this.trackingId = id;
        return this;
    }

    /**
     * Set the ID of this client
     *
     * @param id Client ID
     * @return This
     */
    GoogleAnalytics setClientId(String id) {
        if (id == null)
            throw new IllegalArgumentException("Invalid client ID! Client ID cannot be null.");
        this.clientId = id;
        return this;
    }

    /**
     * Set whether this Google Analytics should be in debug mode
     *
     * @param debug Whether this instance should be in debug mode
     * @return This
     * @see #debug
     */
    GoogleAnalytics setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Get {@link #defaultRequest}
     *
     * @return {@link #defaultRequest}
     */
    public AnalyticsRequest getDefaultRequest() {
        return defaultRequest;
    }

    /**
     * Create Event request
     *
     * @param eventCategory Category of the event (required)
     * @param eventAction   Action of the event (required)
     * @return A new event request
     */
    public EventRequest createEvent(String eventCategory, String eventAction) {
        try {
            final EventRequest eventRequest = new EventRequest(this, eventCategory, eventAction);
            eventRequest.parameters.putAll(defaultRequest.parameters);
            return eventRequest;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new exception request
     *
     * @return New exception request
     */
    public ExceptionRequest createException() {
        try {
            final ExceptionRequest exceptionRequest = new ExceptionRequest(this);
            exceptionRequest.parameters.putAll(defaultRequest.parameters);
            return exceptionRequest;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
