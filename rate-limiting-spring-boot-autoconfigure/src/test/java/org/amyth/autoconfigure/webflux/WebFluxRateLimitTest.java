package org.amyth.autoconfigure.webflux;

import org.amyth.autoconfigure.RateLimitingAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = TestFluxController.class)
@ImportAutoConfiguration(RateLimitingAutoConfiguration.class)
@ContextConfiguration(classes = ReactiveTestBootApp.class)
@TestPropertySource(properties = {
        "ratelimit.backend=CAFFEINE",
        "ratelimit.sliding-buckets=6",
        "ratelimit.include-forwarded-for=true"
})
public class WebFluxRateLimitTest {

    @Autowired WebTestClient client;

    @Test
    void allows_first_2_streams_then_blocks_3rd_by_ip() {
        // 1st allowed
        client.get().uri("/flux/sse").exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-RateLimit-Limit", "2");

        // 2nd allowed
        client.get().uri("/flux/sse").exchange()
                .expectStatus().isOk();

        // 3rd within a minute -> 429
        client.get().uri("/flux/sse").exchange()
                .expectStatus().isEqualTo(429)
                .expectHeader().exists("Retry-After")
                .expectHeader().valueEquals("X-RateLimit-Remaining", "0");
    }

    @Test
    void limits_per_header_value_independently() {
        client.get().uri("/flux/by-key").header("X-Api-Key", "A").exchange()
                .expectStatus().isOk();
        client.get().uri("/flux/by-key").header("X-Api-Key", "A").exchange()
                .expectStatus().isOk();
        // Third for A blocked
        client.get().uri("/flux/by-key").header("X-Api-Key", "A").exchange()
                .expectStatus().isEqualTo(429);

        // Different key -> separate bucket
        client.get().uri("/flux/by-key").header("X-Api-Key", "B").exchange()
                .expectStatus().isOk();
    }

    @Test
    void honors_x_forwarded_for_when_enabled() {
        client.get().uri("/flux/sse")
                .header("X-Forwarded-For", "198.51.100.1")
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/flux/sse")
                .header("X-Forwarded-For", "198.51.100.2")
                .exchange()
                .expectStatus().isOk();
    }
}
