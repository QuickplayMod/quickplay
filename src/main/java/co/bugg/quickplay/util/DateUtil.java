package co.bugg.quickplay.util;

public class DateUtil {
    public static int calculateDaysUntil(final long millis) {
        final long now = System.currentTimeMillis();
        return (int) ((millis - now) / (24 * 60 * 60 * 1000));
    }
}
