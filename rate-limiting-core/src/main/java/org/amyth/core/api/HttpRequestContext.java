package org.amyth.core.api;

import java.util.Optional;

/**
 * Minimal HTTP request abstraction for key resolvers.
 * Lets core code work without depending on Servlet or WebFlux APIs.
 */
public interface HttpRequestContext {
    /** Returns the client IP (may include logic to handle proxies). */
    String clientIp();

    /** Returns a cookie value by name, or empty if not present. */
    Optional<String> cookie(String name);

    /** Returns a header value by name, or empty if not present. */
    Optional<String> header(String name);

    /** Returns an authenticated user identifier, or empty if not present. */
    Optional<String> userId();
}
