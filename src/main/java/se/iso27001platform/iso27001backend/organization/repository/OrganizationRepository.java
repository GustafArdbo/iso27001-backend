package se.iso27001platform.iso27001backend.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iso27001platform.iso27001backend.organization.model.Organization;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
