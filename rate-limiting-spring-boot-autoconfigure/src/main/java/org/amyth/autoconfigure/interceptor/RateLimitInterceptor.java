package org.amyth.autoconfigure.interceptor;

import org.amyth.autoconfigure.RateLimitProperties;
import org.amyth.autoconfigure.metrics.RateLimitMetricsBinder;
import org.amyth.core.annotation.RateLimit;
import org.amyth.core.api.KeyStrategy;
import org.amyth.core.api.RateLimiter;
import org.amyth.core.model.HitResult;
import org.amyth.core.model.LimitRule;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;

/**
 * Spring MVC interceptor that enforces @RateLimit on controller methods/classes.
 */
public final class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter limiter;
    private final RateLimitProperties props;
    private final RateLimitMetricsBinder metrics;
    private final ExpressionParser spel = new SpelExpressionParser();

    public RateLimitInterceptor(RateLimiter limiter, RateLimitProperties props, RateLimitMetricsBinder metrics) {
        this.limiter = limiter;
        this.props = props;
        this.metrics = metrics;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        System.out.println("RateLimitInterceptor: preHandle called");
        if (!(handler instanceof HandlerMethod hm)) {
            return true; // not a controller method
        }

        RateLimit rl = resolveAnnotation(hm);
        if (rl == null) {
            return true; // no annotation -> skip
        }

        String key = resolveKey(request, rl);
        if (!StringUtils.hasText(key)) {
            // fail-open if key can't be resolved (you could make this configurable)
            return true;
        }

        LimitRule rule = new LimitRule(rl.permits(), Duration.of(rl.window(), rl.unit().toChronoUnit()));
        HitResult hr = limiter.hit(key, rule);

        writeHeaders(response, rl, hr, rule);

        if (hr.allowed()) {
            metrics.incrementAllowed();
            return true; // continue to controller
        } else {
            metrics.incrementBlocked();
            response.setStatus(rl.httpStatus());
            // body is optional; headers provide the details
            return false; // short-circuit with 429
        }
    }

    /* ------------------ helpers ------------------ */

    private RateLimit resolveAnnotation(HandlerMethod hm) {
        RateLimit rl = hm.getMethodAnnotation(RateLimit.class);
        if (rl != null) return rl;
        return hm.getBeanType().getAnnotation(RateLimit.class);
    }

    private String resolveKey(HttpServletRequest request, RateLimit rl) {
        // 1) SpEL (highest precedence)
        if (StringUtils.hasText(rl.key())) {
            try {
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                ctx.setVariable("request", request);
                Expression exp = spel.parseExpression(rl.key());
                String v = exp.getValue(ctx, String.class);
                if (StringUtils.hasText(v)) {
                    return v;
                }
            } catch (Exception ignored) {
                // fall through to strategy
            }
        }

        // 2) Strategy-based
        KeyStrategy strategy = rl.strategy();
        String arg = rl.strategyArg();

        return switch (strategy) {
            case IP -> clientIp(request);
            case COOKIE -> {
                if (!StringUtils.hasText(arg)) yield "nocookie";
                var cookies = request.getCookies();
                if (cookies != null) {
                    for (var c : cookies) {
                        if (arg.equals(c.getName())) {
                            yield c.getValue();
                        }
                    }
                }
                yield "nocookie";
            }
            case HEADER -> {
                if (!StringUtils.hasText(arg)) yield "noheader";
                String hv = request.getHeader(arg);
                yield StringUtils.hasText(hv) ? hv : "noheader";
            }
            case USER -> {
                Principal p = request.getUserPrincipal();
                yield (p != null && StringUtils.hasText(p.getName())) ? p.getName() : "nouser";
            }
            case CUSTOM -> "custom"; // encourage users to use SpEL via key() for custom
        };
    }

    private String clientIp(HttpServletRequest request) {
        if (props.isIncludeForwardedFor()) {
            String xff = headerFirst(request, "X-Forwarded-For");
            if (StringUtils.hasText(xff)) {
                int comma = xff.indexOf(',');
                return (comma > 0 ? xff.substring(0, comma) : xff).trim();
            }
            String xri = headerFirst(request, "X-Real-IP");
            if (StringUtils.hasText(xri)) return xri.trim();
        }
        String remote = request.getRemoteAddr();
        return (remote != null) ? remote : "unknown";
    }

    private String headerFirst(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        return v != null ? v : "";
    }

    private void writeHeaders(HttpServletResponse response, RateLimit rl, HitResult hr, LimitRule rule) {
        if (!rl.sendHeaders()) return;
        response.setHeader("X-RateLimit-Limit", String.valueOf(rule.permits()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(hr.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(hr.resetAt().getEpochSecond()));
        if (!hr.allowed()) {
            long retryAfter = Math.max(0, hr.resetAt().getEpochSecond() - Instant.now().getEpochSecond());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
        }
    }
}