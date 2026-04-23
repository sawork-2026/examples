package com.example.intro;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Demonstrates the three core function types of Spring Cloud Function,
 * matching the slides in 08-serverless.md (function types and examples).
 *
 * Each bean is exposed automatically as an HTTP endpoint by spring-cloud-function-web:
 *
 *   POST /uppercase            body: hello             -> HELLO
 *   POST /reverseString        body: hello             -> olleh
 *   POST /uppercaseFlux        body: ["a","b","c"]     -> ["A","B","C"]
 *   POST /logEvent             body: anything          -> 202 Accepted (no body)
 *   GET  /timestamp                                    -> current ISO timestamp
 *   POST /uppercaseReverse     body: hello             -> OLLEH  (composed via YAML)
 *   POST /greeter              body: world             -> Hello world, ...
 */
@Configuration
public class FunctionConfig {

    /** Function<I,O>: input -> output. The most common shape — HTTP request handler. */
    @Bean
    public Function<String, String> uppercase() {
        return value -> value.toUpperCase();
    }

    /** Another Function used for composition (uppercase|reverseString). */
    @Bean
    public Function<String, String> reverseString() {
        return value -> new StringBuilder(value).reverse().toString();
    }

    /** Reactive Function: process an async stream rather than a single value. */
    @Bean
    public Function<Flux<String>, Flux<String>> uppercaseFlux() {
        return flux -> flux.map(String::toUpperCase);
    }

    /** Consumer<I>: side-effect only, no return value — fits event/message handlers. */
    @Bean
    public Consumer<String> logEvent() {
        return value -> System.out.println("[event] " + value);
    }

    /** Supplier<O>: produces output without input — fits scheduled producers, health probes. */
    @Bean
    public Supplier<String> timestamp() {
        return () -> LocalDateTime.now().toString();
    }

    /** Stateful Supplier showing that beans are singletons — counter survives across calls. */
    @Bean
    public Supplier<Long> counter() {
        AtomicLong n = new AtomicLong();
        return n::incrementAndGet;
    }
}
