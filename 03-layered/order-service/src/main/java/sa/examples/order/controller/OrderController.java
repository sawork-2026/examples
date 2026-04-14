package sa.examples.order.controller;

import org.springframework.web.bind.annotation.*;
import sa.examples.order.model.Order;
import sa.examples.order.service.OrderService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    // 依赖注入：通过构造器注入 Service
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> list() {
        return orderService.listAll();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping
    public Order create(@RequestBody Map<String, Object> body) {
        String items = (String) body.get("items");
        double price = ((Number) body.get("price")).doubleValue();
        return orderService.createOrder(items, price);
    }
}
