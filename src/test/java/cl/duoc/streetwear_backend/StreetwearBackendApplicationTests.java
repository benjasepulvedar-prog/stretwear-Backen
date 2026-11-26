package cl.duoc.streetwear_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import cl.duoc.streetwear_backend.repositories.ProductRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:caos-migrations;MODE=PostgreSQL")
class StreetwearBackendApplicationTests {

    @Autowired
    private ProductRepository products;

    @Test
    void flywayCreatesAndSeedsTheCatalog() {
        assertEquals(11, products.count());
    }
}
