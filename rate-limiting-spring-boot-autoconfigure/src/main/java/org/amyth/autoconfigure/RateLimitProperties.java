package org.amyth.autoconfigure;

import org.amyth.core.api.KeyStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for rate limiting functionality.
 * Prefix: "ratelimit"
 */
@ConfigurationProperties("ratelimit")
public class RateLimitProperties {

    /**
     * Available backend storage types for rate limiting.
     */
    public enum Backend {
        /**
         * In-memory cache implementation using Caffeine
         */
        CAFFEINE,

        /**
         * Distributed cache implementation using Redis
         */
        REDIS
    }

    /**
     * Creates a new instance of RateLimitProperties.
     * Default constructor used by Spring Boot for properties binding.
     */
    public RateLimitProperties() {
    }

    private Backend backend = Backend.CAFFEINE;
    private int slidingBuckets = 60;
    private long defaultPermits = 60;
    private Duration defaultWindow = Duration.ofMinutes(1);
    private boolean sendHeaders = true;
    private boolean includeForwardedFor = true;

    // caffeine
    private long caffeineMaxSize = 200_000;

    // redis
    private String redisKeyPrefix = "rl:";

    // key fallback
    private KeyStrategy defaultStrategy = KeyStrategy.IP;

    /**
     * Gets the configured backend storage type.
     * @return The backend storage type (CAFFEINE or REDIS)
     */
    public Backend getBackend() {
        return backend;
    }

    /**
     * Sets the backend storage type.
     * @param backend The backend storage type to use
     */
    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    /**
     * Gets the number of sliding window buckets.
     * @return Number of buckets used in the sliding window
     */
    public int getSlidingBuckets() {
        return slidingBuckets;
    }

    /**
     * Sets the number of sliding window buckets.
     * @param slidingBuckets Number of buckets to use
     */
    public void setSlidingBuckets(int slidingBuckets) {
        this.slidingBuckets = slidingBuckets;
    }

    /**
     * Gets the default number of permits allowed per window.
     * @return Default number of permits
     */
    public long getDefaultPermits() {
        return defaultPermits;
    }

    /**
     * Sets the default number of permits allowed per window.
     * @param defaultPermits Number of permits to allow
     */
    public void setDefaultPermits(long defaultPermits) {
        this.defaultPermits = defaultPermits;
    }

    /**
     * Gets the default time window duration.
     * @return Default time window
     */
    public Duration getDefaultWindow() {
        return defaultWindow;
    }

    /**
     * Sets the default time window duration.
     * @param defaultWindow Time window to use
     */
    public void setDefaultWindow(Duration defaultWindow) {
        this.defaultWindow = defaultWindow;
    }

    /**
     * Checks if rate limit headers should be sent in responses.
     * @return true if headers should be sent, false otherwise
     */
    public boolean isSendHeaders() {
        return sendHeaders;
    }

    /**
     * Sets whether rate limit headers should be sent in responses.
     * @param sendHeaders true to send headers, false otherwise
     */
    public void setSendHeaders(boolean sendHeaders) {
        this.sendHeaders = sendHeaders;
    }

    /**
     * Checks if X-Forwarded-For header should be included in rate limit key.
     * @return true if X-Forwarded-For should be included, false otherwise
     */
    public boolean isIncludeForwardedFor() {
        return includeForwardedFor;
    }

    /**
     * Sets whether X-Forwarded-For header should be included in rate limit key.
     * @param includeForwardedFor true to include header, false otherwise
     */
    public void setIncludeForwardedFor(boolean includeForwardedFor) {
        this.includeForwardedFor = includeForwardedFor;
    }

    /**
     * Gets the maximum size of the Caffeine cache.
     * @return Maximum number of entries in Caffeine cache
     */
    public long getCaffeineMaxSize() {
        return caffeineMaxSize;
    }

    /**
     * Sets the maximum size of the Caffeine cache.
     * @param caffeineMaxSize Maximum number of entries to store
     */
    public void setCaffeineMaxSize(long caffeineMaxSize) {
        this.caffeineMaxSize = caffeineMaxSize;
    }

    /**
     * Gets the Redis key prefix for rate limit entries.
     * @return Prefix used for Redis keys
     */
    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    /**
     * Sets the Redis key prefix for rate limit entries.
     * @param redisKeyPrefix Prefix to use for Redis keys
     */
    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    /**
     * Gets the default key strategy for rate limiting.
     * @return Default strategy for generating rate limit keys
     */
    public KeyStrategy getDefaultStrategy() {
        return defaultStrategy;
    }

    /**
     * Sets the default key strategy for rate limiting.
     * @param defaultStrategy Strategy to use for generating rate limit keys
     */
    public void setDefaultStrategy(KeyStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }
}
