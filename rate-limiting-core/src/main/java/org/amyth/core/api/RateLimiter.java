package org.amyth.core.api;

import org.amyth.core.model.HitResult;
import org.amyth.core.model.LimitRule;

public interface RateLimiter {
    HitResult hit(String key, LimitRule rule);
}
