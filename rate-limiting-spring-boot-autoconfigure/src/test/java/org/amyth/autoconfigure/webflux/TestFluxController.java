package org.amyth.autoconfigure.webflux;

import org.amyth.core.annotation.RateLimit;
import org.amyth.core.api.KeyStrategy;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RestController
class TestFluxController {

    // 2 streams per minute per IP
    @RateLimit(permits = 2, window = 1, unit = TimeUnit.MINUTES,
            strategy = KeyStrategy.IP, sendHeaders = true)
    @GetMapping(value = "/flux/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ServerSentEvent<String>> sse() {
        return Flux.interval(Duration.ofMillis(100))
                .take(5)
                .map(i -> ServerSentEvent.builder("tick-" + i).event("tick").build());
    }

    // 2 per header value
    @RateLimit(permits = 2, window = 1, unit = TimeUnit.MINUTES,
            key = "#request.getHeaders().getFirst('X-Api-Key')", sendHeaders = true)
    @GetMapping("/flux/by-key")
    Flux<String> byKey() {
        return Flux.interval(Duration.ofMillis(50)).take(3).map(i -> "v" + i);
    }
}
