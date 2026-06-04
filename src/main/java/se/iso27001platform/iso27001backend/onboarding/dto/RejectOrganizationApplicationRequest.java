package se.iso27001platform.iso27001backend.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectOrganizationApplicationRequest(
		@NotBlank @Size(max = 2000) String reason
) {

	public RejectOrganizationApplicationRequest {
		if (reason != null) {
			reason = reason.trim();
		}
	}
}
