package se.iso27001platform.iso27001backend.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
				.orElseGet(() -> userProfileRepository.save(new UserProfile(supabaseUserId, normalizedEmail)));
	}

	public UserProfile getOrCreateForMembership(UUID supabaseUserId, String email) {
		String normalizedEmail = normalizeEmail(email);
		return userProfileRepository.findBySupabaseUserId(supabaseUserId)
				.orElseGet(() -> userProfileRepository.save(new UserProfile(supabaseUserId, normalizedEmail)));
	}

	public UserProfile createUnlinked(String email) {
		return userProfileRepository.save(new UserProfile(null, normalizeEmail(email)));
	}

	private UserProfile updateEmail(UserProfile userProfile, String email) {
		if (!userProfile.getEmail().equals(email)) {
			userProfile.updateEmail(email);
		}
		return userProfile;
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
