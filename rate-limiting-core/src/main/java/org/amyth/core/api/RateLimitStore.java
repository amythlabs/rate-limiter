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

public interface RateLimitStore {
    /**
     * Atomically add a hit for a window bucket and return current count.
     * For sliding window you’ll manage multiple buckets (e.g., per-minute).
     */
    long incrementAndGet(String bucketKey, long ttlMillis);
    long get(String bucketKey);
    void expire(String bucketKey, long ttlMillis);
}
