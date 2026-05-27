package se.iso27001platform.iso27001backend.auth.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.auth.dto.RevocationResponse;
import se.iso27001platform.iso27001backend.auth.enums.AuthRevocationType;
import se.iso27001platform.iso27001backend.auth.model.AuthRevocation;
import se.iso27001platform.iso27001backend.auth.repository.AuthRevocationRepository;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class AuthRevocationService {

	private final AuthRevocationRepository authRevocationRepository;

	public AuthRevocationService(AuthRevocationRepository authRevocationRepository) {
		this.authRevocationRepository = authRevocationRepository;
	}

	@Transactional(readOnly = true)
	public boolean isRevoked(Jwt jwt) {
		return authRevocationRepository.existsActiveRevocation(
				hashToken(jwt.getTokenValue()),
				jwt.getId(),
				sessionId(jwt),
				Instant.now()
		);
	}

	public RevocationResponse revokeToken(Jwt jwt, String reason) {
		UUID sessionId = sessionId(jwt);
		AuthRevocation revocation = authRevocationRepository.save(AuthRevocation.token(
				hashToken(jwt.getTokenValue()),
				jwt.getId(),
				sessionId,
				jwt.getSubject(),
				normalizeReason(reason),
				jwt.getExpiresAt()
		));

		return new RevocationResponse(
				AuthRevocationType.TOKEN,
				revocation.getSubject(),
				revocation.getJwtId(),
				revocation.getSessionId(),
				revocation.getExpiresAt()
		);
	}

	public RevocationResponse revokeSession(Jwt jwt, String reason) {
		UUID sessionId = sessionId(jwt);
		if (sessionId == null) {
			throw new ResourceNotFoundException("JWT does not contain a session_id claim");
		}

		AuthRevocation revocation = authRevocationRepository.save(AuthRevocation.session(
				sessionId,
				jwt.getSubject(),
				normalizeReason(reason)
		));

		return new RevocationResponse(
				AuthRevocationType.SESSION,
				revocation.getSubject(),
				null,
				revocation.getSessionId(),
				revocation.getExpiresAt()
		);
	}

	private UUID sessionId(Jwt jwt) {
		String sessionId = jwt.getClaimAsString("session_id");
		return sessionId == null || sessionId.isBlank() ? null : UUID.fromString(sessionId);
	}

	private String normalizeReason(String reason) {
		return reason == null || reason.isBlank() ? null : reason.trim();
	}

	private String hashToken(String tokenValue) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(tokenValue.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
