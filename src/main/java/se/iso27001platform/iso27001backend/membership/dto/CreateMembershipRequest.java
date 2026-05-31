package se.iso27001platform.iso27001backend.membership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;

import java.util.UUID;

public record CreateMembershipRequest(
		@NotBlank @Email String email,
		UUID supabaseUserId,
		@NotNull MembershipRole role
) {

	public CreateMembershipRequest {
		if (email != null) {
			email = email.trim();
		}
	}
}
