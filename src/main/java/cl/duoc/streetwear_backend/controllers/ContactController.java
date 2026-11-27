package cl.duoc.streetwear_backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.streetwear_backend.dto.ContactRequest;
import cl.duoc.streetwear_backend.models.ContactMessage;
import cl.duoc.streetwear_backend.repositories.ContactMessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contact")
@Tag(name = "Contacto")
public class ContactController {

    private final ContactMessageRepository messages;

    public ContactController(ContactMessageRepository messages) {
        this.messages = messages;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar un mensaje de contacto")
    public void send(@Valid @RequestBody ContactRequest request) {
        messages.save(new ContactMessage(request.name().trim(), request.email(), request.message().trim()));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar mensajes de contacto (administrador)")
    public List<ContactMessage> list() {
        return messages.findAllByOrderByIdDesc();
    }
}
