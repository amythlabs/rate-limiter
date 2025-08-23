package org.amyth.autoconfigure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Micrometer binder + simple counters you can call from Interceptor/WebFilter.
 *
 * Exposes:
 *  - ratelimit.allowed
 *  - ratelimit.blocked
 *
 * Also keeps local AtomicLongs so an Actuator endpoint can expose quick totals
 * without having to query the registry.
 */
public final class RateLimitMetricsBinder implements MeterBinder {

    private final String backendTag;
    private final String algoTag;

    private volatile Counter allowedCounter;
    private volatile Counter blockedCounter;

    private final AtomicLong allowedTotal = new AtomicLong();
    private final AtomicLong blockedTotal = new AtomicLong();

    public RateLimitMetricsBinder(String backendTag, String algoTag) {
        this.backendTag = backendTag == null ? "unknown" : backendTag;
        this.algoTag = algoTag == null ? "sliding_window" : algoTag;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        this.allowedCounter = Counter.builder("ratelimit.allowed")
                .tag("backend", backendTag)
                .tag("algo", algoTag)
                .description("Count of allowed requests per rate limiting rules")
                .register(registry);

        this.blockedCounter = Counter.builder("ratelimit.blocked")
                .tag("backend", backendTag)
                .tag("algo", algoTag)
                .description("Count of blocked (429) requests due to rate limiting")
                .register(registry);
    }

    public void incrementAllowed() {
        allowedTotal.incrementAndGet();
        if (allowedCounter != null) allowedCounter.increment();
    }

    public void incrementBlocked() {
        blockedTotal.incrementAndGet();
        if (blockedCounter != null) blockedCounter.increment();
    }

    public long getAllowedTotal() { return allowedTotal.get(); }
    public long getBlockedTotal() { return blockedTotal.get(); }
    public String getBackendTag() { return backendTag; }
    public String getAlgoTag() { return algoTag; }
}
