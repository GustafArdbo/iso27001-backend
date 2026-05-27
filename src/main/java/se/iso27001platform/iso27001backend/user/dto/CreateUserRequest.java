package se.iso27001platform.iso27001backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.iso27001platform.iso27001backend.user.enums.UserRole;

import java.util.UUID;

public record CreateUserRequest(
		@NotBlank @Email String email,
		UUID supabaseUserId,
		@NotNull UserRole role
) {

	public CreateUserRequest {
		if (email != null) {
			email = email.trim();
		}
	}
}
