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
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;
import se.iso27001platform.iso27001backend.organization.model.Organization;

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
	private MembershipRole role;

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
	@JoinColumn(name = "invited_by_membership_id", nullable = false)
	private OrganizationMembership invitedByMembership;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accepted_by_membership_id")
	private OrganizationMembership acceptedByMembership;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "revoked_by_membership_id")
	private OrganizationMembership revokedByMembership;

	protected OrganizationInvitation() {
	}

	public OrganizationInvitation(
			Organization organization,
			String email,
			MembershipRole role,
			String tokenHash,
			Instant expiresAt,
			OrganizationMembership invitedByMembership
	) {
		this.organization = organization;
		this.email = email;
		this.role = role;
		this.tokenHash = tokenHash;
		this.status = InvitationStatus.PENDING;
		this.expiresAt = expiresAt;
		this.invitedByMembership = invitedByMembership;
	}

	public Organization getOrganization() {
		return organization;
	}

	public String getEmail() {
		return email;
	}

	public MembershipRole getRole() {
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

	public OrganizationMembership getInvitedByMembership() {
		return invitedByMembership;
	}

	public OrganizationMembership getAcceptedByMembership() {
		return acceptedByMembership;
	}

	public OrganizationMembership getRevokedByMembership() {
		return revokedByMembership;
	}

	public boolean isExpiredAt(Instant now) {
		return status == InvitationStatus.PENDING && !expiresAt.isAfter(now);
	}

	public void markAccepted(OrganizationMembership acceptedByMembership, Instant acceptedAt) {
		this.status = InvitationStatus.ACCEPTED;
		this.acceptedByMembership = acceptedByMembership;
		this.acceptedAt = acceptedAt;
	}

	public void markRevoked(OrganizationMembership revokedByMembership, Instant revokedAt) {
		this.status = InvitationStatus.REVOKED;
		this.revokedByMembership = revokedByMembership;
		this.revokedAt = revokedAt;
	}

	public void markExpired() {
		this.status = InvitationStatus.EXPIRED;
	}
}
