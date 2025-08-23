package org.amyth.core.keys;

import org.amyth.core.api.HttpRequestContext;
import org.amyth.core.api.KeyResolver;

public final class CookieKeyResolver implements KeyResolver {
    private final String cookieName;

    public CookieKeyResolver(String cookieName) {
        this.cookieName = cookieName;
    }

    public String resolve(HttpRequestContext ctx) { return ctx.cookie(cookieName).orElse("nocookie"); }
}
