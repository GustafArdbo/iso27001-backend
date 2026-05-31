package se.iso27001platform.iso27001backend.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

	@Column(name = "supabase_user_id")
	private UUID supabaseUserId;

	@Column(nullable = false)
	private String email;

	protected UserProfile() {
	}

	public UserProfile(UUID supabaseUserId, String email) {
		this.supabaseUserId = supabaseUserId;
		this.email = email;
	}

	public UUID getSupabaseUserId() {
		return supabaseUserId;
	}

	public String getEmail() {
		return email;
	}

	public void linkSupabaseIdentity(UUID supabaseUserId) {
		this.supabaseUserId = supabaseUserId;
	}

	public void updateEmail(String email) {
		this.email = email;
	}
}
