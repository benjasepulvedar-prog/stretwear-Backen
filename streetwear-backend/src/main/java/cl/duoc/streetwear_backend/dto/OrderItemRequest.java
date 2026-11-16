package cl.duoc.streetwear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull @Positive @Schema(example = "1") Long productId,
        @NotNull @Min(1) @Max(1000) @Schema(example = "2") Integer quantity) {
}
