package se.iso27001platform.iso27001backend.invitation.dto;

import se.iso27001platform.iso27001backend.invitation.model.OrganizationInvitation;

public record CreateInvitationResponse(
		InvitationResponse invitation,
		String acceptanceToken
) {

	public static CreateInvitationResponse from(OrganizationInvitation invitation, String acceptanceToken) {
		return new CreateInvitationResponse(InvitationResponse.from(invitation), acceptanceToken);
	}
}
