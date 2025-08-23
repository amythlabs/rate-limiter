package org.amyth.autoconfigure.mvc;

import org.amyth.core.annotation.RateLimit;
import org.amyth.core.api.KeyStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/mvc")
class TestMvcController {

    // 3 req/min per IP
    @RateLimit(permits = 3, window = 1, unit = TimeUnit.MINUTES,
            strategy = KeyStrategy.IP, sendHeaders = true)
    @GetMapping("/hello")
    ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }

    // 2 req/min per header value
    @RateLimit(permits = 2, window = 1, unit = TimeUnit.MINUTES,
            key = "#request.getHeader('X-Api-Key')", sendHeaders = true)
    @GetMapping("/by-key")
    ResponseEntity<String> byKey() {
        return ResponseEntity.ok("ok");
    }
}
