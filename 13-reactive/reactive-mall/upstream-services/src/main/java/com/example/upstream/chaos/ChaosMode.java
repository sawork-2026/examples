package com.example.upstream.chaos;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 故障注入组件——通过 /api/admin/chaos/{mode} 切换模式。
 * 各 Controller 调用 apply() 给正常响应加上延迟或直接失败。
 *
 *   NORMAL — 正常响应（50ms 模拟延迟）
 *   SLOW   — 慢响应（2s 延迟，用于触发 order-service 的 timeout）
 *   FAIL   — 直接 500（用于触发 order-service 的 onErrorResume）
 */
@Component
public class ChaosMode {

    public enum Mode { NORMAL, SLOW, FAIL }

    private final AtomicReference<Mode> current = new AtomicReference<>(Mode.NORMAL);

    public void set(Mode mode) {
        current.set(mode);
        System.out.println("[ChaosMode] switched to " + mode);
    }

    public Mode get() {
        return current.get();
    }

    /**
     * 包装一个 Mono 响应：根据当前模式注入延迟或故障
     */
    public <T> Mono<T> apply(Mono<T> original) {
        return switch (current.get()) {
            case NORMAL -> original.delayElement(Duration.ofMillis(50));
            case SLOW   -> original.delayElement(Duration.ofSeconds(2));
            case FAIL   -> Mono.error(new RuntimeException("Chaos: service unavailable"));
        };
    }
}
