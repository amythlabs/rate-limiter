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

package org.amyth.core.algo;

import org.amyth.core.api.RateLimitStore;
import org.amyth.core.api.RateLimiter;
import org.amyth.core.model.HitResult;
import org.amyth.core.model.LimitRule;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class SlidingWindowRateLimiter implements RateLimiter {
    private final RateLimitStore store;
    private final Clock clock;
    private final int buckets; // e.g., 60

    /**
     * @param store   backing store (Caffeine/Redis)
     * @param clock   time source (use Clock.systemUTC())
     * @param buckets number of sub-buckets per window (e.g., 60 for 1-min bucket size within 1-hour window)
     */
    public SlidingWindowRateLimiter(RateLimitStore store, Clock clock, int buckets) {
        this.store = Objects.requireNonNull(store, "store");
        this.clock = (clock == null) ? Clock.systemUTC() : clock;
        if (buckets <= 0) throw new IllegalArgumentException("buckets must be > 0");
        this.buckets = buckets;
    }

    @Override
    public HitResult hit(String key, LimitRule rule) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(rule, "rule");

        long windowMs = rule.window().toMillis();
        if (windowMs <= 0) throw new IllegalArgumentException("window must be > 0");

        long bucketSize = Math.max(1, windowMs / buckets); // defend against tiny windows
        long now = clock.millis();
        long currentBucket = Math.floorDiv(now, bucketSize);

        String bucketKey = "rl:%s:%d".formatted(key, currentBucket);

        long total = store.incrementAndGet(bucketKey, windowMs);

        // Sum previous (buckets - 1) buckets
        for (int i = 1; i < buckets; i++) {
            long b = currentBucket - i;
            total += store.get("rl:%s:%d".formatted(key, b));
        }

        boolean allowed = total <= rule.permits();
        long remaining = Math.max(0, rule.permits() - total);
        long resetAtMs = (currentBucket + 1) * bucketSize;

        return new HitResult(allowed, remaining, Instant.ofEpochMilli(resetAtMs));
    }
}
