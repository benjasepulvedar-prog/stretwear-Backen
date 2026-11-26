package cl.duoc.streetwear_backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.streetwear_backend.dto.UserRequest;
import cl.duoc.streetwear_backend.dto.UserResponse;
import cl.duoc.streetwear_backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Mi cuenta")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Consultar mi cuenta y comprobar mi JWT")
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        return service.getCurrentUser(Long.valueOf(jwt.getSubject()));
    }

    @PutMapping
    @Operation(summary = "Actualizar mi nombre, correo y contraseña")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserRequest request) {
        return service.updateCurrentUser(Long.valueOf(jwt.getSubject()), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar mi propia cuenta")
    public void deleteMe(@AuthenticationPrincipal Jwt jwt) {
        service.deleteCurrentUser(Long.valueOf(jwt.getSubject()));
    }
}
