package se.iso27001platform.iso27001backend.assessment.dto;

import se.iso27001platform.iso27001backend.assessment.enums.AnswerStatus;
import se.iso27001platform.iso27001backend.assessment.enums.AssessmentStatus;

import java.util.Map;
import java.util.UUID;

public record AssessmentSummaryResponse(
		UUID id,
		UUID organizationId,
		String name,
		AssessmentStatus status,
		int totalControls,
		int answeredControls,
		int unansweredControls,
		int completionPercentage,
		int totalAnswers,
		int applicableAnswers,
		double score,
		int scorePercentage,
		int gapPercentage,
		Map<AnswerStatus, Long> answerCounts
) {
}
