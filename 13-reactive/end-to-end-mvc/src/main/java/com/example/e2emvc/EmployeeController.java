package com.example.e2emvc;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    private final EmployeeRepository employees;

    public EmployeeController(EmployeeRepository employees) {
        this.employees = employees;
    }

    @GetMapping("/employees/{latency}")
    public List<Employee> all(@PathVariable long latency) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(latency);
        return employees.findAll();
    }
}
