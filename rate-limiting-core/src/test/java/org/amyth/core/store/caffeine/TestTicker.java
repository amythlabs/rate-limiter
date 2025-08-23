package org.amyth.core.store.caffeine;

import com.github.benmanes.caffeine.cache.Ticker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class TestTicker implements Ticker {
    private final AtomicLong nanos = new AtomicLong();

    @Override
    public long read() {
        return nanos.get();
    }

    void advance(long time, TimeUnit unit) {
        nanos.addAndGet(unit.toNanos(time));
    }
}
