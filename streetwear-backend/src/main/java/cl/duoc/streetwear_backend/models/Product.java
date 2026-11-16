package cl.duoc.streetwear_backend.models;

import java.math.BigDecimal;

public class Product {

    private Long id;

    private String name;

    private String category;

    private BigDecimal price;

    private Integer stock;

    private String image;

    private boolean featured;

    public Product() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getImage() { return image; }
    public boolean isFeatured() { return featured; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setImage(String image) { this.image = image; }
    public void setFeatured(boolean featured) { this.featured = featured; }
}
