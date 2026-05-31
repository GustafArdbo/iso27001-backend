package se.iso27001platform.iso27001backend.invitation.dto;

import se.iso27001platform.iso27001backend.invitation.model.OrganizationInvitation;
import se.iso27001platform.iso27001backend.membership.dto.MembershipResponse;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;

public record AcceptInvitationResponse(
		InvitationResponse invitation,
		MembershipResponse membership
) {

	public static AcceptInvitationResponse from(OrganizationInvitation invitation, OrganizationMembership membership) {
		return new AcceptInvitationResponse(InvitationResponse.from(invitation), MembershipResponse.from(membership));
	}
}
