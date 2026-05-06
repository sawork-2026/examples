package com.example.loan;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StreamLoanApplication {

    private static final long MAX_AMOUNT = 10000L;

    public static void main(String[] args) {
        SpringApplication.run(StreamLoanApplication.class, args);
    }

    @Bean
    public Supplier<Loan> supplyLoan() {
        return () -> {
            Loan loan = new Loan(UUID.randomUUID().toString(), "Applicant", (long) (Math.random() * 20000));
            System.out.println("[Source]  " + loan.status() + " " + loan.uuid().substring(0, 8)
                    + " $" + loan.amount() + " for " + loan.name());
            return loan;
        };
    }

    @Bean
    public Function<Loan, Loan> checkLoan() {
        return loan -> {
            String status = loan.amount() > MAX_AMOUNT ? "DECLINED" : "APPROVED";
            Loan checked = loan.withStatus(status);
            System.out.println("[Check]  " + checked.status() + " " + checked.uuid().substring(0, 8)
                    + " $" + checked.amount());
            return checked;
        };
    }

    @Bean
    public Consumer<Loan> logLoan() {
        return loan -> System.out.println("[Result]  " + loan.status() + " " + loan.uuid().substring(0, 8)
                + " $" + loan.amount() + " for " + loan.name());
    }
}
