package com.example.e2e;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface EmployeeRepository extends R2dbcRepository<Employee, Long> {
}
