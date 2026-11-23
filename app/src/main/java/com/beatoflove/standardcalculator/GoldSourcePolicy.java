package com.beatoflove.standardcalculator;

/** Selection by publisher timestamp; network response time is never a price timestamp. */
public final class GoldSourcePolicy {
    private GoldSourcePolicy() {}
    public static boolean validPublisher(long at, long now) {
        return at > 0L && at <= now + 10L * 60L * 1000L;
    }
    /** 0 = primary ThaiGoldToday, 1 = fallback Rakhagold, -1 = neither usable. */
    public static int choose(long primaryAt, long fallbackAt, long now) {
        boolean primary = validPublisher(primaryAt, now);
        boolean fallback = validPublisher(fallbackAt, now);
        if (!primary && !fallback) return -1;
        if (!primary) return 1;
        if (!fallback) return 0;
        return primaryAt >= fallbackAt ? 0 : 1;
    }
}
