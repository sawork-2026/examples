package com.example.bookstore.dao;

import com.example.bookstore.model.Book;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private static List<Book> books = new ArrayList<>();
    private static Long nextId = 1L;

    static {
        books.add(new Book(nextId++, "Effective Java", "Joshua Bloch",
                          new BigDecimal("59.99"), "978-0134685991"));
        books.add(new Book(nextId++, "Clean Code", "Robert Martin",
                          new BigDecimal("49.99"), "978-0132350884"));
        books.add(new Book(nextId++, "Design Patterns", "Gang of Four",
                          new BigDecimal("54.99"), "978-0201633610"));
    }

    public List<Book> findAll() {
        return new ArrayList<>(books);
    }

    public Book findById(Long id) {
        return books.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
