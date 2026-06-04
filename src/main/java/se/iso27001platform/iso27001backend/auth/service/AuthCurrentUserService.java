package se.iso27001platform.iso27001backend.auth.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.dto.AuthMembershipResponse;
import se.iso27001platform.iso27001backend.auth.dto.AuthUserResponse;
import se.iso27001platform.iso27001backend.membership.repository.OrganizationMembershipRepository;
import se.iso27001platform.iso27001backend.onboarding.service.OrganizationApplicationService;
import se.iso27001platform.iso27001backend.user.dto.UserProfileResponse;
import se.iso27001platform.iso27001backend.user.model.UserProfile;
import se.iso27001platform.iso27001backend.user.service.UserProfileService;

import java.util.UUID;

@Service
public class AuthCurrentUserService {

	private final UserProfileService userProfileService;
	private final OrganizationMembershipRepository membershipRepository;
	private final OrganizationApplicationService applicationService;
	private final PlatformAdminAccessService platformAdminAccessService;
	private final CurrentUserService currentUserService;

	public AuthCurrentUserService(
			UserProfileService userProfileService,
			OrganizationMembershipRepository membershipRepository,
			OrganizationApplicationService applicationService,
			PlatformAdminAccessService platformAdminAccessService,
			CurrentUserService currentUserService
	) {
		this.userProfileService = userProfileService;
		this.membershipRepository = membershipRepository;
		this.applicationService = applicationService;
		this.platformAdminAccessService = platformAdminAccessService;
		this.currentUserService = currentUserService;
	}

	@Transactional
	public AuthUserResponse currentUser(Jwt jwt) {
		UUID supabaseUserId = currentUserService.currentSupabaseUserId();
		String email = currentUserService.currentEmail();
		UserProfile userProfile = userProfileService.getOrCreate(supabaseUserId, email);
		applicationService.completeOwnerAuthentication(userProfile);
		return new AuthUserResponse(
				jwt.getSubject(),
				email,
				jwt.getClaimAsString("role"),
				sessionId(jwt),
				jwt.getId(),
				jwt.getExpiresAt(),
				platformAdminAccessService.isPlatformAdmin(supabaseUserId),
				UserProfileResponse.from(userProfile),
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
