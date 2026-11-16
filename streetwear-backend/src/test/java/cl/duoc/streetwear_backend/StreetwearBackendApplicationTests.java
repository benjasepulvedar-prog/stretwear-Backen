package cl.duoc.streetwear_backend;

import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class StreetwearBackendApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsWithoutStartingJpaOrADatabase() {
        assertTrue(context.getBeansOfType(DataSource.class).isEmpty());
        assertTrue(context.getBeansOfType(EntityManagerFactory.class).isEmpty());
    }
}
