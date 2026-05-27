package se.iso27001platform.iso27001backend.assessment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.assessment.enums.AssessmentStatus;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;
import se.iso27001platform.iso27001backend.organization.model.Organization;

@Entity
@Table(name = "assessments")
public class Assessment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AssessmentStatus status = AssessmentStatus.DRAFT;

	protected Assessment() {
	}

	public Assessment(Organization organization, String name) {
		this.organization = organization;
		this.name = name;
	}

	public Organization getOrganization() {
		return organization;
	}

	public String getName() {
		return name;
	}

	public AssessmentStatus getStatus() {
		return status;
	}

	public void markInProgress() {
		if (status == AssessmentStatus.DRAFT) {
			status = AssessmentStatus.IN_PROGRESS;
		}
	}
}
