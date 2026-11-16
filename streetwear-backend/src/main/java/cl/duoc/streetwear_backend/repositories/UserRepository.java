package cl.duoc.streetwear_backend.repositories;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.streetwear_backend.models.User;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new LinkedHashMap<>();
    private long nextId = 1;

    public synchronized Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public synchronized Optional<User> findByEmail(String email) {
        return users.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    public synchronized boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public synchronized boolean existsByEmailAndIdNot(String email, Long id) {
        return users.values().stream().anyMatch(
                user -> user.getEmail().equals(email) && !Objects.equals(user.getId(), id));
    }

    public synchronized User save(User user) {
        if (existsByEmailAndIdNot(user.getEmail(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    public synchronized void delete(User user) {
        users.remove(user.getId());
    }

    public synchronized void deleteById(Long id) {
        users.remove(id);
    }

    public synchronized long count() {
        return users.size();
    }

    public synchronized void deleteAll() {
        users.clear();
    }
}
