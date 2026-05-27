package se.iso27001platform.iso27001backend.assessment.dto;

import se.iso27001platform.iso27001backend.assessment.enums.AnswerStatus;
import se.iso27001platform.iso27001backend.assessment.model.AssessmentAnswer;
import se.iso27001platform.iso27001backend.control.enums.ControlDomain;
import se.iso27001platform.iso27001backend.control.model.ControlDefinition;

import java.time.Instant;
import java.util.UUID;

public record AssessmentQuestionResponse(
		String controlId,
		ControlDomain domain,
		String title,
		String question,
		int sortOrder,
		boolean answered,
		UUID answerId,
		AnswerStatus answer,
		String comment,
		Instant answeredAt
) {

	public static AssessmentQuestionResponse from(ControlDefinition controlDefinition, AssessmentAnswer assessmentAnswer) {
		return new AssessmentQuestionResponse(
				controlDefinition.id(),
				controlDefinition.domain(),
				controlDefinition.title(),
				controlDefinition.question(),
				controlDefinition.sortOrder(),
				assessmentAnswer != null,
				assessmentAnswer == null ? null : assessmentAnswer.getId(),
				assessmentAnswer == null ? null : assessmentAnswer.getAnswer(),
				assessmentAnswer == null ? null : assessmentAnswer.getComment(),
				assessmentAnswer == null ? null : assessmentAnswer.getCreatedAt()
		);
	}
}
