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

    public RateLimitEndpoint(RateLimiter limiter, RateLimitMetricsBinder metrics) {
        this.limiter = limiter;
        this.metrics = metrics;
    }

    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> out = new HashMap<>();
        out.put("time", Instant.now().toString());
        out.put("backend", metrics.getBackendTag());
        out.put("algo", metrics.getAlgoTag());
        out.put("allowedTotal", metrics.getAllowedTotal());
        out.put("blockedTotal", metrics.getBlockedTotal());
        // You can add more fields (e.g., configured defaults) if you expose them via properties.
        return out;
    }
}
