/*
 * Copyright 2025 amythlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.amyth.core.api;

/**
 * Strategies for resolving a unique rate-limiting key for a request.
 * <p>
 * The {@code KeyStrategy} enum defines how the rate-limiting key is determined for each request. This key is used to track and enforce rate limits per client, user, or other criteria.
 * <p>
 * <b>Usage:</b> Select the appropriate strategy in the {@code @RateLimit} annotation to control how requests are grouped for rate limiting.
 * </p>
 * <ul>
 *   <li><b>IP</b>: Uses the client's IP address. Suitable for anonymous clients or when you want to limit by network origin. May use X-Forwarded-For if configured.</li>
 *   <li><b>COOKIE</b>: Uses a specific cookie value, as specified by {@code strategyArg}. Useful for session-based rate limiting.</li>
 *   <li><b>HEADER</b>: Uses a specific HTTP header value, as specified by {@code strategyArg}. Common for API keys or custom tokens.</li>
 *   <li><b>USER</b>: Uses the authenticated user's ID or username (from Principal or JWT). Best for per-user rate limiting in authenticated systems.</li>
 *   <li><b>CUSTOM</b>: Uses a custom key resolver, either via SPI or a direct SpEL expression in {@code @RateLimit.key()}. Allows advanced or application-specific grouping.</li>
 * </ul>
 */
public enum KeyStrategy {
    /**
     * Client IP address (with optional X-Forwarded-For trust config).
     * <p>
     * Use this strategy to apply rate limits based on the originating IP address of the request. This is suitable for public APIs or when user identification is not available.
     * </p>
     */
    IP,

    /**
     * A cookie value, specified via {@code strategyArg} in {@code @RateLimit}.
     * <p>
     * Use this strategy to group requests by a specific cookie value, such as a session ID. This is useful for web applications with session-based authentication.
     * </p>
     */
    COOKIE,

    /**
     * A specific HTTP header value, specified via {@code strategyArg} in {@code @RateLimit}.
     * <p>
     * Use this strategy to group requests by a header value, such as an API key or custom token. This is common for API clients or integrations.
     * </p>
     */
    HEADER,

    /**
     * Authenticated user ID or username (from Principal/JWT).
     * <p>
     * Use this strategy to apply rate limits per authenticated user. This is recommended for systems where users are identified and authenticated.
     * </p>
     */
    USER,

    /**
     * Custom key resolver via SPI or direct {@code @RateLimit.key()} SpEL expression.
     * <p>
     * Use this strategy for advanced scenarios where the key is determined by a custom resolver or a SpEL expression. This allows for flexible, application-specific rate limiting.
     * </p>
     */
    CUSTOM
}
