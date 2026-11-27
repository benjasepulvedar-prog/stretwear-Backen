package cl.duoc.streetwear_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.streetwear_backend.models.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findAllByOrderByIdDesc();
}
