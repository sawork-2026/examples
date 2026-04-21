package com.example.serverless.service;

import com.example.serverless.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * In-memory product catalog simulating a BaaS data store (e.g. Firebase Firestore).
 * In a real Serverless deployment this would delegate to the Firebase SDK or DynamoDB.
 */
@Repository
public class ProductRepository {

    private static final List<Product> CATALOG = List.of(
            new Product("DOG-001", "Dalmatian",        "Friendly dalmatian puppy",          "Dogs",     150.00),
            new Product("DOG-002", "Golden Retriever", "Adorable golden retriever",         "Dogs",     200.00),
            new Product("DOG-003", "Bulldog",          "Sturdy and loyal english bulldog",  "Dogs",     180.00),
            new Product("CAT-001", "Persian Cat",      "Elegant persian cat",               "Cats",     120.00),
            new Product("CAT-002", "Siamese Cat",      "Vocal and social siamese cat",      "Cats",     100.00),
            new Product("FISH-001", "Angel Fish",      "Graceful tropical angel fish",      "Fish",      15.00),
            new Product("FISH-002", "Tiger Shark",     "Impressive tiger shark",            "Fish",     300.00),
            new Product("BIRD-001", "Amazon Parrot",   "Colorful and talkative parrot",     "Birds",    250.00),
            new Product("REPT-001", "Green Iguana",    "Docile green iguana",               "Reptiles",  80.00)
    );

    public List<Product> search(String keyword, String category) {
        return CATALOG.stream()
                .filter(p -> isBlank(keyword) ||
                        p.name().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.description().toLowerCase().contains(keyword.toLowerCase()))
                .filter(p -> isBlank(category) ||
                        p.category().equalsIgnoreCase(category))
                .toList();
    }

    public boolean exists(String productId) {
        return CATALOG.stream().anyMatch(p -> p.id().equals(productId));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
