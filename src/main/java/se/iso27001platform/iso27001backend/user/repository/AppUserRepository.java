package se.iso27001platform.iso27001backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.user.model.AppUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

	boolean existsByOrganization_IdAndEmail(UUID organizationId, String email);

	boolean existsByOrganization_IdAndSupabaseUserId(UUID organizationId, UUID supabaseUserId);

	List<AppUser> findByOrganization_IdOrderByCreatedAtAsc(UUID organizationId);

	List<AppUser> findBySupabaseUserIdOrderByCreatedAtAsc(UUID supabaseUserId);

	Optional<AppUser> findByOrganization_IdAndEmail(UUID organizationId, String email);
}
