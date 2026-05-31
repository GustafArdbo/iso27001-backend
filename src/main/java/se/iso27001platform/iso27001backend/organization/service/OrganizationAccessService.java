package se.iso27001platform.iso27001backend.organization.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.service.CurrentUserService;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class OrganizationAccessService {

	private static final EnumSet<MembershipRole> OWNER_OR_ADMIN = EnumSet.of(MembershipRole.OWNER, MembershipRole.ADMIN);
	private static final EnumSet<MembershipRole> ASSESSMENT_EDITORS = EnumSet.of(
			MembershipRole.OWNER,
			MembershipRole.ADMIN,
			MembershipRole.AUDITOR
	);
	private static final EnumSet<MembershipRole> ANSWER_EDITORS = EnumSet.of(
			MembershipRole.OWNER,
			MembershipRole.ADMIN,
			MembershipRole.AUDITOR,
			MembershipRole.MEMBER
	);

	private final CurrentUserService currentUserService;

	public OrganizationAccessService(CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public OrganizationMembership requireMember(UUID organizationId) {
		return currentUserService.currentMembership(organizationId)
				.orElseThrow(() -> new AccessDeniedException("Membership is required for organization: " + organizationId));
	}

	@Transactional(readOnly = true)
	public OrganizationMembership requireOwnerOrAdmin(UUID organizationId) {
		return requireAnyRole(organizationId, OWNER_OR_ADMIN);
	}

	@Transactional(readOnly = true)
	public OrganizationMembership requireAssessmentEditor(UUID organizationId) {
		return requireAnyRole(organizationId, ASSESSMENT_EDITORS);
	}

	@Transactional(readOnly = true)
	public OrganizationMembership requireAnswerEditor(UUID organizationId) {
		return requireAnyRole(organizationId, ANSWER_EDITORS);
	}

	@Transactional(readOnly = true)
	public void requireCanReadMembership(OrganizationMembership membership) {
		UUID currentSupabaseUserId = currentUserService.currentSupabaseUserId();
		if (currentSupabaseUserId.equals(membership.getUserProfile().getSupabaseUserId())) {
			return;
		}

		requireOwnerOrAdmin(membership.getOrganization().getId());
	}

	private OrganizationMembership requireAnyRole(UUID organizationId, EnumSet<MembershipRole> allowedRoles) {
		OrganizationMembership membership = requireMember(organizationId);
		if (!allowedRoles.contains(membership.getRole())) {
			throw new AccessDeniedException(
					"Required one of roles " + Arrays.toString(allowedRoles.toArray()) + " for organization: " + organizationId
			);
		}
		return membership;
	}
}
