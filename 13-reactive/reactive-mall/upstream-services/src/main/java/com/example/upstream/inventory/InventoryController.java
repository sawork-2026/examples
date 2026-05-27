package com.example.upstream.inventory;

import java.util.Map;

import com.example.upstream.chaos.ChaosMode;
import com.example.upstream.model.DeductRequest;
import com.example.upstream.model.InventoryInfo;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryStore store;
    private final ChaosMode chaos;

    public InventoryController(InventoryStore store, ChaosMode chaos) {
        this.store = store;
        this.chaos = chaos;
    }

    @GetMapping("/{productId}")
    public Mono<InventoryInfo> check(@PathVariable String productId) {
        int stock = store.getStock(productId);
        if (stock < 0) {
            return chaos.apply(Mono.error(
                    new RuntimeException("Product not found: " + productId)));
        }
        return chaos.apply(Mono.just(new InventoryInfo(productId, stock)));
    }

    @PostMapping("/deduct")
    public Mono<Map<String, Object>> deduct(@RequestBody DeductRequest req) {
        boolean ok = store.deduct(req.productId(), req.quantity());
        return chaos.apply(Mono.just(Map.of(
                "success", ok,
                "productId", req.productId(),
                "remaining", store.getStock(req.productId()))));
    }
}
