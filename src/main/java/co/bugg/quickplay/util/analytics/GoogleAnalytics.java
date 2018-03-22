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
     * This instance's application name
     */
    String appName;
    /**
     * This instances application version
     */
    String appVersion;

    /**
     * Whether this instance of GoogleAnalytics should be in debug mode
     * Debug mode sends requests to the debug endpoint and outputs the result
     */
    boolean debug = false;

    /**
     * Default request that all Analytics requests extend off of
     */
    AnalyticsRequest defaultRequest;

    /**
     * Package-private Constructor
     * @see GoogleAnalyticsFactory for creating instances
     */
    GoogleAnalytics() throws MalformedURLException {
        defaultRequest = new AnalyticsRequest(null, this);
    }

    /**
     * Set the tracking ID of this Google Analytics instance
     * Must match {@link #trackingIdPattern}
     * @param id Tracking ID
     * @return This
     */
    GoogleAnalytics setTrackingId(String id) {
        if(!id.matches(trackingIdPattern))
            throw new IllegalArgumentException("Tracking ID invalid format! Regex: " + trackingIdPattern);

        this.trackingId = id;
        return this;
    }

    /**
     * Set the ID of this client
     * @param id Client ID
     * @return This
     */
    public GoogleAnalytics setClientId(String id) {
        if(id == null)
            throw new IllegalArgumentException("Invalid client ID! Client ID cannot be null.");
        this.clientId = id;
        return this;
    }

    /**
     * Set the name of this Application
     * @param name Application name
     * @return This
     */
    public GoogleAnalytics setApplicationName(String name) {
        this.appName = name;
        return this;
    }

    /**
     * Set the version of this application
     * @param version Version of this application
     * @return This
     */
    public GoogleAnalytics setApplicationVersion(String version) {
        this.appVersion = version;
        return this;
    }

    /**
     * Set whether this Google Analytics should be in debug mode
     * @see #debug
     * @param debug Whether this instance should be in debug mode
     * @return This
     */
    GoogleAnalytics setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Get {@link #appName}
     * @return {@link #appName}
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Get {@link #appVersion}
     * @return {@link #appVersion}
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * Get {@link #defaultRequest}
     * @return {@link #defaultRequest}
     */
    public AnalyticsRequest getDefaultRequest() {
        return defaultRequest;
    }

    /**
     * Create Event request
     * @param eventCategory Category of the event (required)
     * @param eventAction Action of the event (required)
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

    /**
     * Create a new pageview request
     * @return New pageview request
     */
    public PageviewRequest createPageview() {
        try {
            final PageviewRequest pageviewRequest = new PageviewRequest(this);
            pageviewRequest.parameters.putAll(defaultRequest.parameters);
            return pageviewRequest;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new screenview request
     * @param screenName Name of the screen
     * @return New screenview request
     */
    public ScreenviewRequest createScreenview(String screenName) {
        try {
            final ScreenviewRequest screenviewRequest = new ScreenviewRequest(this, screenName);
            screenviewRequest.parameters.putAll(defaultRequest.parameters);
            return screenviewRequest;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new timing request
     * @param timingCategory Category of the timing request
     * @param timingVariableName Name of the timing request
     * @param time Timing value for the request
     * @return New timing request
     */
    public TimingRequest createTiming(String timingCategory, String timingVariableName, int time) {
        try {
            final TimingRequest timingRequest = new TimingRequest(this, timingCategory, timingVariableName, time);
            timingRequest.parameters.putAll(defaultRequest.parameters);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
