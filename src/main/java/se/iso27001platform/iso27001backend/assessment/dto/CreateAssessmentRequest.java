package se.iso27001platform.iso27001backend.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAssessmentRequest(
		@NotNull UUID organizationId,
		@NotBlank String name
) {
}
