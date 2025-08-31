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

package org.amyth.core.annotation;

import org.amyth.core.api.KeyStrategy;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Annotation for rate limiting method or class invocations.
 * <p>
 * Use {@code @RateLimit} to restrict the number of allowed requests within a specified time window.
 * Supports various keying strategies (IP, user, header, cookie, custom) and flexible configuration.
 * </p>
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Example 1: Limit to 5 requests per hour per user
 * @RateLimit(permits = 5, window = 1, unit = TimeUnit.HOURS, key = "#userId", strategy = KeyStrategy.USER)
 * public void getProfile() { ... }
 *
 * // Example 2: Limit to 100 requests per minute per IP
 * @RateLimit(permits = 100, window = 1, unit = TimeUnit.MINUTES)
 * public void getData() { ... }
 *
 * // Example 3: Limit to 10 requests per day using a custom header
 * @RateLimit(permits = 10, window = 1, unit = TimeUnit.DAYS,
 *           strategy = KeyStrategy.HEADER,
 *           strategyArg = "X-Api-Key",
 *           key = "#request.getHeader('X-Api-Key')")
 * public void getReport() { ... }
 *
 * // Example 4: Limit to 20 requests per 10 seconds, using a cookie value
 * @RateLimit(permits = 20, window = 10, unit = TimeUnit.SECONDS,
 *           strategy = KeyStrategy.COOKIE,
 *           strategyArg = "SESSIONID",
 *           key = "#request.getCookie('SESSIONID')")
 * public void submitForm() { ... }
 *
 * // Example 5: Apply rate limit to all methods in a class
 * @RateLimit(permits = 50, window = 1, unit = TimeUnit.HOURS)
 * public class MyController { ... }
 * }</pre>
 * <h2>Parameters</h2>
 * <ul>
 *   <li><b>permits</b>: Maximum number of allowed requests within the specified window.</li>
 *   <li><b>window</b>: Duration of the rate limit window.</li>
 *   <li><b>unit</b>: Time unit for the window duration.</li>
 *   <li><b>key</b>: SpEL expression to extract the key for rate limiting.</li>
 *   <li><b>strategy</b>: Strategy for resolving the rate limit key.</li>
 *   <li><b>strategyArg</b>: Argument for the selected strategy, such as cookie or header name.</li>
 *   <li><b>sendHeaders</b>: Whether to send rate limit headers in the response.</li>
 *   <li><b>httpStatus</b>: HTTP status code returned when the rate limit is exceeded.</li>
 * </ul>
 * <p>
 * This annotation can be applied at the method or class level. When applied at the class level, all methods inherit the rate limit unless overridden.
 * </p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RateLimit {
    /**
     * Maximum number of allowed requests within the specified window.
     * <p>
     * For example, {@code permits = 100} allows up to 100 requests per window per key.
     * </p>
     *
     * @return the maximum number of permitted requests
     */
    long permits() default 60;

    /**
     * Duration of the rate limit window.
     * <p>
     * Used together with {@link #unit()} to define the time window (e.g., 1 HOUR, 10 SECONDS).
     * </p>
     *
     * @return the duration of the rate limit window
     */
    long window() default 60;

    /**
     * Time unit for the window duration.
     * <p>
     * Common values: {@code TimeUnit.SECONDS}, {@code TimeUnit.MINUTES}, {@code TimeUnit.HOURS}, {@code TimeUnit.DAYS}.
     * </p>
     *
     * @return the time unit for the window duration
     */
    java.util.concurrent.TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * SpEL expression to extract the key for rate limiting.
     * <p>
     * Examples: {@code "#userId"}, {@code "#request.getHeader('X-Api-Key')"}.
     * If empty, uses the default for the selected {@link #strategy()}.
     * </p>
     *
     * @return the SpEL expression for key extraction
     */
    String key() default "";

    /**
     * Strategy for resolving the rate limit key.
     * <p>
     * Options: IP (default), COOKIE, HEADER, USER, CUSTOM.
     * Determines how requests are grouped for rate limiting.
     * </p>
     *
     * @return the strategy to use for key resolution
     */
    KeyStrategy strategy() default KeyStrategy.IP;

    /**
     * Argument for the selected strategy, such as cookie or header name.
     * <p>
     * Used with COOKIE (e.g., "SESSIONID"), HEADER (e.g., "X-Api-Key"), or USER (e.g., claim name).
     * </p>
     *
     * @return the argument for the selected strategy
     */
    String strategyArg() default "";

    /**
     * Whether to send rate limit headers in the response.
     * <p>
     * If true, headers like {@code X-RateLimit-Remaining} and {@code Retry-After} are included.
     * </p>
     *
     * @return true if rate limit headers should be sent, false otherwise
     */
    boolean sendHeaders() default true;

    /**
     * HTTP status code returned when the rate limit is exceeded.
     * <p>
     * Default is 429 (Too Many Requests). Can be customized as needed.
     * </p>
     *
     * @return the HTTP status code to use when rate limit is exceeded
     */
    int httpStatus() default 429;
}
