package se.iso27001platform.iso27001backend.membership.dto;

import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;

import java.time.Instant;
import java.util.UUID;

public record MembershipResponse(
		UUID id,
		UUID organizationId,
		UUID userProfileId,
		String email,
		UUID supabaseUserId,
		MembershipRole role,
		Instant createdAt
) {

	public static MembershipResponse from(OrganizationMembership membership) {
		return new MembershipResponse(
				membership.getId(),
				membership.getOrganization().getId(),
				membership.getUserProfile().getId(),
				membership.getUserProfile().getEmail(),
				membership.getUserProfile().getSupabaseUserId(),
				membership.getRole(),
				membership.getCreatedAt()
		);
	}
}
