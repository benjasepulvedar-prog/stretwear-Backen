package cl.duoc.streetwear_backend.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.streetwear_backend.dto.OrderItemRequest;
import cl.duoc.streetwear_backend.dto.OrderRequest;
import cl.duoc.streetwear_backend.models.OrderItem;
import cl.duoc.streetwear_backend.models.Product;
import cl.duoc.streetwear_backend.models.PurchaseOrder;
import cl.duoc.streetwear_backend.models.User;
import cl.duoc.streetwear_backend.repositories.OrderRepository;
import cl.duoc.streetwear_backend.repositories.ProductRepository;
import cl.duoc.streetwear_backend.repositories.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders;
        this.products = products;
        this.users = users;
    }

    public PurchaseOrder create(Long buyerId, OrderRequest request) {
        synchronized (products) {
            User buyer = users.findById(buyerId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta ya no existe"));
            PurchaseOrder order = new PurchaseOrder(
                    buyer, request.name().trim(), request.address().trim(), request.delivery());
            BigDecimal total = BigDecimal.ZERO;
            Set<Long> seen = new HashSet<>();
            List<Product> selected = new ArrayList<>();
            List<OrderItemRequest> lines = request.items().stream()
                    .sorted(Comparator.comparing(OrderItemRequest::productId)).toList();

            // Se valida el pedido completo antes de cambiar cualquier stock.
            for (OrderItemRequest line : lines) {
                if (!seen.add(line.productId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No repitas productos en el pedido");
                }
                Product product = products.findById(line.productId()).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
                if (product.getStock() < line.quantity()) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "Stock insuficiente para " + product.getName());
                }
                selected.add(product);
                order.getItems().add(new OrderItem(product, line.quantity()));
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
            }

            for (int i = 0; i < selected.size(); i++) {
                Product product = selected.get(i);
                product.setStock(product.getStock() - lines.get(i).quantity());
            }
            order.setTotal(total);
            return orders.save(order);
        }
    }

    public List<PurchaseOrder> findAll(Long userId, String role) {
        return "employee".equals(role) ? orders.findAll() : orders.findByUserIdOrderByIdDesc(userId);
    }

    public PurchaseOrder findById(Long id, Long userId, String role) {
        PurchaseOrder order = findOrder(id);
        if (!"employee".equals(role) && !order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
        }
        return order;
    }

    public PurchaseOrder updateStatus(Long id, String status) {
        synchronized (orders) {
            PurchaseOrder order = findOrder(id);
            order.setStatus(status);
            return orders.save(order);
        }
    }

    private PurchaseOrder findOrder(Long id) {
        return orders.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }
}
