package se.iso27001platform.iso27001backend.onboarding.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.onboarding.dto.OrganizationApplicationResponse;
import se.iso27001platform.iso27001backend.onboarding.dto.RejectOrganizationApplicationRequest;
import se.iso27001platform.iso27001backend.onboarding.service.OrganizationApplicationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/organization-applications")
public class AdminOrganizationApplicationController {

	private final OrganizationApplicationService applicationService;

	public AdminOrganizationApplicationController(OrganizationApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@GetMapping
	public List<OrganizationApplicationResponse> findAll() {
		return applicationService.findAll();
	}

	@GetMapping("/{id}")
	public OrganizationApplicationResponse findById(@PathVariable UUID id) {
		return applicationService.findById(id);
	}

	@PostMapping("/{id}/approve")
	public OrganizationApplicationResponse approve(@PathVariable UUID id) {
		return applicationService.approve(id);
	}

	@PostMapping("/{id}/reject")
	public OrganizationApplicationResponse reject(
			@PathVariable UUID id,
			@Valid @RequestBody RejectOrganizationApplicationRequest request
	) {
		return applicationService.reject(id, request.reason());
	}

	@PostMapping("/{id}/resend-owner-invitation")
	public OrganizationApplicationResponse resendOwnerInvitation(@PathVariable UUID id) {
		return applicationService.resendOwnerInvitation(id);
	}
}
