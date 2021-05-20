package co.bugg.quickplay.util;

import java.util.Date;

public class DateUtil {
    public static int calculateDaysUntil(final Date date) {
        final long now = System.currentTimeMillis();
        return (int) ((date.getTime() - now) / (24 * 60 * 60 * 1000));
    }
}
