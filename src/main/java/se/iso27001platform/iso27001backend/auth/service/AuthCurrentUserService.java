package se.iso27001platform.iso27001backend.auth.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.dto.AuthMembershipResponse;
import se.iso27001platform.iso27001backend.auth.dto.AuthUserResponse;
import se.iso27001platform.iso27001backend.membership.repository.OrganizationMembershipRepository;
import se.iso27001platform.iso27001backend.user.dto.UserProfileResponse;
import se.iso27001platform.iso27001backend.user.repository.UserProfileRepository;

import java.util.UUID;

@Service
public class AuthCurrentUserService {

	private final UserProfileRepository userProfileRepository;
	private final OrganizationMembershipRepository membershipRepository;

	public AuthCurrentUserService(
			UserProfileRepository userProfileRepository,
			OrganizationMembershipRepository membershipRepository
	) {
		this.userProfileRepository = userProfileRepository;
		this.membershipRepository = membershipRepository;
	}

	@Transactional(readOnly = true)
	public AuthUserResponse currentUser(Jwt jwt) {
		UUID supabaseUserId = UUID.fromString(jwt.getSubject());
		return new AuthUserResponse(
				jwt.getSubject(),
				jwt.getClaimAsString("email"),
				jwt.getClaimAsString("role"),
				sessionId(jwt),
				jwt.getId(),
				jwt.getExpiresAt(),
				userProfileRepository.findBySupabaseUserId(supabaseUserId)
						.map(UserProfileResponse::from)
						.orElse(null),
				membershipRepository.findByUserProfile_SupabaseUserIdOrderByCreatedAtAsc(supabaseUserId).stream()
						.map(AuthMembershipResponse::from)
						.toList()
		);
	}

	private UUID sessionId(Jwt jwt) {
		String sessionId = jwt.getClaimAsString("session_id");
		return sessionId == null || sessionId.isBlank() ? null : UUID.fromString(sessionId);
	}
}
