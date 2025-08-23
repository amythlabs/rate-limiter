package org.amyth.core.api;

public interface RateLimitStore {
    /**
     * Atomically add a hit for a window bucket and return current count.
     * For sliding window you’ll manage multiple buckets (e.g., per-minute).
     */
    long incrementAndGet(String bucketKey, long ttlMillis);
    long get(String bucketKey);
    void expire(String bucketKey, long ttlMillis);
}
