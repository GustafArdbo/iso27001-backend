package se.iso27001platform.iso27001backend.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.auth.enums.AuthRevocationType;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_revocations")
public class AuthRevocation extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "revocation_type", nullable = false)
	private AuthRevocationType revocationType;

	@Column(name = "token_hash")
	private String tokenHash;

	@Column(name = "jwt_id")
	private String jwtId;

	@Column(name = "session_id")
	private UUID sessionId;

	private String subject;

	@Column(columnDefinition = "TEXT")
	private String reason;

	@Column(name = "expires_at")
	private Instant expiresAt;

	protected AuthRevocation() {
	}

	private AuthRevocation(
			AuthRevocationType revocationType,
			String tokenHash,
			String jwtId,
			UUID sessionId,
			String subject,
			String reason,
			Instant expiresAt
	) {
		this.revocationType = revocationType;
		this.tokenHash = tokenHash;
		this.jwtId = jwtId;
		this.sessionId = sessionId;
		this.subject = subject;
		this.reason = reason;
		this.expiresAt = expiresAt;
	}

	public static AuthRevocation token(String tokenHash, String jwtId, UUID sessionId, String subject, String reason, Instant expiresAt) {
		return new AuthRevocation(AuthRevocationType.TOKEN, tokenHash, jwtId, sessionId, subject, reason, expiresAt);
	}

	public static AuthRevocation session(UUID sessionId, String subject, String reason) {
		return new AuthRevocation(AuthRevocationType.SESSION, null, null, sessionId, subject, reason, null);
	}

	public AuthRevocationType getRevocationType() {
		return revocationType;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public String getJwtId() {
		return jwtId;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public String getSubject() {
		return subject;
	}

	public String getReason() {
		return reason;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
