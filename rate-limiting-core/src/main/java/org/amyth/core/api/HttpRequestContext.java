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
    /**
     * Returns the client IP address, possibly taking into account proxy headers.
     *
     * @return the client's IP address as a string
     */
    String clientIp();

    /**
     * Returns the value of the specified cookie.
     *
     * @param name the name of the cookie to retrieve
     * @return an Optional containing the cookie value, or empty if not present
     */
    Optional<String> cookie(String name);

    /**
     * Returns the value of the specified HTTP header.
     *
     * @param name the name of the header to retrieve
     * @return an Optional containing the header value, or empty if not present
     */
    Optional<String> header(String name);

    /**
     * Returns the authenticated user's identifier.
     *
     * @return an Optional containing the user ID, or empty if user is not authenticated
     */
    Optional<String> userId();
}
