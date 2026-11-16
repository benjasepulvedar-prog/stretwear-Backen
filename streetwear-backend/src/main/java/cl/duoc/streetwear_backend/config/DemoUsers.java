package cl.duoc.streetwear_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import cl.duoc.streetwear_backend.models.Role;
import cl.duoc.streetwear_backend.models.User;
import cl.duoc.streetwear_backend.repositories.UserRepository;

@Component
@Profile("demo")
public class DemoUsers implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DemoUsers(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        create("Administrador", "admin@caos.cl", Role.ADMIN);
        create("Empleado", "empleado@caos.cl", Role.EMPLOYEE);
        create("Cliente", "cliente@caos.cl", Role.CUSTOMER);
    }

    private void create(String name, String email, Role role) {
        if (!users.existsByEmail(email)) {
            User user = new User(name, email, encoder.encode("CAOS2026!"));
            user.setRole(role);
            users.save(user);
        }
    }
}
