package se.iso27001platform.iso27001backend.user.dto;

import se.iso27001platform.iso27001backend.user.model.UserProfile;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
		UUID id,
		UUID supabaseUserId,
		String email,
		Instant createdAt
) {

	public static UserProfileResponse from(UserProfile userProfile) {
		return new UserProfileResponse(
				userProfile.getId(),
				userProfile.getSupabaseUserId(),
				userProfile.getEmail(),
				userProfile.getCreatedAt()
		);
	}
}
