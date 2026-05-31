package se.iso27001platform.iso27001backend.invitation.dto;

import se.iso27001platform.iso27001backend.invitation.enums.InvitationStatus;
import se.iso27001platform.iso27001backend.invitation.model.OrganizationInvitation;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
		UUID id,
		UUID organizationId,
		String email,
		MembershipRole role,
		InvitationStatus status,
		Instant expiresAt,
		Instant acceptedAt,
		Instant revokedAt,
		UUID invitedByMembershipId,
		UUID acceptedByMembershipId,
		UUID revokedByMembershipId,
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
				invitation.getInvitedByMembership().getId(),
				membershipId(invitation.getAcceptedByMembership()),
				membershipId(invitation.getRevokedByMembership()),
				invitation.getCreatedAt()
		);
	}

	private static UUID membershipId(OrganizationMembership membership) {
		return membership == null ? null : membership.getId();
	}
}
