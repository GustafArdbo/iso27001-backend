package se.iso27001platform.iso27001backend.organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;

@Entity
@Table(name = "organizations")
public class Organization extends BaseEntity {

	@Column(nullable = false)
	private String name;

	protected Organization() {
	}

	public Organization(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
