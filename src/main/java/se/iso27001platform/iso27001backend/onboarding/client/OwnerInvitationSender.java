package se.iso27001platform.iso27001backend.onboarding.client;

import java.util.UUID;

public interface OwnerInvitationSender {

	OwnerInvitationResult send(String email, String ownerName, UUID organizationId);
}
