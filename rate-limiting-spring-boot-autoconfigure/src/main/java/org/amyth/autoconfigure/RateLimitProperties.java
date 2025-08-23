package org.amyth.autoconfigure;

import org.amyth.core.api.KeyStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("ratelimit")
public class RateLimitProperties {
    public enum Backend { CAFFEINE, REDIS }

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

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public int getSlidingBuckets() {
        return slidingBuckets;
    }

    public void setSlidingBuckets(int slidingBuckets) {
        this.slidingBuckets = slidingBuckets;
    }

    public long getDefaultPermits() {
        return defaultPermits;
    }

    public void setDefaultPermits(long defaultPermits) {
        this.defaultPermits = defaultPermits;
    }

    public Duration getDefaultWindow() {
        return defaultWindow;
    }

    public void setDefaultWindow(Duration defaultWindow) {
        this.defaultWindow = defaultWindow;
    }

    public boolean isSendHeaders() {
        return sendHeaders;
    }

    public void setSendHeaders(boolean sendHeaders) {
        this.sendHeaders = sendHeaders;
    }

    public boolean isIncludeForwardedFor() {
        return includeForwardedFor;
    }

    public void setIncludeForwardedFor(boolean includeForwardedFor) {
        this.includeForwardedFor = includeForwardedFor;
    }

    public long getCaffeineMaxSize() {
        return caffeineMaxSize;
    }

    public void setCaffeineMaxSize(long caffeineMaxSize) {
        this.caffeineMaxSize = caffeineMaxSize;
    }

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public KeyStrategy getDefaultStrategy() {
        return defaultStrategy;
    }

    public void setDefaultStrategy(KeyStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }
}
