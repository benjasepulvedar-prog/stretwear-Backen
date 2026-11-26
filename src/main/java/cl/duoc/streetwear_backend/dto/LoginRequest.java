package cl.duoc.streetwear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe ser válido")
        @Size(max = 254, message = "El correo admite hasta 254 caracteres")
        @Schema(example = "benjamin@example.com")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72, message = "La contraseña admite hasta 72 caracteres")
        @Schema(example = "MiClave123!", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        String password) {
}
