package se.iso27001platform.iso27001backend.membership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.common.exception.DuplicateResourceException;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.membership.dto.CreateMembershipRequest;
import se.iso27001platform.iso27001backend.membership.dto.MembershipResponse;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;
import se.iso27001platform.iso27001backend.membership.repository.OrganizationMembershipRepository;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.service.OrganizationAccessService;
import se.iso27001platform.iso27001backend.organization.service.OrganizationService;
import se.iso27001platform.iso27001backend.user.model.UserProfile;
import se.iso27001platform.iso27001backend.user.service.UserProfileService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class MembershipService {

	private final OrganizationMembershipRepository membershipRepository;
	private final OrganizationService organizationService;
	private final OrganizationAccessService organizationAccessService;
	private final UserProfileService userProfileService;

	public MembershipService(
			OrganizationMembershipRepository membershipRepository,
			OrganizationService organizationService,
			OrganizationAccessService organizationAccessService,
			UserProfileService userProfileService
	) {
		this.membershipRepository = membershipRepository;
		this.organizationService = organizationService;
		this.organizationAccessService = organizationAccessService;
		this.userProfileService = userProfileService;
	}

	public MembershipResponse create(UUID organizationId, CreateMembershipRequest request) {
		Organization organization = organizationService.getRequired(organizationId);
		organizationAccessService.requireOwnerOrAdmin(organizationId);
		String email = normalizeEmail(request.email());

		if (membershipRepository.existsByOrganization_IdAndUserProfile_Email(organizationId, email)) {
			throw new DuplicateResourceException("Membership already exists in organization for email: " + email);
		}
		if (request.supabaseUserId() != null
				&& membershipRepository.existsByOrganization_IdAndUserProfile_SupabaseUserId(
						organizationId,
						request.supabaseUserId()
				)) {
			throw new DuplicateResourceException("Membership already exists in organization for Supabase user: " + request.supabaseUserId());
		}

		UserProfile userProfile = request.supabaseUserId() == null
				? userProfileService.createUnlinked(email)
				: userProfileService.getOrCreateForMembership(request.supabaseUserId(), email);
		OrganizationMembership membership = membershipRepository.save(new OrganizationMembership(
				organization,
				userProfile,
				request.role()
		));
		return MembershipResponse.from(membership);
	}

	@Transactional(readOnly = true)
	public MembershipResponse findById(UUID id) {
		OrganizationMembership membership = getRequired(id);
		organizationAccessService.requireCanReadMembership(membership);
		return MembershipResponse.from(membership);
	}

	@Transactional(readOnly = true)
	public List<MembershipResponse> findByOrganization(UUID organizationId) {
		organizationService.getRequired(organizationId);
		organizationAccessService.requireOwnerOrAdmin(organizationId);
		return membershipRepository.findByOrganization_IdOrderByCreatedAtAsc(organizationId).stream()
				.map(MembershipResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrganizationMembership getRequired(UUID id) {
		return membershipRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + id));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
