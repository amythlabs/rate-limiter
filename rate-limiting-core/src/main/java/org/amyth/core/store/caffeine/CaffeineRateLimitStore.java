package org.amyth.core.store.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Ticker;
import org.amyth.core.api.RateLimitStore;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CaffeineRateLimitStore implements RateLimitStore {
    private final Cache<String, AtomicLong> cache;

    public CaffeineRateLimitStore(long maxSize) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .build();
    }

    public CaffeineRateLimitStore(long maxSize, Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfter(new Expiry<String, AtomicLong>() {
                    @Override
                    public long expireAfterCreate(String key, AtomicLong value, long currentTime) {
                        return TimeUnit.MILLISECONDS.toNanos(0); // default, overwritten per entry
                    }
                    @Override
                    public long expireAfterUpdate(String key, AtomicLong value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                    @Override
                    public long expireAfterRead(String key, AtomicLong value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .ticker(ticker)
                .build();
    }

    public long incrementAndGet(String bucketKey, long ttlMillis) {
        AtomicLong counter = cache.get(bucketKey, k -> new AtomicLong(0));
        cache.policy().expireVariably().ifPresent(p -> p.setExpiresAfter(bucketKey, Duration.ofMillis(ttlMillis)));
        return counter.incrementAndGet();
    }

    public long get(String bucketKey) {
        AtomicLong v = cache.getIfPresent(bucketKey);
        return v == null ? 0L : v.get();
    }

    public void expire(String bucketKey, long ttlMillis) {
        cache.policy().expireVariably().ifPresent(p -> p.setExpiresAfter(bucketKey, Duration.ofMillis(ttlMillis)));
    }

    // package-private, for tests only
    Cache<String, AtomicLong> cache() {
        return cache;
    }
}

