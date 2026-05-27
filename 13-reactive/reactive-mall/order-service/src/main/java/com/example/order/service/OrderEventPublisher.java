package com.example.order.service;

import java.time.Duration;

import com.example.order.model.OrderEvent;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 订单事件发布器——基于 Reactor Sinks 实现 SSE 推送。
 *
 * Sinks.Many 是一个编程式的 Publisher：
 *   - emit() 非阻塞地向 Sink 推入事件
 *   - stream() 返回按 orderId 过滤的 Flux，作为 SSE 端点的数据源
 *
 * 使用 replay().limit(5min)：迟到的 SSE 客户端仍能看到过去 5 分钟的事件。
 */
@Component
public class OrderEventPublisher {

    private final Sinks.Many<OrderEvent> sink =
            Sinks.many().replay().limit(Duration.ofMinutes(5));

    public void emit(OrderEvent event) {
        sink.tryEmitNext(event);
        System.out.printf("  [EVENT] %s | %s | %s%n", event.orderId(), event.step(), event.status());
    }

    public Flux<ServerSentEvent<OrderEvent>> stream(String orderId) {
        return sink.asFlux()
                .filter(e -> e.orderId().equals(orderId))
                .map(e -> ServerSentEvent.<OrderEvent>builder()
                        .event(e.step())
                        .data(e)
                        .build());
    }
}
