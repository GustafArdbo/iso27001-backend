package se.iso27001platform.iso27001backend.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.common.exception.DuplicateResourceException;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.service.OrganizationAccessService;
import se.iso27001platform.iso27001backend.organization.service.OrganizationService;
import se.iso27001platform.iso27001backend.user.dto.CreateUserRequest;
import se.iso27001platform.iso27001backend.user.dto.UserResponse;
import se.iso27001platform.iso27001backend.user.model.AppUser;
import se.iso27001platform.iso27001backend.user.repository.AppUserRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class UserService {

	private final AppUserRepository appUserRepository;
	private final OrganizationService organizationService;
	private final OrganizationAccessService organizationAccessService;

	public UserService(
			AppUserRepository appUserRepository,
			OrganizationService organizationService,
			OrganizationAccessService organizationAccessService
	) {
		this.appUserRepository = appUserRepository;
		this.organizationService = organizationService;
		this.organizationAccessService = organizationAccessService;
	}

	public UserResponse create(UUID organizationId, CreateUserRequest request) {
		Organization organization = organizationService.getRequired(organizationId);
		organizationAccessService.requireOwnerOrAdmin(organizationId);
		String email = normalizeEmail(request.email());

		if (appUserRepository.existsByOrganization_IdAndEmail(organizationId, email)) {
			throw new DuplicateResourceException("User already exists in organization: " + email);
		}
		if (request.supabaseUserId() != null
				&& appUserRepository.existsByOrganization_IdAndSupabaseUserId(organizationId, request.supabaseUserId())) {
			throw new DuplicateResourceException("Supabase user already exists in organization: " + request.supabaseUserId());
		}

		AppUser appUser = appUserRepository.save(new AppUser(organization, email, request.supabaseUserId(), request.role()));
		return UserResponse.from(appUser);
	}

	@Transactional(readOnly = true)
	public UserResponse findById(UUID id) {
		AppUser appUser = getRequired(id);
		organizationAccessService.requireCanReadUser(appUser);
		return UserResponse.from(appUser);
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findByOrganization(UUID organizationId) {
		organizationService.getRequired(organizationId);
		organizationAccessService.requireOwnerOrAdmin(organizationId);
		return appUserRepository.findByOrganization_IdOrderByCreatedAtAsc(organizationId).stream()
				.map(UserResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AppUser getRequired(UUID id) {
		return appUserRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
