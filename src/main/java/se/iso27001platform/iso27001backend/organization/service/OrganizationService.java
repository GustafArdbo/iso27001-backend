package se.iso27001platform.iso27001backend.organization.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.service.CurrentUserService;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;
import se.iso27001platform.iso27001backend.membership.repository.OrganizationMembershipRepository;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.repository.OrganizationRepository;
import se.iso27001platform.iso27001backend.organization.dto.CreateOrganizationRequest;
import se.iso27001platform.iso27001backend.organization.dto.OrganizationResponse;
import se.iso27001platform.iso27001backend.user.model.UserProfile;
import se.iso27001platform.iso27001backend.user.service.UserProfileService;

import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

	private final OrganizationRepository organizationRepository;
	private final OrganizationAccessService organizationAccessService;
	private final CurrentUserService currentUserService;
	private final UserProfileService userProfileService;
	private final OrganizationMembershipRepository membershipRepository;

	public OrganizationService(
			OrganizationRepository organizationRepository,
			OrganizationAccessService organizationAccessService,
			CurrentUserService currentUserService,
			UserProfileService userProfileService,
			OrganizationMembershipRepository membershipRepository
	) {
		this.organizationRepository = organizationRepository;
		this.organizationAccessService = organizationAccessService;
		this.currentUserService = currentUserService;
		this.userProfileService = userProfileService;
		this.membershipRepository = membershipRepository;
	}

	public OrganizationResponse create(CreateOrganizationRequest request) {
		Organization organization = organizationRepository.save(new Organization(request.name()));
		UserProfile userProfile = userProfileService.getOrCreate(
				currentUserService.currentSupabaseUserId(),
				currentUserService.currentEmail()
		);
		membershipRepository.save(new OrganizationMembership(
				organization,
				userProfile,
				MembershipRole.OWNER
		));
		return OrganizationResponse.from(organization);
	}

	@Transactional(readOnly = true)
	public OrganizationResponse findById(UUID id) {
		Organization organization = getRequired(id);
		organizationAccessService.requireMember(id);
		return OrganizationResponse.from(organization);
	}

	@Transactional(readOnly = true)
	public Organization getRequired(UUID id) {
		return organizationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
	}
}
