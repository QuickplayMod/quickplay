package co.bugg.quickplay.util;

/**
 * Callback for when an {@link ServerChecker} verifies the client's server
 */
@FunctionalInterface
public interface ServerCheckerCallback {

    /**
     * Called when a {@link ServerChecker} verifies a server
     * @param onHypixel Whether the client is on Hypixel or not
     * @param ip The IP the client is on
     * @param method The method used to verify that the client is on Hypixel, or null if not on hypixel
     */
    void run(boolean onHypixel, String ip, ServerChecker.VerificationMethod method);
}
