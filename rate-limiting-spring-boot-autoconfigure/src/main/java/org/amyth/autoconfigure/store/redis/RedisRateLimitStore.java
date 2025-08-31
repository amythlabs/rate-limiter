package org.amyth.autoconfigure.store.redis;

import org.amyth.core.api.RateLimitStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Objects;

/**
 * Redis-backed RateLimitStore.
 *
 * Uses a small Lua script to INCR and set PEXPIRE only on first creation,
 * so we don't keep resetting TTL on subsequent increments.
 */
public final class RedisRateLimitStore implements RateLimitStore {

    private static final String INCR_WITH_TTL_LUA =
            "local v = redis.call('INCR', KEYS[1]); " +
                    "if v == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]); end; " +
                    "return v;";

    private final StringRedisTemplate redis;
    private final String keyPrefix;

    /**
     * Creates a new RedisRateLimitStore instance.
     *
     * @param redis The Redis template to use for storage operations
     * @param keyPrefix Prefix to apply to all Redis keys (defaults to "rl:" if null or blank)
     */
    public RedisRateLimitStore(@NonNull StringRedisTemplate redis, String keyPrefix) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.keyPrefix = (keyPrefix == null || keyPrefix.isBlank()) ? "rl:" : keyPrefix;
    }

    @Override
    public long incrementAndGet(String bucketKey, long ttlMillis) {
        Long v = redis.execute((connection) ->
                connection.scriptingCommands().eval(
                        INCR_WITH_TTL_LUA.getBytes(),
                        org.springframework.data.redis.connection.ReturnType.INTEGER,
                        1,
                        full(bucketKey).getBytes(),
                        String.valueOf(ttlMillis).getBytes()
                ), true, true
        );
        return v == null ? 0L : v;
    }

    @Override
    public long get(String bucketKey) {
        String v = redis.opsForValue().get(full(bucketKey));
        if (v == null) return 0L;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return 0L; }
    }

    @Override
    public void expire(String bucketKey, long ttlMillis) {
        redis.expire(full(bucketKey), Duration.ofMillis(ttlMillis));
    }

    private String full(String bucketKey) {
        return keyPrefix + bucketKey;
    }
}
