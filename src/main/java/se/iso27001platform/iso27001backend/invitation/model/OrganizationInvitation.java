package se.iso27001platform.iso27001backend.invitation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;
import se.iso27001platform.iso27001backend.invitation.enums.InvitationStatus;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.user.enums.UserRole;
import se.iso27001platform.iso27001backend.user.model.AppUser;

import java.time.Instant;

@Entity
@Table(name = "organization_invitations")
public class OrganizationInvitation extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@Column(nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	@Column(name = "token_hash", nullable = false, updatable = false)
	private String tokenHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InvitationStatus status;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "accepted_at")
	private Instant acceptedAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invited_by_user_id", nullable = false)
	private AppUser invitedByUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accepted_by_user_id")
	private AppUser acceptedByUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "revoked_by_user_id")
	private AppUser revokedByUser;

	protected OrganizationInvitation() {
	}

	public OrganizationInvitation(
			Organization organization,
			String email,
			UserRole role,
			String tokenHash,
			Instant expiresAt,
			AppUser invitedByUser
	) {
		this.organization = organization;
		this.email = email;
		this.role = role;
		this.tokenHash = tokenHash;
		this.status = InvitationStatus.PENDING;
		this.expiresAt = expiresAt;
		this.invitedByUser = invitedByUser;
	}

	public Organization getOrganization() {
		return organization;
	}

	public String getEmail() {
		return email;
	}

	public UserRole getRole() {
		return role;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public InvitationStatus getStatus() {
		return status;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getAcceptedAt() {
		return acceptedAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public AppUser getInvitedByUser() {
		return invitedByUser;
	}

	public AppUser getAcceptedByUser() {
		return acceptedByUser;
	}

	public AppUser getRevokedByUser() {
		return revokedByUser;
	}

	public boolean isExpiredAt(Instant now) {
		return status == InvitationStatus.PENDING && !expiresAt.isAfter(now);
	}

	public void markAccepted(AppUser acceptedByUser, Instant acceptedAt) {
		this.status = InvitationStatus.ACCEPTED;
		this.acceptedByUser = acceptedByUser;
		this.acceptedAt = acceptedAt;
	}

	public void markRevoked(AppUser revokedByUser, Instant revokedAt) {
		this.status = InvitationStatus.REVOKED;
		this.revokedByUser = revokedByUser;
		this.revokedAt = revokedAt;
	}

	public void markExpired() {
		this.status = InvitationStatus.EXPIRED;
	}
}
