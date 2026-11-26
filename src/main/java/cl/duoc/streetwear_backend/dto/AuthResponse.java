package cl.duoc.streetwear_backend.dto;

public record AuthResponse(String token, String tokenType, long expiresIn, String role) {
}
