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

package org.amyth.core.model;

import java.time.Instant;

/**
 * Represents the result of a rate limit check.
 *
 * @param allowed   whether the request is allowed to proceed
 * @param remaining the number of remaining permits in the current time window
 * @param resetAt   the instant when the rate limit window resets
 */
public record HitResult(boolean allowed, long remaining, Instant resetAt) {}
