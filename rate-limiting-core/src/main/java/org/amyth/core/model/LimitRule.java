package org.amyth.core.model;

import java.time.Duration;

public record LimitRule(long permits, Duration window) {}
