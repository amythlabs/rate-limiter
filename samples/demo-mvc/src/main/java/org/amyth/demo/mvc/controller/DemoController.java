package org.amyth.demo.mvc.controller;

import org.amyth.core.annotation.RateLimit;
import org.amyth.core.api.KeyStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
public class DemoController {

    // 5 req/min per IP
    @RateLimit(permits = 5, window = 1, unit = TimeUnit.MINUTES, strategy = KeyStrategy.IP)
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }

    // 3 req/min per API key (SpEL)
    @RateLimit(permits = 3, window = 1, unit = TimeUnit.MINUTES,
            key = "#request.getHeader('X-Api-Key')")
    @GetMapping("/by-key")
    public ResponseEntity<String> byKey() {
        return ResponseEntity.ok("ok-by-key");
    }
}
