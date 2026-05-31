package se.iso27001platform.iso27001backend.auth.dto;

import se.iso27001platform.iso27001backend.user.dto.UserProfileResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthUserResponse(
		String subject,
		String email,
		String providerRole,
		UUID sessionId,
		String jwtId,
		Instant expiresAt,
		UserProfileResponse profile,
		List<AuthMembershipResponse> memberships
) {
}
