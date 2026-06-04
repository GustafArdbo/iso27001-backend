package se.iso27001platform.iso27001backend.onboarding.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;
import se.iso27001platform.iso27001backend.onboarding.enums.ApplicationStatus;
import se.iso27001platform.iso27001backend.onboarding.enums.OwnerInvitationStatus;
import se.iso27001platform.iso27001backend.onboarding.enums.RequestedMaterial;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.user.model.UserProfile;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organization_applications")
public class OrganizationApplication extends BaseEntity {

	@Column(name = "company_name", nullable = false)
	private String companyName;

	@Column(name = "owner_name", nullable = false)
	private String ownerName;

	@Column(name = "owner_email", nullable = false)
	private String ownerEmail;

	@Column(nullable = false)
	private String country;

	private String phone;

	@Column(name = "company_size", nullable = false)
	private String companySize;

	@Column(columnDefinition = "TEXT")
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(name = "application_status", nullable = false)
	private ApplicationStatus applicationStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "invitation_status", nullable = false)
	private OwnerInvitationStatus invitationStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_profile_id")
	private UserProfile ownerProfile;

	@Column(name = "approved_by_supabase_user_id")
	private UUID approvedBySupabaseUserId;

	@Column(name = "approved_at")
	private Instant approvedAt;

	@Column(name = "rejected_by_supabase_user_id")
	private UUID rejectedBySupabaseUserId;

	@Column(name = "rejected_at")
	private Instant rejectedAt;

	@Column(name = "rejection_reason", columnDefinition = "TEXT")
	private String rejectionReason;

	@Column(name = "invitation_sent_at")
	private Instant invitationSentAt;

	@Column(name = "invitation_accepted_at")
	private Instant invitationAcceptedAt;

	@Column(name = "invitation_failure_reason", columnDefinition = "TEXT")
	private String invitationFailureReason;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "organization_application_materials",
			joinColumns = @JoinColumn(name = "organization_application_id")
	)
	@Enumerated(EnumType.STRING)
	@Column(name = "material", nullable = false)
	private Set<RequestedMaterial> materials = new LinkedHashSet<>();

	protected OrganizationApplication() {
	}

	public OrganizationApplication(
			String companyName,
			String ownerName,
			String ownerEmail,
			String country,
			String phone,
			String companySize,
			String message,
			Set<RequestedMaterial> materials
	) {
		this.companyName = companyName;
		this.ownerName = ownerName;
		this.ownerEmail = ownerEmail;
		this.country = country;
		this.phone = phone;
		this.companySize = companySize;
		this.message = message;
		this.applicationStatus = ApplicationStatus.SUBMITTED;
		this.invitationStatus = OwnerInvitationStatus.NOT_SENT;
		this.materials = new LinkedHashSet<>(materials);
	}

	public void approve(Organization organization, UserProfile ownerProfile, UUID approvedBySupabaseUserId, Instant approvedAt) {
		this.organization = organization;
		this.ownerProfile = ownerProfile;
		this.approvedBySupabaseUserId = approvedBySupabaseUserId;
		this.approvedAt = approvedAt;
		this.applicationStatus = ApplicationStatus.APPROVED;
	}

	public void reject(UUID rejectedBySupabaseUserId, Instant rejectedAt, String rejectionReason) {
		this.rejectedBySupabaseUserId = rejectedBySupabaseUserId;
		this.rejectedAt = rejectedAt;
		this.rejectionReason = rejectionReason;
		this.applicationStatus = ApplicationStatus.REJECTED;
	}

	public void markInvitationSent(Instant invitationSentAt) {
		this.invitationStatus = OwnerInvitationStatus.SENT;
		this.invitationSentAt = invitationSentAt;
		this.invitationFailureReason = null;
	}

	public void markInvitationFailed(String reason) {
		this.invitationStatus = OwnerInvitationStatus.FAILED;
		this.invitationFailureReason = reason;
	}

	public void markInvitationAccepted(Instant invitationAcceptedAt) {
		this.invitationStatus = OwnerInvitationStatus.ACCEPTED;
		this.invitationAcceptedAt = invitationAcceptedAt;
		this.invitationFailureReason = null;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public String getCountry() {
		return country;
	}

	public String getPhone() {
		return phone;
	}

	public String getCompanySize() {
		return companySize;
	}

	public String getMessage() {
		return message;
	}

	public ApplicationStatus getApplicationStatus() {
		return applicationStatus;
	}

	public OwnerInvitationStatus getInvitationStatus() {
		return invitationStatus;
	}

	public Organization getOrganization() {
		return organization;
	}

	public UserProfile getOwnerProfile() {
		return ownerProfile;
	}

	public UUID getApprovedBySupabaseUserId() {
		return approvedBySupabaseUserId;
	}

	public Instant getApprovedAt() {
		return approvedAt;
	}

	public UUID getRejectedBySupabaseUserId() {
		return rejectedBySupabaseUserId;
	}

	public Instant getRejectedAt() {
		return rejectedAt;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public Instant getInvitationSentAt() {
		return invitationSentAt;
	}

	public Instant getInvitationAcceptedAt() {
		return invitationAcceptedAt;
	}

	public String getInvitationFailureReason() {
		return invitationFailureReason;
	}

	public Set<RequestedMaterial> getMaterials() {
		return materials;
	}
}
