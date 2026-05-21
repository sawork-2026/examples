package com.example.employee;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class EmployeeController {

    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/employees", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Employee> getAllEmployees() {
        return repository.findAllEmployees()
                .delayElements(Duration.ofMillis(100))
                .doOnNext(e -> System.out.println("Server produces: " + e));
    }
}
