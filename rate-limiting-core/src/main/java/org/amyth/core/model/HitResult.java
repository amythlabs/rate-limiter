package org.amyth.core.model;

import java.time.Instant;

public record HitResult(boolean allowed, long remaining, Instant resetAt) {}
