package com.example.e2e;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

/**
 * 全链路 Reactive：HTTP (WebFlux) ← Flux ← R2DBC (H2)
 *
 *   GET /users          → 一次性返回数组（同样是 Flux，但等所有元素到齐再写）
 *   GET /users/stream   → 以 NDJSON 流式输出，每条记录到位就立刻写
 *
 * 关键点：
 *   - 整条链路上没有阻塞，没有 JDBC，没有 thread-per-request
 *   - .log() 会打印 onSubscribe / request(n) / onNext / onComplete，
 *     可以看到 HTTP 订阅者通过 request(n) 反向控制 DB 的取数速率（背压）
 */
@RestController
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/users")
    public Flux<User> all() {
        return users.findAll().log("e2e.users");
    }

    @GetMapping(value = "/users/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<User> stream() {
        return users.findAll()
                .delayElements(Duration.ofMillis(200)) // 拖慢让流式效果可见
                .log("e2e.stream");
    }
}
