package cl.duoc.streetwear_backend.repositories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import cl.duoc.streetwear_backend.models.Product;

@Repository
public class ProductRepository {

    private final Map<Long, Product> products = new LinkedHashMap<>();
    private long nextId = 1;

    public synchronized List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public synchronized Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public synchronized Product save(Product product) {
        if (product.getId() == null) {
            product.setId(nextId++);
        }
        products.put(product.getId(), product);
        return product;
    }

    public synchronized void delete(Product product) {
        products.remove(product.getId());
    }

    public synchronized long count() {
        return products.size();
    }

    public synchronized void deleteAll() {
        products.clear();
    }
}
