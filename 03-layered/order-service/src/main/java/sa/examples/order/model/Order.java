package sa.examples.order.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String items;

    private double total;

    private LocalDateTime createdAt;

    protected Order() {}

    public Order(String items, double total) {
        this.items = items;
        this.total = total;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getItems() { return items; }
    public double getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setItems(String items) { this.items = items; }
    public void setTotal(double total) { this.total = total; }
}
