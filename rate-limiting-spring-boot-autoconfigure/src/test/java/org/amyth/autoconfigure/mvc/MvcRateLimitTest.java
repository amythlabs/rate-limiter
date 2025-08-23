package org.amyth.autoconfigure.mvc;

import org.amyth.autoconfigure.RateLimitingAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ImportAutoConfiguration({
        RateLimitingAutoConfiguration.class
})
@TestPropertySource(properties = {
        "ratelimit.backend=CAFFEINE",
        "ratelimit.sliding-buckets=6",
        "ratelimit.include-forwarded-for=true"
})
class MvcRateLimitTest {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("Allows first 3 requests, then blocks the 4th by IP")
    void rateLimitByIp() throws Exception {
        for (int i = 1; i <= 3; i++) {
            mvc.perform(get("/mvc/hello"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-RateLimit-Limit", "3"));
        }
        mvc.perform(get("/mvc/hello"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    @DisplayName("Applies limits per header value independently")
    void rateLimitByHeaderValue() throws Exception {
        mvc.perform(get("/mvc/by-key").header("X-Api-Key", "A")).andExpect(status().isOk());
        mvc.perform(get("/mvc/by-key").header("X-Api-Key", "A")).andExpect(status().isOk());
        mvc.perform(get("/mvc/by-key").header("X-Api-Key", "A")).andExpect(status().isTooManyRequests());
        mvc.perform(get("/mvc/by-key").header("X-Api-Key", "B")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Honors X-Forwarded-For header when enabled")
    void honorsXForwardedFor() throws Exception {
        mvc.perform(get("/mvc/hello").header("X-Forwarded-For", "203.0.113.10"))
                .andExpect(status().isOk());
        mvc.perform(get("/mvc/hello").header("X-Forwarded-For", "203.0.113.11"))
                .andExpect(status().isOk());
    }
}
