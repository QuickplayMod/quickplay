package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;

/**
 * "event" requests
 * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide#event"
 */
public class EventRequest extends AnalyticsRequest {

    /**
     * Constructor
     * @param analytics Parent analytics instance
     * @param eventCategory Category of this event. Required
     * @param eventAction Action of this event. Required
     * @throws MalformedURLException URL to send the data to is malformed
     */
    EventRequest(GoogleAnalytics analytics, String eventCategory, String eventAction) throws MalformedURLException {
        super(RequestType.EVENT, analytics);

        if(eventAction == null || eventAction.length() == 0 || eventCategory == null || eventCategory.length() == 0)
            throw new IllegalArgumentException("eventCategory and eventAction cannot be null and must be at least 1 character in length.");

        parameters.put("ec", eventCategory);
        parameters.put("ea", eventAction);
    }

    /**
     * Set the event label
     * Not required
     * Pass "null" or empty string to remove the label
     * @param label New label, or null/empty string to remove it
     * @return This
     */
    public EventRequest setEventLabel(String label) {
        if(label == null || label.length() == 0)
            parameters.remove("el");
        else
            parameters.put("el", label);

        return this;
    }

    /**
     * Set the event value
     * Not required
     * @param value New value
     * @return This
     */
    public EventRequest setEventValue(int value) {
        parameters.put("ev", String.valueOf(value));
        return this;
    }
}
