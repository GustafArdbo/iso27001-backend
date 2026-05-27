package se.iso27001platform.iso27001backend.organization.dto;

import se.iso27001platform.iso27001backend.organization.model.Organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
		UUID id,
		String name,
		Instant createdAt
) {

	public static OrganizationResponse from(Organization organization) {
		return new OrganizationResponse(
				organization.getId(),
				organization.getName(),
				organization.getCreatedAt()
		);
	}
}
