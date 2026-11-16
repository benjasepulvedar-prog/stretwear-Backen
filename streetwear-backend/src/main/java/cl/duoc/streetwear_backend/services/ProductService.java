package cl.duoc.streetwear_backend.services;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.streetwear_backend.dto.ProductRequest;
import cl.duoc.streetwear_backend.models.Product;
import cl.duoc.streetwear_backend.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public List<Product> findAll() {
        return products.findAll();
    }

    public Product findById(Long id) {
        return products.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Product create(ProductRequest request) {
        synchronized (products) {
            Product product = new Product();
            apply(product, request);
            return products.save(product);
        }
    }

    public Product update(Long id, ProductRequest request) {
        synchronized (products) {
            Product product = findById(id);
            apply(product, request);
            return products.save(product);
        }
    }

    public void delete(Long id) {
        synchronized (products) {
            products.delete(findById(id));
        }
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setCategory(request.category().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImage(request.image().trim());
        product.setFeatured(request.featured());
    }
}
