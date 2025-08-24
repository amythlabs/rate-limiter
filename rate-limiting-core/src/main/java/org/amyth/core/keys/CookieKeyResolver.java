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

public final class CookieKeyResolver implements KeyResolver {
    private final String cookieName;

    public CookieKeyResolver(String cookieName) {
        this.cookieName = cookieName;
    }

    public String resolve(HttpRequestContext ctx) { return ctx.cookie(cookieName).orElse("nocookie"); }
}
