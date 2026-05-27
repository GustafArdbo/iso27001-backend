package se.iso27001platform.iso27001backend.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganizationRequest(
		@NotBlank String name
) {
}
