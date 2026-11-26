package cl.duoc.streetwear_backend.models;

import java.math.BigDecimal;

public class OrderItem {

    private Long productId;

    private String name;

    private BigDecimal price;

    private Integer quantity;

    public OrderItem(Product product, Integer quantity) {
        this.productId = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
}
