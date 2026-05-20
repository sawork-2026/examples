package com.example.employee;

import java.util.List;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class EmployeeRepository {

    private static final List<Employee> DATA = List.of(
            new Employee(1L, "Alice", "Engineering"),
            new Employee(2L, "Bob", "Engineering"),
            new Employee(3L, "Carol", "Product"),
            new Employee(4L, "Dave", "Design"),
            new Employee(5L, "Eve", "Engineering"),
            new Employee(6L, "Frank", "Product"),
            new Employee(7L, "Grace", "Design"),
            new Employee(8L, "Heidi", "Engineering")
    );

    public Flux<Employee> findAllEmployees() {
        return Flux.fromIterable(DATA);
    }
}
