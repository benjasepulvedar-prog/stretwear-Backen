package cl.duoc.streetwear_backend.repositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import cl.duoc.streetwear_backend.models.PurchaseOrder;

@Repository
public class OrderRepository {

    private final Map<Long, PurchaseOrder> orders = new LinkedHashMap<>();
    private long nextId = 1;

    public synchronized PurchaseOrder save(PurchaseOrder order) {
        if (order.getId() == null) {
            order.setId(nextId++);
        }
        orders.put(order.getId(), order);
        return order;
    }

    public synchronized Optional<PurchaseOrder> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public synchronized List<PurchaseOrder> findAll() {
        List<PurchaseOrder> result = new ArrayList<>(orders.values());
        Collections.reverse(result);
        return result;
    }

    public synchronized List<PurchaseOrder> findByUserIdOrderByIdDesc(Long userId) {
        return findAll().stream().filter(order -> order.getUserId().equals(userId)).toList();
    }

    public synchronized long count() {
        return orders.size();
    }

    public synchronized void deleteAll() {
        orders.clear();
    }
}
