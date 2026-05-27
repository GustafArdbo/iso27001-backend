package se.iso27001platform.iso27001backend.assessment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.assessment.model.Assessment;

import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
}
