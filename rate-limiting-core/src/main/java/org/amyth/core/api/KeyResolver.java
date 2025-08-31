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
 * Interface for resolving rate limit keys from HTTP requests.
 * Implementations can extract keys based on different request attributes
 * such as IP address, headers, cookies, or request parameters.
 */
public interface KeyResolver {
    /**
     * Resolves the rate limit key from the given HTTP request context.
     *
     * @param ctx the HTTP request context containing request information
     * @return a string key that will be used for rate limiting
     */
    String resolve(HttpRequestContext ctx); // your own minimal ctx abstraction
}
