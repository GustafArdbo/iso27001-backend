package se.iso27001platform.iso27001backend.auth.dto;

import se.iso27001platform.iso27001backend.auth.enums.AuthRevocationType;

import java.time.Instant;
import java.util.UUID;

public record RevocationResponse(
		AuthRevocationType type,
		String subject,
		String jwtId,
		UUID sessionId,
		Instant expiresAt
) {
}
