package com.beatoflove.standardcalculator;

/** Conservative cache replacement policy for timestamped upstream prices. */
public final class CacheUpdatePolicy {
    private CacheUpdatePolicy() {}
    /** No stored source timestamp: accept validated prices. Otherwise never regress. */
    public static boolean shouldAccept(long savedPublisherMillis, long newPublisherMillis) {
        if (savedPublisherMillis <= 0) return true;
        return newPublisherMillis > 0 && newPublisherMillis >= savedPublisherMillis;
    }
    /** Offline or failed refresh must always identify cached values as historical. */
    public static boolean isCachedDisplay(boolean receivedThisSession, boolean refreshFailed) {
        return !receivedThisSession || refreshFailed;
    }
}
