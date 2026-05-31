package se.iso27001platform.iso27001backend.auth.dto;

import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;

import java.util.UUID;

public record AuthMembershipResponse(
		UUID membershipId,
		UUID organizationId,
		UUID userProfileId,
		String email,
		MembershipRole role
) {

	public static AuthMembershipResponse from(OrganizationMembership membership) {
		return new AuthMembershipResponse(
				membership.getId(),
				membership.getOrganization().getId(),
				membership.getUserProfile().getId(),
				membership.getUserProfile().getEmail(),
				membership.getRole()
		);
	}
}
