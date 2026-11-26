package cl.duoc.streetwear_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;

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

import cl.duoc.streetwear_backend.repositories.UserRepository;
import cl.duoc.streetwear_backend.security.JwtService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.application.name=users-api-test")
class UserApiTests {

    private static final String PASSWORD = "MiClave123!";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder encoder;

    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void clearTestUsers() {
        repository.deleteAll();
    }

    @Test
    void registrationStoresAHashAndNeverReturnsThePassword() throws Exception {
        HttpResponse<String> response = register("BENJAMIN@example.com");
        assertEquals(201, response.statusCode(), response.body());
        JsonNode user = json(response);
        assertEquals("benjamin@example.com", user.path("email").asText());
        assertFalse(user.has("password"));
        assertFalse(user.has("passwordHash"));
        var stored = repository.findByEmail("benjamin@example.com").orElseThrow();
        assertNotEquals(PASSWORD, stored.getPasswordHash());
        assertTrue(passwordEncoder.matches(PASSWORD, stored.getPasswordHash()));
    }

    @Test
    void rejectsDuplicateEmailAndInvalidData() throws Exception {
        assertEquals(201, register("benjamin@example.com").statusCode());
        assertEquals(409, register("BENJAMIN@example.com").statusCode());
        var invalid = send("POST", "/api/auth/register",
                Map.of("name", "", "email", "no-es-correo", "password", "123"), null);
        assertEquals(400, invalid.statusCode(), invalid.body());
        assertTrue(json(invalid).has("errors"));
        assertEquals(1, repository.count());
    }

    @Test
    void rejectsPasswordsBeyondTheBcryptByteLimit() throws Exception {
        var response = send("POST", "/api/auth/register",
                Map.of("name", "Prueba", "email", "prueba@example.com", "password", "á".repeat(40)), null);
        assertEquals(400, response.statusCode(), response.body());
        assertEquals(0, repository.count());
    }

    @Test
    void loginVerifiesCredentialsAndProtectedRoutesRequireJwt() throws Exception {
        register("benjamin@example.com");
        var incorrect = send("POST", "/api/auth/login",
                Map.of("email", "benjamin@example.com", "password", "incorrecta"), null);
        var missing = send("POST", "/api/auth/login",
                Map.of("email", "otra@example.com", "password", PASSWORD), null);
        assertEquals(401, incorrect.statusCode(), incorrect.body());
        assertEquals(401, missing.statusCode(), missing.body());
        assertEquals(401, send("GET", "/api/users/me", null, null).statusCode());
        assertEquals(401, send("PUT", "/api/users/me", userData("benjamin@example.com", PASSWORD), null).statusCode());
        assertEquals(401, send("DELETE", "/api/users/me", null, null).statusCode());

        String token = login("benjamin@example.com", PASSWORD);
        assertEquals(200, send("GET", "/api/users/me", null, token).statusCode());
        String[] parts = token.split("\\.");
        parts[2] = (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        assertEquals(401, send("GET", "/api/users/me", null, String.join(".", parts)).statusCode());
        assertEquals(401, send("GET", "/api/users/me", null, "token-invalido").statusCode());
    }

    @Test
    void rejectsExpiredTokensWrongIssuerAndMissingExpiration() throws Exception {
        String id = json(register("benjamin@example.com")).path("id").asText();
        assertEquals(401, send("GET", "/api/users/me", null,
                signedToken(id, JwtService.ISSUER, Instant.now().minusSeconds(600))).statusCode());
        assertEquals(401, send("GET", "/api/users/me", null,
                signedToken(id, "otra-aplicacion", Instant.now().plusSeconds(3600))).statusCode());
        assertEquals(401, send("GET", "/api/users/me", null,
                signedToken(id, JwtService.ISSUER, null)).statusCode());
    }

    @Test
    void accountCanBeReadUpdatedAndDeleted() throws Exception {
        register("benjamin@example.com");
        String token = login("benjamin@example.com", PASSWORD);
        var updated = send("PUT", "/api/users/me",
                Map.of("name", "Benjamín actualizado", "email", "nuevo@example.com", "password", "NuevaClave123!"), token);
        assertEquals(200, updated.statusCode(), updated.body());
        assertEquals("nuevo@example.com", json(updated).path("email").asText());
        assertFalse(json(updated).has("passwordHash"));
        assertEquals(401, send("POST", "/api/auth/login",
                Map.of("email", "nuevo@example.com", "password", PASSWORD), null).statusCode());

        String newToken = login("nuevo@example.com", "NuevaClave123!");
        assertEquals("Benjamín actualizado",
                json(send("GET", "/api/users/me", null, newToken)).path("name").asText());
        assertEquals(204, send("DELETE", "/api/users/me", null, newToken).statusCode());
        assertEquals(0, repository.count());
        assertEquals(401, send("GET", "/api/users/me", null, newToken).statusCode());
        assertEquals(401, send("POST", "/api/auth/login",
                Map.of("email", "nuevo@example.com", "password", "NuevaClave123!"), null).statusCode());
    }

    @Test
    void usersCannotManageOtherAccountsOrTakeTheirEmail() throws Exception {
        register("uno@example.com");
        long otherId = json(register("dos@example.com")).path("id").asLong();
        String token = login("uno@example.com", PASSWORD);
        assertEquals("uno@example.com", json(send("GET", "/api/users/me", null, token)).path("email").asText());
        assertEquals(409, send("PUT", "/api/users/me", userData("dos@example.com", PASSWORD), token).statusCode());
        for (String method : new String[] {"GET", "PUT", "DELETE"}) {
            assertEquals(404, send(method, "/api/users/" + otherId,
                    method.equals("PUT") ? userData("cambio@example.com", PASSWORD) : null, token).statusCode());
        }
        assertEquals(404, send("GET", "/api/users", null, token).statusCode());
        assertEquals(2, repository.count());
        assertEquals("dos@example.com", repository.findById(otherId).orElseThrow().getEmail());
    }

    @Test
    void swaggerDocumentsBearerAuthenticationAndThePublicExampleStillWorks() throws Exception {
        assertEquals(200, send("GET", "/swagger-ui/index.html", null, null).statusCode());
        var definition = send("GET", "/v3/api-docs", null, null);
        assertEquals(200, definition.statusCode(), definition.body());
        JsonNode openapi = json(definition);
        assertEquals("bearer", openapi.path("components").path("securitySchemes").path("bearerAuth").path("scheme").asText());
        assertTrue(openapi.path("paths").path("/api/users/me").path("get").path("security").get(0).has("bearerAuth"));
        assertTrue(openapi.path("paths").path("/api/auth/register").has("post"));
        assertTrue(json(send("GET", "/products", null, null)).isArray());
    }

    private Map<String, String> userData(String email, String password) {
        return Map.of("name", "Prueba", "email", email, "password", password);
    }

    private HttpResponse<String> register(String email) throws Exception {
        return send("POST", "/api/auth/register", userData(email, PASSWORD), null);
    }

    private String login(String email, String password) throws Exception {
        var response = send("POST", "/api/auth/login", Map.of("email", email, "password", password), null);
        assertEquals(200, response.statusCode(), response.body());
        assertEquals("Bearer", json(response).path("tokenType").asText());
        assertEquals(3600, json(response).path("expiresIn").asLong());
        return json(response).path("token").asText();
    }

    private HttpResponse<String> send(String method, String path, Map<String, String> body, String token)
            throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Accept", "application/json");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode json(HttpResponse<String> response) {
        return mapper.readTree(response.body());
    }

    private String signedToken(String subject, String issuer, Instant expiration) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(subject).issuer(issuer).claim("role", "customer").issuedAt(Instant.now().minusSeconds(3600));
        if (expiration != null) {
            claims.expiresAt(expiration);
        }
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
    }
}
