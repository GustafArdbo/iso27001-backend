package se.iso27001platform.iso27001backend.onboarding.dto;

import se.iso27001platform.iso27001backend.onboarding.enums.ApplicationStatus;
import se.iso27001platform.iso27001backend.onboarding.model.OrganizationApplication;

import java.time.Instant;
import java.util.UUID;

public record SubmittedOrganizationApplicationResponse(
		UUID id,
		ApplicationStatus status,
		Instant createdAt
) {

	public static SubmittedOrganizationApplicationResponse from(OrganizationApplication application) {
		return new SubmittedOrganizationApplicationResponse(
				application.getId(),
				application.getApplicationStatus(),
				application.getCreatedAt()
		);
	}
}
