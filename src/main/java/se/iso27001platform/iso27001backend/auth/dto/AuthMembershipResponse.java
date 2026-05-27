package se.iso27001platform.iso27001backend.auth.dto;

import se.iso27001platform.iso27001backend.user.enums.UserRole;
import se.iso27001platform.iso27001backend.user.model.AppUser;

import java.util.UUID;

public record AuthMembershipResponse(
		UUID userId,
		UUID organizationId,
		String email,
		UserRole role
) {

	public static AuthMembershipResponse from(AppUser appUser) {
		return new AuthMembershipResponse(
				appUser.getId(),
				appUser.getOrganization().getId(),
				appUser.getEmail(),
				appUser.getRole()
		);
	}
}
