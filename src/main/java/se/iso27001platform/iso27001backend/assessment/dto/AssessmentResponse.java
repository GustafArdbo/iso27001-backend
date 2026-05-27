package se.iso27001platform.iso27001backend.assessment.dto;

import se.iso27001platform.iso27001backend.assessment.enums.AssessmentStatus;
import se.iso27001platform.iso27001backend.assessment.model.Assessment;

import java.time.Instant;
import java.util.UUID;

public record AssessmentResponse(
		UUID id,
		UUID organizationId,
		String name,
		AssessmentStatus status,
		Instant createdAt
) {

	public static AssessmentResponse from(Assessment assessment) {
		return new AssessmentResponse(
				assessment.getId(),
				assessment.getOrganization().getId(),
				assessment.getName(),
				assessment.getStatus(),
				assessment.getCreatedAt()
		);
	}
}
