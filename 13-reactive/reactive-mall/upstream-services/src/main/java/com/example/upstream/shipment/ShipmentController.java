package com.example.upstream.shipment;

import java.util.UUID;

import com.example.upstream.chaos.ChaosMode;
import com.example.upstream.model.ShipmentRequest;
import com.example.upstream.model.ShipmentResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ChaosMode chaos;

    public ShipmentController(ChaosMode chaos) {
        this.chaos = chaos;
    }

    @PostMapping
    public Mono<ShipmentResult> create(@RequestBody ShipmentRequest req) {
        String id = "SHIP-" + UUID.randomUUID().toString().substring(0, 8);
        return chaos.apply(Mono.just(new ShipmentResult(id, "CREATED")));
    }
}
