package co.bugg.quickplay.util;

@FunctionalInterface
public interface ServerCheckerCallback {

    void run(boolean onHypixel, String ip, ServerChecker.VerificationMethod method);
}
