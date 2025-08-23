package org.amyth.autoconfigure;

import org.amyth.autoconfigure.actuator.RateLimitEndpoint;
import org.amyth.autoconfigure.metrics.RateLimitMetricsBinder;
import org.amyth.autoconfigure.mvc.RateLimitWebMvcConfig;
import org.amyth.autoconfigure.store.redis.RedisRateLimitStore;
import org.amyth.autoconfigure.webflux.RateLimitWebFilter;
import org.amyth.core.algo.SlidingWindowRateLimiter;
import org.amyth.core.annotation.RateLimit;
import org.amyth.core.api.RateLimitStore;
import org.amyth.core.api.RateLimiter;
import org.amyth.core.store.caffeine.CaffeineRateLimitStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitingAutoConfiguration {

    /* ---------- Store selection (Caffeine | Redis) ---------- */

    @Bean
    @ConditionalOnMissingBean(RateLimitStore.class)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(prefix = "ratelimit", name = "backend", havingValue = "REDIS")
    public RateLimitStore redisRateLimitStore(
            RateLimitProperties props,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider
    ) {
        if (props.getBackend() == RateLimitProperties.Backend.REDIS) {
            StringRedisTemplate tpl = redisTemplateProvider.getIfAvailable();
            if (tpl == null) {
                throw new IllegalStateException("ratelimit.backend=REDIS but StringRedisTemplate is missing. " +
                        "Add spring-boot-starter-data-redis and configure Redis connection.");
            }
            return new RedisRateLimitStore(tpl, props.getRedisKeyPrefix());
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitStore.class)
    public RateLimitStore caffeineRateLimitStore(RateLimitProperties props) {
        return new CaffeineRateLimitStore(props.getCaffeineMaxSize());
    }

    /* ---------- Limiter (Sliding Window) ---------- */

    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter rateLimiter(RateLimitStore store, RateLimitProperties props) {
        return new SlidingWindowRateLimiter(store, Clock.systemUTC(), props.getSlidingBuckets());
    }

    /* ---------- Metrics binder (uses tags, not RateLimiter) ---------- */

    @Bean
    @ConditionalOnMissingBean(RateLimitMetricsBinder.class)
    public RateLimitMetricsBinder rateLimitMetricsBinder(RateLimitProperties props) {
        String backendTag = props.getBackend().name().toLowerCase();     // "caffeine" | "redis"
        String algoTag = "sliding_window";                                // constant for now
        return new RateLimitMetricsBinder(backendTag, algoTag);
    }

    /* ---------- MVC wiring ---------- */

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    static class ServletMvcConfiguration {
        @Bean
        public RateLimitWebMvcConfig rateLimitWebMvcConfig(
                RateLimiter limiter,
                RateLimitProperties props,
                RateLimitMetricsBinder metrics
        ) {
            return new RateLimitWebMvcConfig(limiter, props, metrics);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass(name = {
            "org.springframework.web.server.WebFilter",
            "reactor.core.publisher.Mono",
            "org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping"
    })
    static class ReactiveWebFluxConfiguration {
        @Bean
        public RateLimitWebFilter rateLimitWebFilter(
                RateLimiter limiter,
                RateLimitProperties props,
                RateLimitMetricsBinder metrics,
                @Qualifier("requestMappingHandlerMapping")
                RequestMappingHandlerMapping mapping
        ) {
            // Build registry of (METHOD path -> RateLimit) once at startup
            Map<String, RateLimit> registry = new HashMap<>();
            mapping.getHandlerMethods().forEach((info, handler) -> {
                RateLimit ann = handler.getMethodAnnotation(RateLimit.class);
                if (ann == null) {
                    ann = handler.getBeanType().getAnnotation(RateLimit.class);
                }
                if (ann != null) {
                    RateLimit finalAnn = ann;
                    info.getPatternsCondition().getPatterns().forEach(p -> {
                        info.getMethodsCondition().getMethods().forEach(m -> {
                            registry.put(m.name() + " " + p.getPatternString(), finalAnn);
                        });
                    });
                }
            });
            return new RateLimitWebFilter(limiter, props, metrics, registry);
        }
    }

    /* ---------- Actuator endpoint ---------- */

    @Bean
    @ConditionalOnAvailableEndpoint(endpoint = RateLimitEndpoint.class)
    public RateLimitEndpoint rateLimitEndpoint(
            RateLimiter limiter,
            RateLimitMetricsBinder metrics
    ) {
        return new RateLimitEndpoint(limiter, metrics);
    }
}
