package se.iso27001platform.iso27001backend.onboarding.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.iso27001platform.iso27001backend.onboarding.enums.ApplicationStatus;
import se.iso27001platform.iso27001backend.onboarding.model.OrganizationApplication;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationApplicationRepository extends JpaRepository<OrganizationApplication, UUID> {

	boolean existsByOwnerEmailAndApplicationStatusIn(String ownerEmail, Collection<ApplicationStatus> statuses);

	List<OrganizationApplication> findAllByOrderByCreatedAtDesc();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select application from OrganizationApplication application where application.id = :id")
	Optional<OrganizationApplication> findByIdForUpdate(@Param("id") UUID id);

	List<OrganizationApplication> findByOwnerProfile_IdAndApplicationStatus(
			UUID ownerProfileId,
			ApplicationStatus applicationStatus
	);
}
