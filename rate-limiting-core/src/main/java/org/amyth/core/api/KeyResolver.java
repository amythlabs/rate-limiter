package org.amyth.core.api;

public interface KeyResolver {
    String resolve(HttpRequestContext ctx); // your own minimal ctx abstraction
}
