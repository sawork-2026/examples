package com.example.loan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StreamLoanApplicationTest {

    @Test
    void loanRecordFields() {
        Loan loan = new Loan("aaaaaaaa-1111-2222-3333-444444444444", "Alice", 5000);
        assertEquals("aaaaaaaa-1111-2222-3333-444444444444", loan.uuid());
        assertEquals("Alice", loan.name());
        assertEquals(5000, loan.amount());
        assertEquals("PENDING", loan.status());
    }

    @Test
    void loanWithStatus() {
        Loan loan = new Loan("aaaaaaaa-1111-2222-3333-444444444444", "Alice", 5000);
        Loan approved = loan.withStatus("APPROVED");
        assertEquals("APPROVED", approved.status());
        assertEquals("PENDING", loan.status());
    }

    @Test
    void checkLoanApproves() {
        var app = new StreamLoanApplication();
        var fn = app.checkLoan();
        Loan loan = new Loan("aaaaaaaa-1111-2222-3333-444444444444", "Alice", 5000);
        Loan result = fn.apply(loan);
        assertEquals("APPROVED", result.status());
    }

    @Test
    void checkLoanDeclines() {
        var app = new StreamLoanApplication();
        var fn = app.checkLoan();
        Loan loan = new Loan("bbbbbbbb-1111-2222-3333-444444444444", "Bob", 15000);
        Loan result = fn.apply(loan);
        assertEquals("DECLINED", result.status());
    }
}
