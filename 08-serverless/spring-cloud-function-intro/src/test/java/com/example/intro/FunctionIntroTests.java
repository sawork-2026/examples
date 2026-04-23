package com.example.intro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.FunctionCatalog;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Looks up each function via FunctionCatalog — the same mechanism Spring Cloud Function
 * uses internally — and verifies Function / Consumer / Supplier / reactive / composition
 * all behave as expected.
 */
@SpringBootTest
class FunctionIntroTests {

    @Autowired
    FunctionCatalog catalog;

    @Test
    void uppercase_transformsInput() {
        Function<String, String> fn = catalog.lookup("uppercase");
        assertThat(fn.apply("hello")).isEqualTo("HELLO");
    }

    @Test
    void greeter_isAPojoFunction() {
        Function<String, String> fn = catalog.lookup("greeter");
        assertThat(fn.apply("world"))
                .isEqualTo("Hello world, welcome to Spring Cloud Function!");
    }

    @Test
    void uppercaseFlux_processesReactiveStream() {
        Function<Flux<String>, Flux<String>> fn = catalog.lookup("uppercaseFlux");
        StepVerifier.create(fn.apply(Flux.just("a", "b", "c")))
                .expectNext("A", "B", "C")
                .verifyComplete();
    }

    @Test
    void logEvent_consumesWithoutReturn() {
        Consumer<String> fn = catalog.lookup("logEvent");
        fn.accept("test-event");  // no throw = success
    }

    @Test
    void timestamp_producesWithoutInput() {
        Supplier<String> fn = catalog.lookup("timestamp");
        assertThat(fn.get()).isNotBlank();
    }

    @Test
    void counter_isStatefulAcrossCalls() {
        Supplier<Long> fn = catalog.lookup("counter");
        long first = fn.get();
        long second = fn.get();
        assertThat(second).isEqualTo(first + 1);
    }

    @Test
    void composition_pipesUppercaseIntoReverse() {
        // Spring Cloud Function resolves "uppercase|reverseString" as
        // reverseString(uppercase(input)).
        Function<String, String> fn = catalog.lookup("uppercase|reverseString");
        assertThat(fn.apply("hello")).isEqualTo("OLLEH");
    }
}
