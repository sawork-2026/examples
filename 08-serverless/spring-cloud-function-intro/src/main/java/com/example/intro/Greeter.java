package com.example.intro;

import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * POJO-style function: a class implementing Function directly,
 * instead of declaring the lambda inside an @Bean method.
 *
 * Spring Cloud Function discovers it via the @Component scan and
 * exposes it at POST /greeter just like the @Bean variants.
 */
@Component("greeter")
public class Greeter implements Function<String, String> {
    @Override
    public String apply(String s) {
        return "Hello " + s + ", welcome to Spring Cloud Function!";
    }
}
