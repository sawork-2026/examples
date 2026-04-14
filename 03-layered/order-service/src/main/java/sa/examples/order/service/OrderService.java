package sa.examples.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.examples.order.model.Order;
import sa.examples.order.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    // 依赖注入：通过构造器注入 Repository（推荐方式）
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + id));
    }

    // 业务规则 + 事务管理（AOP 的一种体现）
    @Transactional
    public Order createOrder(String items, double price) {
        if (items == null || items.isBlank()) {
            throw new IllegalArgumentException("商品不能为空");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("价格必须大于零");
        }
        return orderRepository.save(new Order(items, price));
    }
}
