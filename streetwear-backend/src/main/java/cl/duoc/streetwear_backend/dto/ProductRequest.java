package cl.duoc.streetwear_backend.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 100) @Schema(example = "Polera Thrasher Black") String name,
        @NotBlank @Size(max = 50) @Schema(example = "Ropa") String category,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
        @Schema(example = "24990") BigDecimal price,
        @NotNull @Min(0) @Max(1000000) @Schema(example = "10") Integer stock,
        @NotBlank @Size(max = 500)
        @Schema(example = "/img/productos/polera-thrasher.webp") String image,
        @Schema(example = "true") boolean featured) {
}
