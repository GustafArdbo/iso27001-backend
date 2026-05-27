package se.iso27001platform.iso27001backend.invitation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.invitation.dto.AcceptInvitationRequest;
import se.iso27001platform.iso27001backend.invitation.dto.AcceptInvitationResponse;
import se.iso27001platform.iso27001backend.invitation.dto.CreateInvitationRequest;
import se.iso27001platform.iso27001backend.invitation.dto.CreateInvitationResponse;
import se.iso27001platform.iso27001backend.invitation.dto.InvitationResponse;
import se.iso27001platform.iso27001backend.invitation.service.InvitationService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class InvitationController {

	private final InvitationService invitationService;

	public InvitationController(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@PostMapping("/organizations/{organizationId}/invitations")
	public ResponseEntity<CreateInvitationResponse> create(
			@PathVariable UUID organizationId,
			@Valid @RequestBody CreateInvitationRequest request
	) {
		CreateInvitationResponse response = invitationService.create(organizationId, request);
		return ResponseEntity
				.created(URI.create("/organizations/" + organizationId + "/invitations/" + response.invitation().id()))
				.body(response);
	}

	@GetMapping("/organizations/{organizationId}/invitations")
	public List<InvitationResponse> findByOrganization(@PathVariable UUID organizationId) {
		return invitationService.findByOrganization(organizationId);
	}

	@PostMapping("/invitations/accept")
	public AcceptInvitationResponse accept(@Valid @RequestBody AcceptInvitationRequest request) {
		return invitationService.accept(request.token());
	}

	@DeleteMapping("/organizations/{organizationId}/invitations/{invitationId}")
	public ResponseEntity<Void> revoke(@PathVariable UUID organizationId, @PathVariable UUID invitationId) {
		invitationService.revoke(organizationId, invitationId);
		return ResponseEntity.noContent().build();
	}
}
