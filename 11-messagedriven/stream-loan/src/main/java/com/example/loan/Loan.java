package com.example.loan;

public record Loan(String uuid, String name, long amount, String status) {

    public Loan(String uuid, String name, long amount) {
        this(uuid, name, amount, "PENDING");
    }

    public Loan withStatus(String status) {
        return new Loan(uuid, name, amount, status);
    }
}
