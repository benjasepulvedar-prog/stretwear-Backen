package cl.duoc.streetwear_backend.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import cl.duoc.streetwear_backend.dto.OrderRequest;
import cl.duoc.streetwear_backend.dto.OrderStatusRequest;
import cl.duoc.streetwear_backend.models.PurchaseOrder;
import cl.duoc.streetwear_backend.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Compras y pedidos")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una compra (cliente)")
    public PurchaseOrder create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OrderRequest request) {
        return service.create(Long.valueOf(jwt.getSubject()), request);
    }

    @GetMapping
    @Operation(summary = "Consultar mis pedidos (cliente) o todos los pedidos (empleado)")
    public List<PurchaseOrder> list(@AuthenticationPrincipal Jwt jwt) {
        return service.findAll(Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un pedido propio (cliente) o recibido (empleado)")
    public PurchaseOrder get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.findById(id, Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar el estado de un pedido (empleado)")
    public PurchaseOrder updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return service.updateStatus(id, request.status());
    }
}
