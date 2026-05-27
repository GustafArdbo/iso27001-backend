package se.iso27001platform.iso27001backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import se.iso27001platform.iso27001backend.auth.model.AuthRevocation;

import java.time.Instant;
import java.util.UUID;

public interface AuthRevocationRepository extends JpaRepository<AuthRevocation, UUID> {

	@Query("""
			select count(revocation) > 0
			from AuthRevocation revocation
			where (
				(:tokenHash is not null and revocation.tokenHash = :tokenHash)
				or (:jwtId is not null and revocation.jwtId = :jwtId)
				or (:sessionId is not null and revocation.sessionId = :sessionId)
			)
			and (revocation.expiresAt is null or revocation.expiresAt > :now)
			""")
	boolean existsActiveRevocation(String tokenHash, String jwtId, UUID sessionId, Instant now);
}
