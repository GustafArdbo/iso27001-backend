package se.iso27001platform.iso27001backend.demorequest.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;
import se.iso27001platform.iso27001backend.demorequest.enums.DemoRequestMaterial;
import se.iso27001platform.iso27001backend.demorequest.enums.DemoRequestStatus;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "demo_requests")
public class DemoRequest extends BaseEntity {

	@Column(name = "company_name", nullable = false)
	private String companyName;

	@Column(name = "contact_name", nullable = false)
	private String contactName;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String country;

	private String phone;

	@Column(name = "company_size", nullable = false)
	private String companySize;

	@Column(columnDefinition = "TEXT")
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DemoRequestStatus status;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "demo_request_materials", joinColumns = @JoinColumn(name = "demo_request_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "material", nullable = false)
	private Set<DemoRequestMaterial> materials = new LinkedHashSet<>();

	protected DemoRequest() {
	}

	public DemoRequest(
			String companyName,
			String contactName,
			String email,
			String country,
			String phone,
			String companySize,
			String message,
			Set<DemoRequestMaterial> materials
	) {
		this.companyName = companyName;
		this.contactName = contactName;
		this.email = email;
		this.country = country;
		this.phone = phone;
		this.companySize = companySize;
		this.message = message;
		this.status = DemoRequestStatus.NEW;
		this.materials = new LinkedHashSet<>(materials);
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getContactName() {
		return contactName;
	}

	public String getEmail() {
		return email;
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

	public DemoRequestStatus getStatus() {
		return status;
	}

	public Set<DemoRequestMaterial> getMaterials() {
		return materials;
	}
}
