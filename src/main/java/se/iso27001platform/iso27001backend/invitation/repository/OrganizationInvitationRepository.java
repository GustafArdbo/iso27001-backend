package se.iso27001platform.iso27001backend.invitation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.invitation.enums.InvitationStatus;
import se.iso27001platform.iso27001backend.invitation.model.OrganizationInvitation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, UUID> {

	Optional<OrganizationInvitation> findByTokenHash(String tokenHash);

	Optional<OrganizationInvitation> findByOrganization_IdAndId(UUID organizationId, UUID id);

	Optional<OrganizationInvitation> findFirstByOrganization_IdAndEmailAndStatusOrderByCreatedAtDesc(
			UUID organizationId,
			String email,
			InvitationStatus status
	);

	List<OrganizationInvitation> findByOrganization_IdOrderByCreatedAtDesc(UUID organizationId);
}
