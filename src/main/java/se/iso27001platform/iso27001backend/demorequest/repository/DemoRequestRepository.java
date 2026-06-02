package se.iso27001platform.iso27001backend.demorequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.demorequest.model.DemoRequest;

import java.util.UUID;

public interface DemoRequestRepository extends JpaRepository<DemoRequest, UUID> {
}
