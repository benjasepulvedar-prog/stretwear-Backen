package cl.duoc.streetwear_backend.services;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.streetwear_backend.dto.LoginRequest;
import cl.duoc.streetwear_backend.dto.UserRequest;
import cl.duoc.streetwear_backend.dto.UserResponse;
import cl.duoc.streetwear_backend.models.User;
import cl.duoc.streetwear_backend.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public synchronized UserResponse register(UserRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        User user = new User(request.name().trim(), email, hashPassword(request.password()));
        return UserResponse.from(repository.save(user));
    }

    public synchronized User authenticate(LoginRequest request) {
        User user = repository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        return user;
    }

    public synchronized UserResponse getCurrentUser(Long id) {
        return UserResponse.from(findCurrentUser(id));
    }

    public synchronized UserResponse updateCurrentUser(Long id, UserRequest request) {
        User user = findCurrentUser(id);
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(hashPassword(request.password()));
        return UserResponse.from(repository.save(user));
    }

    public synchronized void deleteCurrentUser(Long id) {
        repository.delete(findCurrentUser(id));
    }

    private User findCurrentUser(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "La cuenta ya no existe. Inicia sesión nuevamente"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashPassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La contraseña admite hasta 72 bytes en UTF-8");
        }
        return passwordEncoder.encode(password);
    }
}
