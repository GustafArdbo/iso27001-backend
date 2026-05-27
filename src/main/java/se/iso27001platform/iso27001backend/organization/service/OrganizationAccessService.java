package se.iso27001platform.iso27001backend.organization.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.service.CurrentUserService;
import se.iso27001platform.iso27001backend.user.enums.UserRole;
import se.iso27001platform.iso27001backend.user.model.AppUser;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class OrganizationAccessService {

	private static final EnumSet<UserRole> OWNER_OR_ADMIN = EnumSet.of(UserRole.OWNER, UserRole.ADMIN);
	private static final EnumSet<UserRole> ASSESSMENT_EDITORS = EnumSet.of(UserRole.OWNER, UserRole.ADMIN, UserRole.AUDITOR);
	private static final EnumSet<UserRole> ANSWER_EDITORS = EnumSet.of(UserRole.OWNER, UserRole.ADMIN, UserRole.AUDITOR, UserRole.MEMBER);

	private final CurrentUserService currentUserService;

	public OrganizationAccessService(CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public AppUser requireMember(UUID organizationId) {
		return currentUserService.currentMembership(organizationId)
				.orElseThrow(() -> new AccessDeniedException("Membership is required for organization: " + organizationId));
	}

	@Transactional(readOnly = true)
	public AppUser requireOwnerOrAdmin(UUID organizationId) {
		return requireAnyRole(organizationId, OWNER_OR_ADMIN);
	}

	@Transactional(readOnly = true)
	public AppUser requireAssessmentEditor(UUID organizationId) {
		return requireAnyRole(organizationId, ASSESSMENT_EDITORS);
	}

	@Transactional(readOnly = true)
	public AppUser requireAnswerEditor(UUID organizationId) {
		return requireAnyRole(organizationId, ANSWER_EDITORS);
	}

	@Transactional(readOnly = true)
	public void requireCanReadUser(AppUser appUser) {
		UUID currentSupabaseUserId = currentUserService.currentSupabaseUserId();
		if (currentSupabaseUserId.equals(appUser.getSupabaseUserId())) {
			return;
		}

		requireOwnerOrAdmin(appUser.getOrganization().getId());
	}

	private AppUser requireAnyRole(UUID organizationId, EnumSet<UserRole> allowedRoles) {
		AppUser membership = requireMember(organizationId);
		if (!allowedRoles.contains(membership.getRole())) {
			throw new AccessDeniedException(
					"Required one of roles " + Arrays.toString(allowedRoles.toArray()) + " for organization: " + organizationId
			);
		}
		return membership;
	}
}
