package org.amyth.core.algo;

import org.amyth.core.api.RateLimitStore;
import org.amyth.core.model.LimitRule;
import org.amyth.core.store.caffeine.CaffeineRateLimitStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowRateLimiterTest {

    @Test
    void allows_then_blocks_within_window() {
        RateLimitStore store = new CaffeineRateLimitStore(10_000);
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(store, Clock.systemUTC(), 6);

        LimitRule rule = new LimitRule(3, Duration.ofMinutes(1));
        assertThat(limiter.hit("k", rule).allowed()).isTrue();
        assertThat(limiter.hit("k", rule).allowed()).isTrue();
        assertThat(limiter.hit("k", rule).allowed()).isTrue();
        assertThat(limiter.hit("k", rule).allowed()).isFalse();
    }
}
