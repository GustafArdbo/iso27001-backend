package se.iso27001platform.iso27001backend.onboarding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.service.PlatformAdminAccessService;
import se.iso27001platform.iso27001backend.common.exception.BadRequestException;
import se.iso27001platform.iso27001backend.common.exception.DuplicateResourceException;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;
import se.iso27001platform.iso27001backend.membership.repository.OrganizationMembershipRepository;
import se.iso27001platform.iso27001backend.onboarding.client.OwnerInvitationDeliveryException;
import se.iso27001platform.iso27001backend.onboarding.client.OwnerInvitationResult;
import se.iso27001platform.iso27001backend.onboarding.client.OwnerInvitationSender;
import se.iso27001platform.iso27001backend.onboarding.dto.CreateOrganizationApplicationRequest;
import se.iso27001platform.iso27001backend.onboarding.dto.OrganizationApplicationResponse;
import se.iso27001platform.iso27001backend.onboarding.dto.SubmittedOrganizationApplicationResponse;
import se.iso27001platform.iso27001backend.onboarding.enums.ApplicationStatus;
import se.iso27001platform.iso27001backend.onboarding.enums.OwnerInvitationStatus;
import se.iso27001platform.iso27001backend.onboarding.enums.RequestedMaterial;
import se.iso27001platform.iso27001backend.onboarding.model.OrganizationApplication;
import se.iso27001platform.iso27001backend.onboarding.repository.OrganizationApplicationRepository;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.repository.OrganizationRepository;
import se.iso27001platform.iso27001backend.user.model.UserProfile;
import se.iso27001platform.iso27001backend.user.service.UserProfileService;

import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class OrganizationApplicationService {

	private static final Set<String> COMPANY_SIZES = Set.of("1-10", "11-50", "51-200", "201-500", "500+");
	private static final EnumSet<ApplicationStatus> ACTIVE_APPLICATION_STATUSES =
			EnumSet.of(ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED);

	private final OrganizationApplicationRepository applicationRepository;
	private final OrganizationRepository organizationRepository;
	private final OrganizationMembershipRepository membershipRepository;
	private final UserProfileService userProfileService;
	private final PlatformAdminAccessService platformAdminAccessService;
	private final OwnerInvitationSender ownerInvitationSender;

	public OrganizationApplicationService(
			OrganizationApplicationRepository applicationRepository,
			OrganizationRepository organizationRepository,
			OrganizationMembershipRepository membershipRepository,
			UserProfileService userProfileService,
			PlatformAdminAccessService platformAdminAccessService,
			OwnerInvitationSender ownerInvitationSender
	) {
		this.applicationRepository = applicationRepository;
		this.organizationRepository = organizationRepository;
		this.membershipRepository = membershipRepository;
		this.userProfileService = userProfileService;
		this.platformAdminAccessService = platformAdminAccessService;
		this.ownerInvitationSender = ownerInvitationSender;
	}

	public SubmittedOrganizationApplicationResponse submit(CreateOrganizationApplicationRequest request) {
		String ownerEmail = normalizeEmail(request.email());
		if (applicationRepository.existsByOwnerEmailAndApplicationStatusIn(ownerEmail, ACTIVE_APPLICATION_STATUSES)) {
			throw new DuplicateResourceException("An active organization application already exists for email: " + ownerEmail);
		}

		String companySize = request.size().trim();
		if (!COMPANY_SIZES.contains(companySize)) {
			throw new BadRequestException("Unsupported company size: " + companySize);
		}

		Set<RequestedMaterial> materials = new LinkedHashSet<>();
		for (String material : request.materials()) {
			if (!materials.add(RequestedMaterial.fromClientValue(material))) {
				throw new BadRequestException("Duplicate requested material: " + material);
			}
		}

		OrganizationApplication application = applicationRepository.save(new OrganizationApplication(
				request.company().trim(),
				request.name().trim(),
				ownerEmail,
				request.country().trim(),
				normalizeOptional(request.phone()),
				companySize,
				normalizeOptional(request.message()),
				materials
		));
		return SubmittedOrganizationApplicationResponse.from(application);
	}

	@Transactional(readOnly = true)
	public List<OrganizationApplicationResponse> findAll() {
		platformAdminAccessService.requirePlatformAdmin();
		return applicationRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(OrganizationApplicationResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrganizationApplicationResponse findById(UUID id) {
		platformAdminAccessService.requirePlatformAdmin();
		return OrganizationApplicationResponse.from(getRequired(id));
	}

	public OrganizationApplicationResponse approve(UUID id) {
		UUID adminSupabaseUserId = platformAdminAccessService.requirePlatformAdmin();
		OrganizationApplication application = getRequiredForUpdate(id);

		if (application.getApplicationStatus() == ApplicationStatus.REJECTED) {
			throw new BadRequestException("Rejected organization application cannot be approved");
		}
		if (application.getApplicationStatus() == ApplicationStatus.APPROVED) {
			return OrganizationApplicationResponse.from(application);
		}

		Organization organization = organizationRepository.save(new Organization(application.getCompanyName()));
		UserProfile ownerProfile = userProfileService.createUnlinked(application.getOwnerEmail());
		membershipRepository.save(new OrganizationMembership(organization, ownerProfile, MembershipRole.OWNER));
		application.approve(organization, ownerProfile, adminSupabaseUserId, Instant.now());
		deliverOwnerInvitation(application);

		return OrganizationApplicationResponse.from(application);
	}

	public OrganizationApplicationResponse reject(UUID id, String reason) {
		UUID adminSupabaseUserId = platformAdminAccessService.requirePlatformAdmin();
		OrganizationApplication application = getRequiredForUpdate(id);

		if (application.getApplicationStatus() == ApplicationStatus.APPROVED) {
			throw new BadRequestException("Approved organization application cannot be rejected");
		}
		if (application.getApplicationStatus() == ApplicationStatus.SUBMITTED) {
			application.reject(adminSupabaseUserId, Instant.now(), reason.trim());
		}
		return OrganizationApplicationResponse.from(application);
	}

	public OrganizationApplicationResponse resendOwnerInvitation(UUID id) {
		platformAdminAccessService.requirePlatformAdmin();
		OrganizationApplication application = getRequiredForUpdate(id);
		if (application.getApplicationStatus() != ApplicationStatus.APPROVED) {
			throw new BadRequestException("Organization application must be approved before sending an owner invitation");
		}
		if (application.getInvitationStatus() == OwnerInvitationStatus.ACCEPTED) {
			throw new BadRequestException("Owner invitation has already been accepted");
		}

		deliverOwnerInvitation(application);
		return OrganizationApplicationResponse.from(application);
	}

	public void completeOwnerAuthentication(UserProfile userProfile) {
		List<OrganizationApplication> applications = applicationRepository.findByOwnerProfile_IdAndApplicationStatus(
				userProfile.getId(),
				ApplicationStatus.APPROVED
		);
		Instant now = Instant.now();
		applications.stream()
				.filter(application -> application.getInvitationStatus() != OwnerInvitationStatus.ACCEPTED)
				.forEach(application -> application.markInvitationAccepted(now));
	}

	private void deliverOwnerInvitation(OrganizationApplication application) {
		try {
			OwnerInvitationResult result = ownerInvitationSender.send(
					application.getOwnerEmail(),
					application.getOwnerName(),
					application.getOrganization().getId()
			);
			if (result.supabaseUserId() != null) {
				userProfileService.linkSupabaseIdentity(application.getOwnerProfile(), result.supabaseUserId());
			}
			application.markInvitationSent(Instant.now());
		}
		catch (OwnerInvitationDeliveryException exception) {
			application.markInvitationFailed(exception.getMessage());
		}
	}

	private OrganizationApplication getRequired(UUID id) {
		return applicationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Organization application not found: " + id));
	}

	private OrganizationApplication getRequiredForUpdate(UUID id) {
		return applicationRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new ResourceNotFoundException("Organization application not found: " + id));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
