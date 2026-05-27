package se.iso27001platform.iso27001backend.user.dto;

import se.iso27001platform.iso27001backend.user.enums.UserRole;
import se.iso27001platform.iso27001backend.user.model.AppUser;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
		UUID id,
		UUID organizationId,
		String email,
		UUID supabaseUserId,
		UserRole role,
		Instant createdAt
) {

	public static UserResponse from(AppUser appUser) {
		return new UserResponse(
				appUser.getId(),
				appUser.getOrganization().getId(),
				appUser.getEmail(),
				appUser.getSupabaseUserId(),
				appUser.getRole(),
				appUser.getCreatedAt()
		);
	}
}
