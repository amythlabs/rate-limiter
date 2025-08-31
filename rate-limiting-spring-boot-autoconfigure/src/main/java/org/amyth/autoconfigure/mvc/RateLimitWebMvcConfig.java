package org.amyth.autoconfigure.mvc;

import org.amyth.autoconfigure.RateLimitProperties;
import org.amyth.autoconfigure.interceptor.RateLimitInterceptor;
import org.amyth.autoconfigure.metrics.RateLimitMetricsBinder;
import org.amyth.core.api.RateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for rate limiting.
 * Configures the rate limit interceptor for MVC controllers.
 */
@ConditionalOnClass(WebMvcConfigurer.class)
public class RateLimitWebMvcConfig implements WebMvcConfigurer {

    private final RateLimiter limiter;
    private final RateLimitProperties props;
    private final RateLimitMetricsBinder metrics;

    /**
     * Creates a new RateLimitWebMvcConfig instance.
     *
     * @param limiter The rate limiter implementation to use
     * @param props Configuration properties for rate limiting
     * @param metrics Metrics binder for collecting statistics
     */
    public RateLimitWebMvcConfig(RateLimiter limiter, RateLimitProperties props, RateLimitMetricsBinder metrics) {
        this.limiter = limiter;
        this.props = props;
        this.metrics = metrics;
    }

    /**
     * Creates the rate limit interceptor bean.
     *
     * @return A configured rate limit interceptor
     */
    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor(limiter, props, metrics);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor()).addPathPatterns("/**");
    }
}
