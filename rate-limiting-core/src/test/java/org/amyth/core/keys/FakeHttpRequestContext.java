package org.amyth.core.keys;

import org.amyth.core.api.HttpRequestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class FakeHttpRequestContext implements HttpRequestContext {
    private String clientIp = "unknown";
    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private String userId;

    public FakeHttpRequestContext clientIp(String ip) {
        this.clientIp = ip;
        return this;
    }

    public FakeHttpRequestContext cookie(String name, String value) {
        this.cookies.put(name, value);
        return this;
    }

    public FakeHttpRequestContext header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public FakeHttpRequestContext user(String id) {
        this.userId = id;
        return this;
    }

    @Override public String clientIp() { return clientIp; }
    @Override public Optional<String> cookie(String name) { return Optional.ofNullable(cookies.get(name)); }
    @Override public Optional<String> header(String name) { return Optional.ofNullable(headers.get(name)); }
    @Override public Optional<String> userId() { return Optional.ofNullable(userId); }
}
