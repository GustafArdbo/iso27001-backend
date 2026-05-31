package se.iso27001platform.iso27001backend.auth.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;
import se.iso27001platform.iso27001backend.membership.repository.OrganizationMembershipRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrentUserService {

	private final OrganizationMembershipRepository membershipRepository;

	public CurrentUserService(OrganizationMembershipRepository membershipRepository) {
		this.membershipRepository = membershipRepository;
	}

	public Jwt currentJwt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			return jwtAuthenticationToken.getToken();
		}

		throw new AccessDeniedException("Authenticated JWT is required");
	}

	public UUID currentSupabaseUserId() {
		try {
			return UUID.fromString(currentJwt().getSubject());
		}
		catch (IllegalArgumentException exception) {
			throw new AccessDeniedException("JWT subject must be a Supabase user UUID");
		}
	}

	public String currentEmail() {
		String email = currentJwt().getClaimAsString("email");
		if (email == null || email.isBlank()) {
			throw new AccessDeniedException("JWT email claim is required");
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	@Transactional(readOnly = true)
	public List<OrganizationMembership> currentMemberships() {
		return membershipRepository.findByUserProfile_SupabaseUserIdOrderByCreatedAtAsc(currentSupabaseUserId());
	}

	@Transactional(readOnly = true)
	public Optional<OrganizationMembership> currentMembership(UUID organizationId) {
		return membershipRepository.findByOrganization_IdAndUserProfile_SupabaseUserId(
				organizationId,
				currentSupabaseUserId()
		);
	}
}
