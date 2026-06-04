package se.iso27001platform.iso27001backend.onboarding.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.onboarding.dto.CreateOrganizationApplicationRequest;
import se.iso27001platform.iso27001backend.onboarding.dto.SubmittedOrganizationApplicationResponse;
import se.iso27001platform.iso27001backend.onboarding.service.OrganizationApplicationService;

import java.net.URI;

@RestController
public class OrganizationApplicationController {

	private final OrganizationApplicationService applicationService;

	public OrganizationApplicationController(OrganizationApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@PostMapping("/organization-applications")
	public ResponseEntity<SubmittedOrganizationApplicationResponse> submit(
			@Valid @RequestBody CreateOrganizationApplicationRequest request
	) {
		SubmittedOrganizationApplicationResponse response = applicationService.submit(request);
		return ResponseEntity.created(URI.create("/organization-applications/" + response.id())).body(response);
	}
}
