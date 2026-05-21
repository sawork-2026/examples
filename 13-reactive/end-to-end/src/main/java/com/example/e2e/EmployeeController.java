package com.example.e2e;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class EmployeeController {

    private final EmployeeRepository employees;

    public EmployeeController(EmployeeRepository employees) {
        this.employees = employees;
    }

    @GetMapping("/employees/{latency}")
    public Mono<java.util.List<Employee>> all(@PathVariable long latency) {
        return employees.findAll()
                .collectList()
                .delayElement(Duration.ofMillis(latency));
    }

    @GetMapping(value = "/employees/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Employee> stream() {
        return employees.findAll()
                .delayElements(Duration.ofMillis(200))
                .log("e2e.stream");
    }
}
