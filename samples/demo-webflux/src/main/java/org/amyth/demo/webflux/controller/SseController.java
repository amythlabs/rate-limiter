package org.amyth.demo.webflux.controller;

import org.amyth.core.annotation.RateLimit;
import org.amyth.core.api.KeyStrategy;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RestController
public class SseController {

    // Example event payload
    public record Tick(long seq, String at) {}

    /**
     * Streams one event every 500ms for 20 events (10 seconds total) as SSE.
     * Rate limit: 3 streams per minute per IP.
     */
    @RateLimit(
            permits = 3,
            window = 1, unit = TimeUnit.MINUTES,
            strategy = KeyStrategy.IP,
            sendHeaders = true
    )
    @GetMapping(value = "/sse/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Tick>> ticks() {
        return Flux
                .interval(Duration.ofMillis(500))
                .take(20)
                .map(i -> ServerSentEvent.<Tick>builder()
                        .id(String.valueOf(i))
                        .event("tick")
                        .data(new Tick(i, Instant.now().toString()))
                        .build()
                );
    }

    /**
     * SpEL example: per API key from header "X-Api-Key".
     * 2 streams per minute per distinct header value.
     */
    @RateLimit(
            permits = 2,
            window = 1, unit = TimeUnit.MINUTES,
            key = "#request.getHeaders().getFirst('X-Api-Key')",
            sendHeaders = true
    )
    @GetMapping(value = "/sse/by-key", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Tick>> byKey() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .map(i -> ServerSentEvent.<Tick>builder()
                        .id(String.valueOf(i))
                        .event("tick")
                        .data(new Tick(i, Instant.now().toString()))
                        .build()
                );
    }
}
