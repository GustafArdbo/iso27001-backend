package se.iso27001platform.iso27001backend.organization.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.organization.dto.CreateOrganizationRequest;
import se.iso27001platform.iso27001backend.organization.dto.OrganizationResponse;
import se.iso27001platform.iso27001backend.organization.service.OrganizationService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

	private final OrganizationService organizationService;

	public OrganizationController(OrganizationService organizationService) {
		this.organizationService = organizationService;
	}

	@PostMapping
	public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
		OrganizationResponse response = organizationService.create(request);
		return ResponseEntity.created(URI.create("/organizations/" + response.id())).body(response);
	}

	@GetMapping("/{id}")
	public OrganizationResponse findById(@PathVariable UUID id) {
		return organizationService.findById(id);
	}
}
