package se.iso27001platform.iso27001backend.invitation.dto;

import se.iso27001platform.iso27001backend.invitation.model.OrganizationInvitation;
import se.iso27001platform.iso27001backend.user.dto.UserResponse;
import se.iso27001platform.iso27001backend.user.model.AppUser;

public record AcceptInvitationResponse(
		InvitationResponse invitation,
		UserResponse user
) {

	public static AcceptInvitationResponse from(OrganizationInvitation invitation, AppUser user) {
		return new AcceptInvitationResponse(InvitationResponse.from(invitation), UserResponse.from(user));
	}
}
