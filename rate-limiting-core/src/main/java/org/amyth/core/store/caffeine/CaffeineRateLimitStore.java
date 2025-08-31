/*
 * Copyright 2025 amythlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.amyth.core.store.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Ticker;
import org.amyth.core.api.RateLimitStore;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RateLimitStore implementation backed by Caffeine in-memory cache.
 * <p>
 * Provides atomic increment and expiration for rate limit buckets.
 * Suitable for single-node deployments or ephemeral rate limiting.
 * </p>
 */
public class CaffeineRateLimitStore implements RateLimitStore {
    private final Cache<String, AtomicLong> cache;

    /**
     * Constructs a CaffeineRateLimitStore with the specified maximum cache size.
     *
     * @param maxSize the maximum number of entries the cache can hold
     */
    public CaffeineRateLimitStore(long maxSize) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .build();
    }

    /**
     * Constructs a CaffeineRateLimitStore with custom cache size and ticker.
     * Allows for custom expiration policies via the provided ticker.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @param ticker  the ticker to use for cache expiration
     */
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
