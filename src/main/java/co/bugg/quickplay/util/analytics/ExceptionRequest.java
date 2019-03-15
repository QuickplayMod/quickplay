package co.bugg.quickplay.util.analytics;

import java.net.MalformedURLException;

/**
 * "exception" requests
 *
 * @see "https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide#exception"
 */
public class ExceptionRequest extends AnalyticsRequest {

    /**
     * Constructor
     *
     * @param analytics Parent analytics
     * @throws MalformedURLException Internal error, malformed endpoint URL
     */
    ExceptionRequest(GoogleAnalytics analytics) throws MalformedURLException {
        super(RequestType.EXCEPTION, analytics);
    }

    /**
     * Set the description of this exception, typically probably {@link Exception#getMessage()}
     *
     * @param description Description of the exception, or null/empty string to remove
     * @return This
     */
    public ExceptionRequest setExceptionDescription(String description) {
        if (description == null || description.length() == 0)
            parameters.remove("exd");
        else
            parameters.put("exd", description);
        return this;
    }

    /**
     * Set whether this exception is fatal or not
     *
     * @param fatal Whether this exception is fatal or not
     * @return This
     */
    public ExceptionRequest setIsFatal(boolean fatal) {
        if (fatal)
            parameters.put("exf", "1");
        else
            parameters.remove("exf");
        return this;
    }
}
