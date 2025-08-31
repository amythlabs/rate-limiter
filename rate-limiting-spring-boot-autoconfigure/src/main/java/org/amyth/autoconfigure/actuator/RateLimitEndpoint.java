package org.amyth.autoconfigure.actuator;

import org.amyth.autoconfigure.metrics.RateLimitMetricsBinder;
import org.amyth.core.api.RateLimiter;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple read-only endpoint exposing rate limit meta.
 *
 * GET /actuator/ratelimit
 */
@Endpoint(id = "ratelimit")
public final class RateLimitEndpoint {

    private final RateLimiter limiter;
    private final RateLimitMetricsBinder metrics;

    /**
     * Creates a new RateLimitEndpoint instance.
     *
     * @param limiter The rate limiter instance to expose metrics for
     * @param metrics The metrics binder to collect rate limiting statistics
     */
    public RateLimitEndpoint(RateLimiter limiter, RateLimitMetricsBinder metrics) {
        this.limiter = limiter;
        this.metrics = metrics;
    }

    /**
     * Returns current rate limiting metrics and statistics.
     *
     * @return A map containing rate limit metrics including timestamp, backend type,
     *         algorithm, and request counts
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> out = new HashMap<>();
        out.put("time", Instant.now().toString());
        out.put("backend", metrics.getBackendTag());
        out.put("algo", metrics.getAlgoTag());
        out.put("allowedTotal", metrics.getAllowedTotal());
        out.put("blockedTotal", metrics.getBlockedTotal());
        return out;
    }
}
