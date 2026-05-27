package se.iso27001platform.iso27001backend.invitation.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
		@NotBlank String token
) {

	public AcceptInvitationRequest {
		if (token != null) {
			token = token.trim();
		}
	}
}
