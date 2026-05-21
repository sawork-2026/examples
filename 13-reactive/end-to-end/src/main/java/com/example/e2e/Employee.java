package com.example.e2e;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("employees")
public record Employee(@Id Long id, String name, String department) {}
