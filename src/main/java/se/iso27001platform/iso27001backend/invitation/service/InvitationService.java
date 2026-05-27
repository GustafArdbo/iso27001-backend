package se.iso27001platform.iso27001backend.invitation.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.service.CurrentUserService;
import se.iso27001platform.iso27001backend.common.exception.BadRequestException;
import se.iso27001platform.iso27001backend.common.exception.DuplicateResourceException;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.invitation.dto.AcceptInvitationResponse;
import se.iso27001platform.iso27001backend.invitation.dto.CreateInvitationRequest;
import se.iso27001platform.iso27001backend.invitation.dto.CreateInvitationResponse;
import se.iso27001platform.iso27001backend.invitation.dto.InvitationResponse;
import se.iso27001platform.iso27001backend.invitation.enums.InvitationStatus;
import se.iso27001platform.iso27001backend.invitation.model.OrganizationInvitation;
import se.iso27001platform.iso27001backend.invitation.repository.OrganizationInvitationRepository;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.service.OrganizationAccessService;
import se.iso27001platform.iso27001backend.organization.service.OrganizationService;
import se.iso27001platform.iso27001backend.user.enums.UserRole;
import se.iso27001platform.iso27001backend.user.model.AppUser;
import se.iso27001platform.iso27001backend.user.repository.AppUserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class InvitationService {

	private static final Duration INVITATION_TTL = Duration.ofDays(7);

	private final OrganizationInvitationRepository invitationRepository;
	private final OrganizationService organizationService;
	private final OrganizationAccessService organizationAccessService;
	private final AppUserRepository appUserRepository;
	private final CurrentUserService currentUserService;
	private final InvitationTokenService invitationTokenService;

	public InvitationService(
			OrganizationInvitationRepository invitationRepository,
			OrganizationService organizationService,
			OrganizationAccessService organizationAccessService,
			AppUserRepository appUserRepository,
			CurrentUserService currentUserService,
			InvitationTokenService invitationTokenService
	) {
		this.invitationRepository = invitationRepository;
		this.organizationService = organizationService;
		this.organizationAccessService = organizationAccessService;
		this.appUserRepository = appUserRepository;
		this.currentUserService = currentUserService;
		this.invitationTokenService = invitationTokenService;
	}

	public CreateInvitationResponse create(UUID organizationId, CreateInvitationRequest request) {
		Organization organization = organizationService.getRequired(organizationId);
		AppUser invitedByUser = organizationAccessService.requireOwnerOrAdmin(organizationId);
		requireRoleCanInvite(invitedByUser.getRole(), request.role());

		String email = normalizeEmail(request.email());
		if (appUserRepository.existsByOrganization_IdAndEmail(organizationId, email)) {
			throw new DuplicateResourceException("User already exists in organization: " + email);
		}

		Instant now = Instant.now();
		invitationRepository
				.findFirstByOrganization_IdAndEmailAndStatusOrderByCreatedAtDesc(organizationId, email, InvitationStatus.PENDING)
				.ifPresent(existingInvitation -> requireNoActivePendingInvitation(existingInvitation, now, email));

		String token = invitationTokenService.generateToken();
		OrganizationInvitation invitation = invitationRepository.save(new OrganizationInvitation(
				organization,
				email,
				request.role(),
				invitationTokenService.hashToken(token),
				now.plus(INVITATION_TTL),
				invitedByUser
		));

		return CreateInvitationResponse.from(invitation, token);
	}

	public List<InvitationResponse> findByOrganization(UUID organizationId) {
		organizationService.getRequired(organizationId);
		organizationAccessService.requireOwnerOrAdmin(organizationId);
		Instant now = Instant.now();
		List<OrganizationInvitation> invitations = invitationRepository.findByOrganization_IdOrderByCreatedAtDesc(organizationId);
		invitations.forEach(invitation -> expireIfNeeded(invitation, now));

		return invitations.stream()
				.map(InvitationResponse::from)
				.toList();
	}

	public AcceptInvitationResponse accept(String token) {
		Instant now = Instant.now();
		OrganizationInvitation invitation = invitationRepository.findByTokenHash(invitationTokenService.hashToken(token))
				.orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

		if (invitation.isExpiredAt(now)) {
			invitation.markExpired();
			throw new BadRequestException("Invitation has expired");
		}
		if (invitation.getStatus() != InvitationStatus.PENDING) {
			throw new BadRequestException("Invitation is not pending");
		}

		String currentEmail = currentUserService.currentEmail();
		if (!invitation.getEmail().equals(currentEmail)) {
			throw new AccessDeniedException("Invitation email does not match current user");
		}

		UUID organizationId = invitation.getOrganization().getId();
		UUID currentSupabaseUserId = currentUserService.currentSupabaseUserId();
		if (appUserRepository.existsByOrganization_IdAndEmail(organizationId, currentEmail)) {
			throw new DuplicateResourceException("User already exists in organization: " + currentEmail);
		}
		if (appUserRepository.existsByOrganization_IdAndSupabaseUserId(organizationId, currentSupabaseUserId)) {
			throw new DuplicateResourceException("Supabase user already exists in organization: " + currentSupabaseUserId);
		}

		AppUser appUser = appUserRepository.save(new AppUser(
				invitation.getOrganization(),
				currentEmail,
				currentSupabaseUserId,
				invitation.getRole()
		));
		invitation.markAccepted(appUser, now);

		return AcceptInvitationResponse.from(invitation, appUser);
	}

	public void revoke(UUID organizationId, UUID invitationId) {
		organizationService.getRequired(organizationId);
		AppUser revokedByUser = organizationAccessService.requireOwnerOrAdmin(organizationId);
		OrganizationInvitation invitation = invitationRepository.findByOrganization_IdAndId(organizationId, invitationId)
				.orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));

		Instant now = Instant.now();
		if (invitation.isExpiredAt(now)) {
			invitation.markExpired();
			throw new BadRequestException("Invitation has expired");
		}
		if (invitation.getStatus() != InvitationStatus.PENDING) {
			throw new BadRequestException("Invitation is not pending");
		}

		invitation.markRevoked(revokedByUser, now);
	}

	private void requireRoleCanInvite(UserRole currentRole, UserRole invitedRole) {
		if (currentRole == UserRole.ADMIN && invitedRole == UserRole.OWNER) {
			throw new AccessDeniedException("Admins cannot invite owners");
		}
	}

	private void requireNoActivePendingInvitation(OrganizationInvitation invitation, Instant now, String email) {
		if (invitation.isExpiredAt(now)) {
			invitation.markExpired();
			return;
		}
		throw new DuplicateResourceException("Invitation already pending for organization: " + email);
	}

	private void expireIfNeeded(OrganizationInvitation invitation, Instant now) {
		if (invitation.isExpiredAt(now)) {
			invitation.markExpired();
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
