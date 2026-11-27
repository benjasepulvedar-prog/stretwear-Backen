package cl.duoc.streetwear_backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.streetwear_backend.repositories.UserRepository;
import cl.duoc.streetwear_backend.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class ContactApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private JwtService jwtService;

    @Test
    void acceptsPublicMessagesAndOnlyAdminsCanReadThem() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Cliente CAOS","email":"contacto@example.com","message":"Consulta por un producto"}
                """)).andExpect(status().isCreated());

        mvc.perform(get("/api/contact")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/contact").header(HttpHeaders.AUTHORIZATION, bearerFor("cliente@caos.cl")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/contact").header(HttpHeaders.AUTHORIZATION, bearerFor("admin@caos.cl")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cliente CAOS"))
                .andExpect(jsonPath("$[0].email").value("contacto@example.com"))
                .andExpect(jsonPath("$[0].message").value("Consulta por un producto"));
    }

    @Test
    void rejectsInvalidMessages() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Cliente CAOS","email":"correo-invalido","message":" "}
                """)).andExpect(status().isBadRequest());
    }

    private String bearerFor(String email) {
        return "Bearer " + jwtService.createToken(users.findByEmail(email).orElseThrow()).token();
    }
}
