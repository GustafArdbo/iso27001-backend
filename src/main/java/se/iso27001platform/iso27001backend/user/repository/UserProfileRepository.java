package se.iso27001platform.iso27001backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.user.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

	Optional<UserProfile> findBySupabaseUserId(UUID supabaseUserId);

	Optional<UserProfile> findFirstByEmailOrderByCreatedAtAsc(String email);
}
