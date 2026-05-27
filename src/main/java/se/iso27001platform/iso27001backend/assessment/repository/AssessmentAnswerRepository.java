package se.iso27001platform.iso27001backend.assessment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.assessment.model.AssessmentAnswer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, UUID> {

	Optional<AssessmentAnswer> findByAssessment_IdAndControlId(UUID assessmentId, String controlId);

	List<AssessmentAnswer> findByAssessment_Id(UUID assessmentId);
}
