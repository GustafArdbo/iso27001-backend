package se.iso27001platform.iso27001backend.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.user.enums.UserRole;

import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@Column(nullable = false)
	private String email;

	@Column(name = "supabase_user_id")
	private UUID supabaseUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	protected AppUser() {
	}

	public AppUser(Organization organization, String email, UUID supabaseUserId, UserRole role) {
		this.organization = organization;
		this.email = email;
		this.supabaseUserId = supabaseUserId;
		this.role = role;
	}

	public Organization getOrganization() {
		return organization;
	}

	public String getEmail() {
		return email;
	}

	public UUID getSupabaseUserId() {
		return supabaseUserId;
	}

	public UserRole getRole() {
		return role;
	}
}
