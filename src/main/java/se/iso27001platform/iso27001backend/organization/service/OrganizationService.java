package se.iso27001platform.iso27001backend.organization.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.repository.OrganizationRepository;
import se.iso27001platform.iso27001backend.organization.dto.CreateOrganizationRequest;
import se.iso27001platform.iso27001backend.organization.dto.OrganizationResponse;

import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

	private final OrganizationRepository organizationRepository;

	public OrganizationService(OrganizationRepository organizationRepository) {
		this.organizationRepository = organizationRepository;
	}

	public OrganizationResponse create(CreateOrganizationRequest request) {
		Organization organization = organizationRepository.save(new Organization(request.name()));
		return OrganizationResponse.from(organization);
	}

	@Transactional(readOnly = true)
	public OrganizationResponse findById(UUID id) {
		return OrganizationResponse.from(getRequired(id));
	}

	@Transactional(readOnly = true)
	public Organization getRequired(UUID id) {
		return organizationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
	}
}
