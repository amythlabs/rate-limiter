# Rate Limiter for Spring Boot

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)

A **Spring Boot 3.x starter** for HTTP rate limiting — works with both **MVC** and **WebFlux**.  
Simple annotation-based API, batteries-included with Redis support, metrics, and standard headers.

---

## ✨ Features
- `@RateLimit` annotation (method or class level).
- Algorithms:
    - Sliding Window (default, smooth + predictable).
    - Token Bucket (optional).
- Key strategies:
    - Client IP, Header, Cookie, UserId (Principal/JWT claim).
    - Custom SpEL expressions (`#request.getHeader('X-Api-Key')`).
- Backends:
    - Caffeine (in-memory, default).
    - Redis (cluster-wide, per-key TTL).
- Works with SSE/Flux endpoints.
- Auto-configured for both MVC and WebFlux.
- Ops-friendly:
    - Standard headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`, `Retry-After`.
    - Micrometer metrics.
    - Actuator endpoint (`/actuator/ratelimit`).

---

## 🚀 Getting Started

### 1. Add the dependency

Maven:
```xml
<dependency>
  <groupId>io.github.amythlabs</groupId>
  <artifactId>spring-boot-starter-rate-limiter</artifactId>
  <version>0.1.0</version>
</dependency>
```
Gradle:

```implementation("io.github.amythlabs:spring-boot-starter-rate-limiter:0.1.0")```
### 2. Annotate your endpoints
```java
@RestController
@RequestMapping("/api")
public class MyController {
    
    // Limit to 5 requests per minute per client IP
    @RateLimit(permits = 5, window = 1, unit = TimeUnit.MINUTES, strategy = KeyStrategy.IP)
    @GetMapping("/data")
    public ResponseEntity<String> getData() {
        return ResponseEntity.ok("Here is your data!");
    }
    
    // Limit to 10 requests per hour per user (Principal name)
    @RateLimit(permits = 10, window = 1, unit = TimeUnit.HOURS, strategy = KeyStrategy.USER)
    @GetMapping("/user-info")
    public ResponseEntity<String> getUserInfo(Principal principal) {
        return ResponseEntity.ok("User info for " + principal.getName());
    }
}
```

### 3. Configure (optional)
`application.yml`
```yaml
ratelimit:
  enabled: true
  backend: caffeine   # or redis
  default-policy:
    limit: 100
    window: 60
  redis:
    host: localhost
    port: 6379
```
### 4. Key strategies
Built-in:
- IP (X-Forwarded-For aware)
- Header
- Cookie
- User (Principal name or JWT claim)
- Custom SpEL expression
```Java
@RateLimit(limit = 10, window = 60, key = "#request.getHeader('X-Api-Key')")
```
## 📊 Observability
- **Micrometer metrics:**

  Example: `rate.limit.hits{algorithm="sliding", backend="redis"}`

- **Actuator endpoint:** 

    `GET /actuator/ratelimit`

## 📦 Samples
- [samples/demo-mvc](https://github.com/amythlabs/rate-limiter/tree/main/samples/demo-mvc)
- [samples/demo-webflux](https://github.com/amythlabs/rate-limiter/tree/main/samples/demo-webflux)

## 📝 Roadmap
- Additional backends (Hazelcast, Ignite).
- More flexible key resolvers (path-based, tenant-aware).
- Advanced policy composition (burst + smoothing).

