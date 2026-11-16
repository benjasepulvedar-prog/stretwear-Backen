package cl.duoc.streetwear_backend.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder {

    private Long id;

    private Long userId;

    private String name;

    private String email;

    private String address;

    private String delivery;

    private BigDecimal total = BigDecimal.ZERO;

    private String status = "Pendiente";

    private Instant createdAt = Instant.now();

    private List<OrderItem> items = new ArrayList<>();

    public PurchaseOrder(User buyer, String name, String address, String delivery) {
        this.userId = buyer.getId();
        this.email = buyer.getEmail();
        this.name = name;
        this.address = address;
        this.delivery = delivery;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getDelivery() { return delivery; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }

    public void setTotal(BigDecimal total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
}
