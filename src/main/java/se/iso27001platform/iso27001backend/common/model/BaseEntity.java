package se.iso27001platform.iso27001backend.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	protected void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
