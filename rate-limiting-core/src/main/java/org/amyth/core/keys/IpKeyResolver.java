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

package org.amyth.core.keys;

import org.amyth.core.api.HttpRequestContext;
import org.amyth.core.api.KeyResolver;

/**
 * A KeyResolver implementation that uses the client's IP address as the rate limit key.
 * Takes into account proxy headers if configured in the underlying implementation.
 */
public final class IpKeyResolver implements KeyResolver {

    /**
     * Default constructor for IpKeyResolver.
     */
    public IpKeyResolver() {
        // Default constructor
    }

    /**
     * Resolves the rate limit key using the client's IP address.
     *
     * @param ctx the HTTP request context
     * @return the client's IP address as the rate limit key
     */
    @Override
    public String resolve(HttpRequestContext ctx) {
        return ctx.clientIp(); // honor X-Forwarded-For if configured
    }
}
