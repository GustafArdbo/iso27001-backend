package se.iso27001platform.iso27001backend.membership.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.membership.dto.CreateMembershipRequest;
import se.iso27001platform.iso27001backend.membership.dto.MembershipResponse;
import se.iso27001platform.iso27001backend.membership.service.MembershipService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class MembershipController {

	private final MembershipService membershipService;

	public MembershipController(MembershipService membershipService) {
		this.membershipService = membershipService;
	}

	@PostMapping("/organizations/{organizationId}/memberships")
	public ResponseEntity<MembershipResponse> create(
			@PathVariable UUID organizationId,
			@Valid @RequestBody CreateMembershipRequest request
	) {
		MembershipResponse response = membershipService.create(organizationId, request);
		return ResponseEntity.created(URI.create("/memberships/" + response.id())).body(response);
	}

	@GetMapping("/organizations/{organizationId}/memberships")
	public List<MembershipResponse> findByOrganization(@PathVariable UUID organizationId) {
		return membershipService.findByOrganization(organizationId);
	}

	@GetMapping("/memberships/{id}")
	public MembershipResponse findById(@PathVariable UUID id) {
		return membershipService.findById(id);
	}
}
