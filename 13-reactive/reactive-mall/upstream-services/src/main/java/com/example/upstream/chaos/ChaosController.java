package com.example.upstream.chaos;

import java.util.Map;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/chaos")
public class ChaosController {

    private final ChaosMode chaosMode;

    public ChaosController(ChaosMode chaosMode) {
        this.chaosMode = chaosMode;
    }

    @PostMapping("/{mode}")
    public Mono<Map<String, String>> setMode(@PathVariable String mode) {
        ChaosMode.Mode m = ChaosMode.Mode.valueOf(mode.toUpperCase());
        chaosMode.set(m);
        return Mono.just(Map.of("chaos", m.name()));
    }

    @GetMapping
    public Mono<Map<String, String>> getMode() {
        return Mono.just(Map.of("chaos", chaosMode.get().name()));
    }
}
