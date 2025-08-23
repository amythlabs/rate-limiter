package org.amyth.core.store.caffeine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaffeineRateLimitStoreTest {

    @Test
    @DisplayName("get() returns 0 for a missing key")
    void returnsZeroForMissingKey() {
        CaffeineRateLimitStore store = new CaffeineRateLimitStore(1_000);
        assertEquals(0L, store.get("nope"));
    }

    @Test
    @DisplayName("incrementAndGet() creates entry and increments atomically")
    void incrementCreatesAndIncrements() {
        CaffeineRateLimitStore store = new CaffeineRateLimitStore(1_000);

        long v1 = store.incrementAndGet("k1", 5_000);
        long v2 = store.incrementAndGet("k1", 5_000);
        long v3 = store.incrementAndGet("k1", 5_000);

        assertEquals(1L, v1);
        assertEquals(2L, v2);
        assertEquals(3L, v3);
        assertEquals(3L, store.get("k1"));
    }

    @Test
    @DisplayName("Different keys are isolated")
    void isolatedKeys() {
        CaffeineRateLimitStore store = new CaffeineRateLimitStore(1_000);

        store.incrementAndGet("A", 5_000);
        store.incrementAndGet("A", 5_000);
        store.incrementAndGet("B", 5_000);

        assertEquals(2L, store.get("A"));
        assertEquals(1L, store.get("B"));
    }

    @Test
    @DisplayName("Entries expire after TTL")
    void respectsTtlExpiry() throws InterruptedException {
        TestTicker ticker = new TestTicker();

        CaffeineRateLimitStore store = new CaffeineRateLimitStore(100, ticker);

        assertEquals(1, store.incrementAndGet("bucket", 2000)); // count=1

        // advance fake clock beyond TTL
        ticker.advance(2000, TimeUnit.MILLISECONDS);

        // trigger cache cleanup
        store.cache().cleanUp();

        assertEquals(0, store.get("bucket"));
    }

    @Test
    @DisplayName("Concurrent increments are aggregated correctly")
    void concurrentIncrements() throws Exception {
        CaffeineRateLimitStore store = new CaffeineRateLimitStore(10_000);

        final String key = "concurrent";
        final int threads = 8;
        final int perThread = 200;
        final long ttlMs = 2_000;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            tasks.add(() -> {
                for (int i = 0; i < perThread; i++) {
                    store.incrementAndGet(key, ttlMs);
                }
                return null;
            });
        }

        List<Future<Void>> futures = pool.invokeAll(tasks);
        for (Future<Void> f : futures) f.get(5, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));

        long expected = (long) threads * perThread;
        long actual = store.get(key);

        assertEquals(expected, actual, "All increments should be counted atomically");
    }
}
