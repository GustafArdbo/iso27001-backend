package se.iso27001platform.iso27001backend.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, UUID> {

	boolean existsByOrganization_IdAndUserProfile_Email(UUID organizationId, String email);

	boolean existsByOrganization_IdAndUserProfile_SupabaseUserId(UUID organizationId, UUID supabaseUserId);

	List<OrganizationMembership> findByOrganization_IdOrderByCreatedAtAsc(UUID organizationId);

	List<OrganizationMembership> findByUserProfile_SupabaseUserIdOrderByCreatedAtAsc(UUID supabaseUserId);

	Optional<OrganizationMembership> findByOrganization_IdAndUserProfile_SupabaseUserId(
			UUID organizationId,
			UUID supabaseUserId
	);
}
