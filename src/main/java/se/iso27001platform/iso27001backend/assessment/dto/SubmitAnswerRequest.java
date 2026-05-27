package se.iso27001platform.iso27001backend.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.iso27001platform.iso27001backend.assessment.enums.AnswerStatus;

public record SubmitAnswerRequest(
		@NotBlank String controlId,
		@NotNull AnswerStatus answer,
		String comment
) {
}
