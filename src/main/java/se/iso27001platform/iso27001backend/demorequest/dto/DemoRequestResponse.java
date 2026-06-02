package se.iso27001platform.iso27001backend.demorequest.dto;

import se.iso27001platform.iso27001backend.demorequest.enums.DemoRequestMaterial;
import se.iso27001platform.iso27001backend.demorequest.enums.DemoRequestStatus;
import se.iso27001platform.iso27001backend.demorequest.model.DemoRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DemoRequestResponse(
		UUID id,
		String company,
		String name,
		String email,
		String country,
		String phone,
		String size,
		String message,
		List<String> materials,
		DemoRequestStatus status,
		Instant createdAt
) {

	public static DemoRequestResponse from(DemoRequest demoRequest) {
		return new DemoRequestResponse(
				demoRequest.getId(),
				demoRequest.getCompanyName(),
				demoRequest.getContactName(),
				demoRequest.getEmail(),
				demoRequest.getCountry(),
				demoRequest.getPhone(),
				demoRequest.getCompanySize(),
				demoRequest.getMessage(),
				demoRequest.getMaterials().stream()
						.map(DemoRequestMaterial::getClientValue)
						.sorted()
						.toList(),
				demoRequest.getStatus(),
				demoRequest.getCreatedAt()
		);
	}
}
