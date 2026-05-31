package se.iso27001platform.iso27001backend.membership.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.user.model.UserProfile;

@Entity
@Table(name = "organization_memberships")
public class OrganizationMembership extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_profile_id", nullable = false)
	private UserProfile userProfile;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MembershipRole role;

	protected OrganizationMembership() {
	}

	public OrganizationMembership(Organization organization, UserProfile userProfile, MembershipRole role) {
		this.organization = organization;
		this.userProfile = userProfile;
		this.role = role;
	}

	public Organization getOrganization() {
		return organization;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public MembershipRole getRole() {
		return role;
	}
}
