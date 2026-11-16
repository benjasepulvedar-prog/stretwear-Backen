package cl.duoc.streetwear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OrderStatusRequest(
        @NotBlank @Pattern(regexp = "Pendiente|Preparando|Enviado|Entregado")
        @Schema(example = "Preparando") String status) {
}
