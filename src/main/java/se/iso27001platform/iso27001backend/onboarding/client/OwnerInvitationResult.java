package se.iso27001platform.iso27001backend.onboarding.client;

import java.util.UUID;

public record OwnerInvitationResult(
		UUID supabaseUserId
) {
}
