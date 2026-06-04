package se.iso27001platform.iso27001backend.user.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.common.exception.DuplicateResourceException;
import se.iso27001platform.iso27001backend.user.model.UserProfile;
import se.iso27001platform.iso27001backend.user.repository.UserProfileRepository;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class UserProfileService {

	private final UserProfileRepository userProfileRepository;

	public UserProfileService(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

	public UserProfile getOrCreate(UUID supabaseUserId, String email) {
		String normalizedEmail = normalizeEmail(email);
		return userProfileRepository.findBySupabaseUserId(supabaseUserId)
				.map(userProfile -> updateEmail(userProfile, normalizedEmail))
				.orElseGet(() -> linkByEmailOrCreate(supabaseUserId, normalizedEmail));
	}

	public UserProfile getOrCreateForMembership(UUID supabaseUserId, String email) {
		return getOrCreate(supabaseUserId, email);
	}

	public UserProfile createUnlinked(String email) {
		String normalizedEmail = normalizeEmail(email);
		return userProfileRepository.findFirstByEmailOrderByCreatedAtAsc(normalizedEmail)
				.orElseGet(() -> userProfileRepository.save(new UserProfile(null, normalizedEmail)));
	}

	public UserProfile linkSupabaseIdentity(UserProfile userProfile, UUID supabaseUserId) {
		userProfileRepository.findBySupabaseUserId(supabaseUserId)
				.filter(existingProfile -> !existingProfile.getId().equals(userProfile.getId()))
				.ifPresent(existingProfile -> {
					throw new DuplicateResourceException("Supabase user is already linked to another user profile");
				});
		if (userProfile.getSupabaseUserId() != null && !userProfile.getSupabaseUserId().equals(supabaseUserId)) {
			throw new DuplicateResourceException("User profile is already linked to another Supabase user");
		}
		if (userProfile.getSupabaseUserId() == null) {
			userProfile.linkSupabaseIdentity(supabaseUserId);
		}
		return userProfile;
	}

	private UserProfile updateEmail(UserProfile userProfile, String email) {
		if (!userProfile.getEmail().equals(email)) {
			userProfile.updateEmail(email);
		}
		return userProfile;
	}

	private UserProfile linkByEmailOrCreate(UUID supabaseUserId, String email) {
		return userProfileRepository.findFirstByEmailOrderByCreatedAtAsc(email)
				.map(userProfile -> {
					if (userProfile.getSupabaseUserId() != null
							&& !userProfile.getSupabaseUserId().equals(supabaseUserId)) {
						throw new AccessDeniedException("Authenticated email is linked to another Supabase user");
					}
					return linkSupabaseIdentity(userProfile, supabaseUserId);
				})
				.orElseGet(() -> userProfileRepository.save(new UserProfile(supabaseUserId, email)));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
