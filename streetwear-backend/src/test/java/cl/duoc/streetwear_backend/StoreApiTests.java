package cl.duoc.streetwear_backend;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import cl.duoc.streetwear_backend.models.Role;
import cl.duoc.streetwear_backend.models.User;
import cl.duoc.streetwear_backend.repositories.OrderRepository;
import cl.duoc.streetwear_backend.repositories.ProductRepository;
import cl.duoc.streetwear_backend.repositories.UserRepository;
import cl.duoc.streetwear_backend.security.JwtService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.application.name=store-api-test")
class StoreApiTests {

    private static final String PASSWORD = "ClavePrueba123!";

    @LocalServerPort
    private int port;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserRepository users;
    @Autowired
    private ProductRepository products;
    @Autowired
    private OrderRepository orders;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtEncoder jwtEncoder;

    private final HttpClient client = HttpClient.newHttpClient();
    private String admin;
    private String employee;
    private String customer;
    private String otherCustomer;
    private Long adminId;
    private Long customerId;

    @BeforeEach
    void prepareUsers() throws Exception {
        orders.deleteAll();
        products.deleteAll();
        users.deleteAll();
        adminId = createUser("admin@example.com", Role.ADMIN).getId();
        createUser("employee@example.com", Role.EMPLOYEE);
        customerId = createUser("customer@example.com", Role.CUSTOMER).getId();
        createUser("other@example.com", Role.CUSTOMER);
        admin = login("admin@example.com", "admin");
        employee = login("employee@example.com", "employee");
        customer = login("customer@example.com", "customer");
        otherCustomer = login("other@example.com", "customer");
    }

    @Test
    void publicRegistrationAndProfileUpdateCannotGrantAdminRole() throws Exception {
        var response = send("POST", "/api/auth/register", Map.of(
                "name", "Nuevo", "email", "nuevo@example.com", "password", PASSWORD, "role", "admin"), null);
        assertEquals(201, response.statusCode(), response.body());
        assertEquals("customer", json(response).path("role").asText());
        String token = login("nuevo@example.com", "customer");
        var update = send("PUT", "/api/users/me", Map.of(
                "name", "Nuevo", "email", "nuevo@example.com", "password", PASSWORD, "role", "admin"), token);
        assertEquals(200, update.statusCode(), update.body());
        assertEquals("customer", json(update).path("role").asText());
        assertEquals(Role.CUSTOMER, users.findByEmail("nuevo@example.com").orElseThrow().getRole());
    }

    @Test
    void productCrudWorksAndOnlyAdminCanWrite() throws Exception {
        assertEquals(401, send("POST", "/products", productData("Polera", 5), null).statusCode());
        assertEquals(403, send("POST", "/products", productData("Polera", 5), customer).statusCode());
        assertEquals(403, send("POST", "/products", productData("Polera", 5), employee).statusCode());
        long id = createProduct("Polera", 5);
        assertEquals(200, send("GET", "/products/" + id, null, null).statusCode());
        assertEquals(1, json(send("GET", "/products", null, null)).size());
        assertEquals(403, send("PUT", "/products/" + id, productData("Cambio", 3), customer).statusCode());
        assertEquals(403, send("DELETE", "/products/" + id, null, employee).statusCode());
        var updated = send("PUT", "/products/" + id, productData("Actualizada", 3), admin);
        assertEquals(200, updated.statusCode(), updated.body());
        assertEquals("Actualizada", json(updated).path("name").asText());
        assertEquals(204, send("DELETE", "/products/" + id, null, admin).statusCode());
        assertEquals(404, send("GET", "/products/" + id, null, null).statusCode());
        assertEquals(404, send("DELETE", "/products/" + id, null, admin).statusCode());
    }

    @Test
    void productValidationRejectsInvalidPricesStockAndMissingFields() throws Exception {
        var badPrice = Map.of("name", "Polera", "category", "Ropa", "price", -10,
                "stock", 1, "image", "/img/polera.webp", "featured", false);
        assertEquals(400, send("POST", "/products", badPrice, admin).statusCode());
        assertEquals(400, send("POST", "/products", productData("Polera", -1), admin).statusCode());
        assertEquals(400, send("POST", "/products", Map.of("name", "Incompleto"), admin).statusCode());
        assertEquals(0, products.count());
    }

    @Test
    void purchaseCalculatesPricesAndEmployeeManagesOrders() throws Exception {
        long productId = createProduct("Polera", 5);
        var body = Map.of("name", "Cliente", "address", "Calle 123", "delivery", "normal",
                "total", 1, "userId", adminId,
                "items", List.of(Map.of("productId", productId, "quantity", 2, "price", 1)));
        var created = send("POST", "/api/orders", body, customer);
        assertEquals(201, created.statusCode(), created.body());
        JsonNode order = json(created);
        long orderId = order.path("id").asLong();
        assertEquals(customerId.longValue(), order.path("userId").asLong());
        assertEquals(0, order.path("total").decimalValue().compareTo(new BigDecimal("49980")));
        assertEquals("customer@example.com", order.path("email").asText());
        assertEquals(3, products.findById(productId).orElseThrow().getStock());
        assertEquals(1, json(send("GET", "/api/orders", null, customer)).size());
        assertEquals(0, json(send("GET", "/api/orders", null, otherCustomer)).size());
        assertEquals(404, send("GET", "/api/orders/" + orderId, null, otherCustomer).statusCode());
        assertEquals(1, json(send("GET", "/api/orders", null, employee)).size());
        assertEquals(200, send("GET", "/api/orders/" + orderId, null, employee).statusCode());
        assertEquals(403, send("GET", "/api/orders", null, admin).statusCode());
        assertEquals(403, send("PATCH", "/api/orders/" + orderId + "/status",
                Map.of("status", "Enviado"), customer).statusCode());
        var updated = send("PATCH", "/api/orders/" + orderId + "/status",
                Map.of("status", "Preparando"), employee);
        assertEquals(200, updated.statusCode(), updated.body());
        assertEquals("Preparando", json(send("GET", "/api/orders/" + orderId, null, customer)).path("status").asText());
        assertEquals(400, send("PATCH", "/api/orders/" + orderId + "/status",
                Map.of("status", "Estado inventado"), employee).statusCode());
    }

    @Test
    void purchaseRequiresCustomerRoleAndValidItems() throws Exception {
        long id = createProduct("Polera", 3);
        assertEquals(401, send("POST", "/api/orders", orderData(id, 1), null).statusCode());
        assertEquals(403, send("POST", "/api/orders", orderData(id, 1), admin).statusCode());
        assertEquals(403, send("POST", "/api/orders", orderData(id, 1), employee).statusCode());
        assertEquals(400, send("POST", "/api/orders", orderData(id, 0), customer).statusCode());
        assertEquals(400, send("POST", "/api/orders", Map.of(
                "name", "Cliente", "address", "Calle 123", "delivery", "normal", "items", List.of()), customer).statusCode());
        assertEquals(404, send("POST", "/api/orders", orderData(999999L, 1), customer).statusCode());
        assertEquals(0, orders.count());
        assertEquals(3, products.findById(id).orElseThrow().getStock());
    }

    @Test
    void failedPurchaseDoesNotChangeStockAndRejectsDuplicateProducts() throws Exception {
        long first = createProduct("Disponible", 5);
        long second = createProduct("Agotado", 0);
        var insufficient = Map.of("name", "Cliente", "address", "Calle 123", "delivery", "normal",
                "items", List.of(Map.of("productId", first, "quantity", 2),
                        Map.of("productId", second, "quantity", 1)));
        assertEquals(409, send("POST", "/api/orders", insufficient, customer).statusCode());
        assertEquals(5, products.findById(first).orElseThrow().getStock());
        var duplicated = Map.of("name", "Cliente", "address", "Calle 123", "delivery", "normal",
                "items", List.of(Map.of("productId", first, "quantity", 1),
                        Map.of("productId", first, "quantity", 1)));
        assertEquals(400, send("POST", "/api/orders", duplicated, customer).statusCode());
        assertEquals(5, products.findById(first).orElseThrow().getStock());
        assertEquals(0, orders.count());
    }

    @Test
    void twoPurchasesCannotSellTheSameLastUnit() throws Exception {
        long id = createProduct("Última unidad", 1);
        var one = client.sendAsync(request("POST", "/api/orders", orderData(id, 1), customer),
                HttpResponse.BodyHandlers.ofString());
        var two = client.sendAsync(request("POST", "/api/orders", orderData(id, 1), otherCustomer),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(Set.of(201, 409), Set.of(one.join().statusCode(), two.join().statusCode()));
        assertEquals(0, products.findById(id).orElseThrow().getStock());
        assertEquals(1, orders.count());
    }

    @Test
    void ordersKeepTheirOriginalProductsAndPrices() throws Exception {
        long id = createProduct("Nombre original", 5);
        var created = send("POST", "/api/orders", orderData(id, 2), customer);
        assertEquals(201, created.statusCode(), created.body());
        long orderId = json(created).path("id").asLong();
        var changed = Map.of("name", "Nombre nuevo", "category", "Ropa", "price", 999,
                "stock", 3, "image", "/img/nueva.webp", "featured", false);
        assertEquals(200, send("PUT", "/products/" + id, changed, admin).statusCode());
        assertEquals(204, send("DELETE", "/products/" + id, null, admin).statusCode());
        JsonNode historical = json(send("GET", "/api/orders/" + orderId, null, customer));
        assertEquals("Nombre original", historical.path("items").get(0).path("name").asText());
        assertEquals(0, historical.path("total").decimalValue().compareTo(new BigDecimal("49980")));
    }

    @Test
    void jwtRoleMustMatchTheAccountAndDeletedAccountsLoseAccess() throws Exception {
        Instant now = Instant.now();
        var forgedClaims = JwtClaimsSet.builder().issuer(JwtService.ISSUER).subject(customerId.toString())
                .claim("role", "admin").issuedAt(now).expiresAt(now.plusSeconds(3600)).build();
        String wrongRole = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), forgedClaims)).getTokenValue();
        assertEquals(401, send("POST", "/products", productData("Polera", 5), wrongRole).statusCode());
        User changed = users.findById(adminId).orElseThrow();
        changed.setRole(Role.CUSTOMER);
        users.save(changed);
        assertEquals(401, send("POST", "/products", productData("Polera", 5), admin).statusCode());
        users.deleteById(customerId);
        assertEquals(401, send("GET", "/api/orders", null, customer).statusCode());
    }

    @Test
    void swaggerIncludesTheNewProductAndOrderOperations() throws Exception {
        JsonNode paths = json(send("GET", "/v3/api-docs", null, null)).path("paths");
        assertTrue(paths.path("/products").has("post"));
        assertTrue(paths.path("/products/{id}").has("get"));
        assertTrue(paths.path("/products/{id}").has("put"));
        assertTrue(paths.path("/products/{id}").has("delete"));
        assertTrue(paths.path("/api/orders").has("post"));
        assertTrue(paths.path("/api/orders/{id}/status").has("patch"));
    }

    private User createUser(String email, Role role) {
        User user = new User("Prueba", email, encoder.encode(PASSWORD));
        user.setRole(role);
        return users.save(user);
    }

    private String login(String email, String role) throws Exception {
        var response = send("POST", "/api/auth/login", Map.of("email", email, "password", PASSWORD), null);
        assertEquals(200, response.statusCode(), response.body());
        assertEquals(role, json(response).path("role").asText());
        return json(response).path("token").asText();
    }

    private Map<String, Object> productData(String name, int stock) {
        return Map.of("name", name, "category", "Ropa", "price", 24990,
                "stock", stock, "image", "/img/productos/polera-thrasher.webp", "featured", true);
    }

    private long createProduct(String name, int stock) throws Exception {
        var response = send("POST", "/products", productData(name, stock), admin);
        assertEquals(201, response.statusCode(), response.body());
        return json(response).path("id").asLong();
    }

    private Map<String, Object> orderData(long id, int quantity) {
        return Map.of("name", "Cliente", "address", "Calle 123", "delivery", "normal",
                "items", List.of(Map.of("productId", id, "quantity", quantity)));
    }

    private HttpRequest request(String method, String path, Object body, String token) {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(java.time.Duration.ofSeconds(20)).header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        }
        return builder.build();
    }

    private HttpResponse<String> send(String method, String path, Object body, String token) throws Exception {
        return client.send(request(method, path, body, token), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode json(HttpResponse<String> response) {
        return mapper.readTree(response.body());
    }
}
