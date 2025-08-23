package org.amyth.core.keys;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CookieKeyResolverTest {

    @Test
    @DisplayName("Returns cookie value when target cookie is present")
    void returnsCookieValue() {
        var ctx = new FakeHttpRequestContext()
                .cookie("api", "A1")
                .cookie("other", "zzz");

        var r = new CookieKeyResolver("api");
        String key = r.resolve(ctx);

        assertEquals("A1", key);
    }

    @Test
    @DisplayName("Returns 'nocookie' when target cookie is absent")
    void returnsFallbackWhenMissing() {
        var ctx = new FakeHttpRequestContext()
                .cookie("not-api", "x");

        var r = new CookieKeyResolver("api");
        String key = r.resolve(ctx);

        assertEquals("nocookie", key);
    }

    @Test
    @DisplayName("Allows empty-string cookie values (no fallback)")
    void emptyCookieIsAccepted() {
        var ctx = new FakeHttpRequestContext()
                .cookie("api", ""); // Optional.of("") → resolver returns ""

        var r = new CookieKeyResolver("api");
        String key = r.resolve(ctx);

        // Current behavior: returns empty string. Change resolver if you prefer fallback on blank.
        assertEquals("", key);
    }
}
