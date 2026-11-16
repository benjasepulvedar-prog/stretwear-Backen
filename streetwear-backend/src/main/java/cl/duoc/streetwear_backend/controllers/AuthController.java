package cl.duoc.streetwear_backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.streetwear_backend.dto.AuthResponse;
import cl.duoc.streetwear_backend.dto.LoginRequest;
import cl.duoc.streetwear_backend.dto.UserRequest;
import cl.duoc.streetwear_backend.dto.UserResponse;
import cl.duoc.streetwear_backend.security.JwtService;
import cl.duoc.streetwear_backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una cuenta")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Verificar las credenciales y obtener un JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return jwtService.createToken(userService.authenticate(request));
    }
}
