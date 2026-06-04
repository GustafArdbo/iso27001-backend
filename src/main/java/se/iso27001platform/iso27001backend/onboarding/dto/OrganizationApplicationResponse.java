package se.iso27001platform.iso27001backend.onboarding.dto;

import se.iso27001platform.iso27001backend.onboarding.enums.ApplicationStatus;
import se.iso27001platform.iso27001backend.onboarding.enums.OwnerInvitationStatus;
import se.iso27001platform.iso27001backend.onboarding.enums.RequestedMaterial;
import se.iso27001platform.iso27001backend.onboarding.model.OrganizationApplication;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrganizationApplicationResponse(
		UUID id,
		String company,
		String ownerName,
		String ownerEmail,
		String country,
		String phone,
		String size,
		String message,
		List<String> materials,
		ApplicationStatus applicationStatus,
		OwnerInvitationStatus invitationStatus,
		UUID organizationId,
		UUID ownerProfileId,
		UUID approvedBySupabaseUserId,
		Instant approvedAt,
		UUID rejectedBySupabaseUserId,
		Instant rejectedAt,
		String rejectionReason,
		Instant invitationSentAt,
		Instant invitationAcceptedAt,
		String invitationFailureReason,
		Instant createdAt
) {

	public static OrganizationApplicationResponse from(OrganizationApplication application) {
		return new OrganizationApplicationResponse(
				application.getId(),
				application.getCompanyName(),
				application.getOwnerName(),
				application.getOwnerEmail(),
				application.getCountry(),
				application.getPhone(),
				application.getCompanySize(),
				application.getMessage(),
				application.getMaterials().stream()
						.map(RequestedMaterial::getClientValue)
						.sorted()
						.toList(),
				application.getApplicationStatus(),
				application.getInvitationStatus(),
				application.getOrganization() == null ? null : application.getOrganization().getId(),
				application.getOwnerProfile() == null ? null : application.getOwnerProfile().getId(),
				application.getApprovedBySupabaseUserId(),
				application.getApprovedAt(),
				application.getRejectedBySupabaseUserId(),
				application.getRejectedAt(),
				application.getRejectionReason(),
				application.getInvitationSentAt(),
				application.getInvitationAcceptedAt(),
				application.getInvitationFailureReason(),
				application.getCreatedAt()
		);
	}
}
