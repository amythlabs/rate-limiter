package org.amyth.core.keys;

import org.amyth.core.api.HttpRequestContext;
import org.amyth.core.api.KeyResolver;

public final class IpKeyResolver implements KeyResolver {
    public String resolve(HttpRequestContext ctx) {
        return ctx.clientIp(); // honor X-Forwarded-For if configured
    }
}
