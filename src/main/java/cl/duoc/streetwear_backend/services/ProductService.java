package cl.duoc.streetwear_backend.services;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
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
        return products.findAll(Sort.by("id"));
    }

    public Product findById(Long id) {
        return products.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Product create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return products.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findForUpdate(id);
        apply(product, request);
        return product;
    }

    @Transactional
    public void delete(Long id) {
        products.delete(findForUpdate(id));
    }

    private Product findForUpdate(Long id) {
        return products.findByIdForUpdate(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
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
