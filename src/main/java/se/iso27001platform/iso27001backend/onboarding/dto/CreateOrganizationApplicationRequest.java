package se.iso27001platform.iso27001backend.onboarding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrganizationApplicationRequest(
		@NotBlank @Size(max = 200) String company,
		@NotBlank @Size(max = 200) String name,
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(max = 100) String country,
		@Size(max = 50) String phone,
		@NotBlank @Size(max = 20) String size,
		@Size(max = 4000) String message,
		@NotNull @Size(max = 3) List<@NotBlank String> materials
) {

	public CreateOrganizationApplicationRequest {
		company = trim(company);
		name = trim(name);
		email = trim(email);
		country = trim(country);
		phone = trim(phone);
		size = trim(size);
		message = trim(message);
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}
}
