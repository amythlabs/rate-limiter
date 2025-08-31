package org.amyth.autoconfigure.webflux;

import org.amyth.autoconfigure.RateLimitProperties;
import org.amyth.autoconfigure.metrics.RateLimitMetricsBinder;
import org.amyth.core.annotation.RateLimit;
import org.amyth.core.api.KeyStrategy;
import org.amyth.core.api.RateLimiter;
import org.amyth.core.model.HitResult;
import org.amyth.core.model.LimitRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * A WebFilter that enforces @RateLimit on WebFlux controllers.
 *
 * It looks up the matched HandlerMethod from exchange attributes, resolves the annotation,
 * computes the key, calls the RateLimiter, and either continues or short-circuits with 429.
 */
@ConditionalOnClass(WebFilter.class)
public final class RateLimitWebFilter implements WebFilter {

    private final RateLimiter limiter;
    private final RateLimitProperties props;
    private final RateLimitMetricsBinder metrics;
    private final Map<String, RateLimit> registry;
    private final ExpressionParser spel = new SpelExpressionParser();

    /**
     * Creates a new RateLimitWebFilter instance.
     *
     * @param limiter The rate limiter implementation to use
     * @param props Configuration properties for rate limiting
     * @param metrics Metrics binder for collecting statistics
     * @param registry Registry mapping of paths to rate limit annotations
     */
    public RateLimitWebFilter(RateLimiter limiter, RateLimitProperties props, RateLimitMetricsBinder metrics, Map<String, RateLimit> registry) {
        this.limiter = limiter;
        this.props = props;
        this.metrics = metrics;
        this.registry = registry;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        RateLimit rl = registry.get(method + " " + path);
        if (rl == null) {
            return chain.filter(exchange); // no annotation
        }

        String key = resolveKey(exchange, rl);
        if (!StringUtils.hasText(key)) {
            return chain.filter(exchange);
        }

        LimitRule rule = new LimitRule(rl.permits(),
                Duration.of(rl.window(), rl.unit().toChronoUnit()));
        HitResult hr = limiter.hit(key, rule);

        writeHeaders(exchange, rl, hr, rule);

        if (hr.allowed()) {
            metrics.incrementAllowed();
            return chain.filter(exchange);
        } else {
            metrics.incrementBlocked();
            exchange.getResponse().setStatusCode(HttpStatus.valueOf(rl.httpStatus()));
            return exchange.getResponse().setComplete();
        }
    }

    private RateLimit findAnnotation(HandlerMethod hm) {
        RateLimit rl = hm.getMethodAnnotation(RateLimit.class);
        if (rl != null) return rl;
        return hm.getBeanType().getAnnotation(RateLimit.class);
    }

    private void writeHeaders(ServerWebExchange exchange, RateLimit rl, HitResult hr, LimitRule rule) {
        if (!rl.sendHeaders()) return;
        var headers = exchange.getResponse().getHeaders();
        headers.add("X-RateLimit-Limit", String.valueOf(rule.permits()));
        headers.add("X-RateLimit-Remaining", String.valueOf(hr.remaining()));
        headers.add("X-RateLimit-Reset", String.valueOf(hr.resetAt().getEpochSecond()));
        if (!hr.allowed()) {
            long retryAfter = Math.max(0, hr.resetAt().getEpochSecond() - Instant.now().getEpochSecond());
            headers.add("Retry-After", String.valueOf(retryAfter));
        }
    }

    private String  resolveKey(ServerWebExchange exchange, RateLimit rl) {
        // SpEL takes precedence if present
        if (StringUtils.hasText(rl.key())) {
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            ctx.setVariable("request", exchange.getRequest());
            Expression exp = spel.parseExpression(rl.key());
            String v = exp.getValue(ctx, String.class);
            if (StringUtils.hasText(v)) return v;
        }

        KeyStrategy strategy = rl.strategy();
        return switch (strategy) {
            case IP -> clientIp(exchange);
            case COOKIE -> exchange.getRequest()
                    .getCookies()
                    .getFirst(rl.strategyArg()) != null
                    ? exchange.getRequest().getCookies().getFirst(rl.strategyArg()).getValue()
                    : "nocookie";
            case HEADER -> {
                String hv = exchange.getRequest().getHeaders().getFirst(rl.strategyArg());
                yield StringUtils.hasText(hv) ? hv : "noheader";
            }
            case USER -> {
                var principalMono = exchange.getPrincipal();
                String uid = principalMono != null
                        ? principalMono.map(p -> p.getName()).blockOptional().orElse(null)
                        : null;
                yield StringUtils.hasText(uid) ? uid : "nouser";
            }
            case CUSTOM -> "custom"; // users should provide SpEL via key(), or you wire your own resolver
        };
    }

    private String clientIp(ServerWebExchange exchange) {
        var headers = exchange.getRequest().getHeaders();
        if (props.isIncludeForwardedFor()) {
            String xff = headers.getFirst("X-Forwarded-For");
            if (StringUtils.hasText(xff)) {
                // Take first IP in list
                int comma = xff.indexOf(',');
                return (comma > 0 ? xff.substring(0, comma) : xff).trim();
            }
        }
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }
}
