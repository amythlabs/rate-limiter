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

import org.amyth.core.model.HitResult;
import org.amyth.core.model.LimitRule;

/**
 * Core interface for rate limiting operations.
 * Implementations provide different rate limiting algorithms such as
 * fixed window, sliding window, or token bucket.
 */
public interface RateLimiter {

    /**
     * Records a hit against the rate limit and returns the result.
     *
     * @param key  the unique identifier for the rate limit bucket
     * @param rule the rate limiting rule defining permits and time window
     * @return a HitResult containing whether the request is allowed and remaining permits
     */
    HitResult hit(String key, LimitRule rule);
}
