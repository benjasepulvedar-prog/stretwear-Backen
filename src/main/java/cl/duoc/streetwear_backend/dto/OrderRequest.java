package cl.duoc.streetwear_backend.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @NotBlank @Size(max = 80) @Schema(example = "Benjamín") String name,
        @NotBlank @Size(max = 200) @Schema(example = "Av. Central 123, Santiago") String address,
        @NotBlank @Pattern(regexp = "normal|express") @Schema(example = "normal") String delivery,
        @NotEmpty @Size(max = 50) List<@Valid OrderItemRequest> items) {
}
