package se.iso27001platform.iso27001backend.assessment.dto;

import se.iso27001platform.iso27001backend.assessment.enums.AnswerStatus;
import se.iso27001platform.iso27001backend.assessment.model.AssessmentAnswer;

import java.time.Instant;
import java.util.UUID;

public record AssessmentAnswerResponse(
		UUID id,
		UUID assessmentId,
		String controlId,
		AnswerStatus answer,
		String comment,
		Instant createdAt
) {

	public static AssessmentAnswerResponse from(AssessmentAnswer assessmentAnswer) {
		return new AssessmentAnswerResponse(
				assessmentAnswer.getId(),
				assessmentAnswer.getAssessment().getId(),
				assessmentAnswer.getControlId(),
				assessmentAnswer.getAnswer(),
				assessmentAnswer.getComment(),
				assessmentAnswer.getCreatedAt()
		);
	}
}
