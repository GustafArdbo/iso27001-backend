package se.iso27001platform.iso27001backend.invitation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;

public record CreateInvitationRequest(
		@NotBlank @Email String email,
		@NotNull MembershipRole role
) {

	public CreateInvitationRequest {
		if (email != null) {
			email = email.trim();
		}
	}
}
