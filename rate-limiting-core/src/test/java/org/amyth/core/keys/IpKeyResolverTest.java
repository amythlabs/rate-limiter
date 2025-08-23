package org.amyth.core.keys;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpKeyResolverTest {

    @Test
    @DisplayName("Returns clientIp from context")
    void returnsClientIp() {
        var ctx = new FakeHttpRequestContext().clientIp("203.0.113.10");

        var r = new IpKeyResolver();
        String key = r.resolve(ctx);

        assertEquals("203.0.113.10", key);
    }

    @Test
    @DisplayName("Defaults to 'unknown' if context clientIp is not set")
    void returnsUnknownWhenMissing() {
        var ctx = new FakeHttpRequestContext(); // default "unknown"

        var r = new IpKeyResolver();
        String key = r.resolve(ctx);

        assertEquals("unknown", key);
    }
}
