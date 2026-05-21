package com.example.resilient;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/**
 * 模拟不稳定的远端依赖：可以切换"模式"来制造不同失败场景。
 */
@Component
public class FlakyUpstream {

    public enum Mode { OK, SLOW, FAIL, FLAKY }

    private final AtomicReference<Mode> mode = new AtomicReference<>(Mode.OK);

    public void setMode(Mode m) { mode.set(m); }
    public Mode getMode() { return mode.get(); }

    public Mono<String> call() {
        return Mono.defer(() -> switch (mode.get()) {
            case OK    -> Mono.just("ok-" + System.currentTimeMillis()).delayElement(Duration.ofMillis(50));
            case SLOW  -> Mono.just("slow-result").delayElement(Duration.ofMillis(2000));
            case FAIL  -> Mono.error(new RuntimeException("upstream down"));
            case FLAKY -> ThreadLocalRandom.current().nextInt(3) == 0
                    ? Mono.just("flaky-ok").delayElement(Duration.ofMillis(50))
                    : Mono.error(new RuntimeException("flaky transient error"));
        });
    }
}
