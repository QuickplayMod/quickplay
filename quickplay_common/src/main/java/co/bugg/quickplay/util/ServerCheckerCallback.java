package co.bugg.quickplay.util;

/**
 * Callback for when an {@link ServerChecker} verifies the client's server
 */
@FunctionalInterface
public interface ServerCheckerCallback {

    /**
     * Called when a {@link ServerChecker} verifies a server
     * @param ip The IP the client is on
     */
    void run(String ip);
}
