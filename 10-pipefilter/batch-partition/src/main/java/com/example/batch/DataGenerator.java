package com.example.batch;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Random;

public class DataGenerator {

    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Carol", "David", "Eve", "Frank", "Grace", "Henry",
            "Ivy", "Jack", "Karen", "Leo", "Mia", "Nick", "Olivia", "Paul",
            "Quinn", "Rose", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xander"
    };
    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Wilson", "Anderson", "Taylor", "Lee"
    };

    record DeptConfig(String name, String fileName, int count) {}

    public static void main(String[] args) throws IOException {
        int perDept = 33_333;
        Path dir = Path.of("src/main/resources");

        DeptConfig[] depts = {
                new DeptConfig("Engineering", "data-engineering.csv", perDept),
                new DeptConfig("Sales", "data-sales.csv", perDept + 1),
                new DeptConfig("HR", "data-hr.csv", perDept),
        };

        Random rng = new Random(42);
        int total = 0;
        for (DeptConfig dept : depts) {
            Path file = dir.resolve(dept.fileName);
            try (PrintWriter pw = new PrintWriter(file.toFile())) {
                for (int i = 0; i < dept.count; i++) {
                    String first = FIRST_NAMES[rng.nextInt(FIRST_NAMES.length)];
                    String last = LAST_NAMES[rng.nextInt(LAST_NAMES.length)];
                    int salary = 40_000 + rng.nextInt(80_000);
                    pw.printf("%s,%s,%s,%d%n", first, last, dept.name, salary);
                }
            }
            total += dept.count;
            System.out.printf("Generated %s: %,d rows%n", dept.fileName, dept.count);
        }
        System.out.printf("Total: %,d rows%n", total);
    }
}
