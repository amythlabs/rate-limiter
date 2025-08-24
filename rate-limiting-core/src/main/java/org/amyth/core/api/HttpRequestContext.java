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
