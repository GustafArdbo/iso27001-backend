package se.iso27001platform.iso27001backend.invitation.dto;

import se.iso27001platform.iso27001backend.invitation.enums.InvitationStatus;
import se.iso27001platform.iso27001backend.invitation.model.OrganizationInvitation;
import se.iso27001platform.iso27001backend.user.enums.UserRole;
import se.iso27001platform.iso27001backend.user.model.AppUser;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
		UUID id,
		UUID organizationId,
		String email,
		UserRole role,
		InvitationStatus status,
		Instant expiresAt,
		Instant acceptedAt,
		Instant revokedAt,
		UUID invitedByUserId,
		UUID acceptedByUserId,
		UUID revokedByUserId,
		Instant createdAt
) {

	public static InvitationResponse from(OrganizationInvitation invitation) {
		return new InvitationResponse(
				invitation.getId(),
				invitation.getOrganization().getId(),
				invitation.getEmail(),
				invitation.getRole(),
				invitation.getStatus(),
				invitation.getExpiresAt(),
				invitation.getAcceptedAt(),
				invitation.getRevokedAt(),
				invitation.getInvitedByUser().getId(),
				userId(invitation.getAcceptedByUser()),
				userId(invitation.getRevokedByUser()),
				invitation.getCreatedAt()
		);
	}

	private static UUID userId(AppUser user) {
		return user == null ? null : user.getId();
	}
}
