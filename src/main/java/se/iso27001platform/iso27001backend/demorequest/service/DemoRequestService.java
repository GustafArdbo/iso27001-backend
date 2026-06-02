package se.iso27001platform.iso27001backend.demorequest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.common.exception.BadRequestException;
import se.iso27001platform.iso27001backend.demorequest.dto.CreateDemoRequestRequest;
import se.iso27001platform.iso27001backend.demorequest.dto.DemoRequestResponse;
import se.iso27001platform.iso27001backend.demorequest.enums.DemoRequestMaterial;
import se.iso27001platform.iso27001backend.demorequest.model.DemoRequest;
import se.iso27001platform.iso27001backend.demorequest.repository.DemoRequestRepository;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class DemoRequestService {

	private static final Set<String> COMPANY_SIZES = Set.of("1-10", "11-50", "51-200", "201-500", "500+");

	private final DemoRequestRepository demoRequestRepository;

	public DemoRequestService(DemoRequestRepository demoRequestRepository) {
		this.demoRequestRepository = demoRequestRepository;
	}

	public DemoRequestResponse create(CreateDemoRequestRequest request) {
		String companySize = request.size().trim();
		if (!COMPANY_SIZES.contains(companySize)) {
			throw new BadRequestException("Unsupported company size: " + companySize);
		}

		Set<DemoRequestMaterial> materials = new LinkedHashSet<>();
		for (String material : request.materials()) {
			if (!materials.add(DemoRequestMaterial.fromClientValue(material))) {
				throw new BadRequestException("Duplicate demo request material: " + material);
			}
		}

		DemoRequest demoRequest = demoRequestRepository.save(new DemoRequest(
				request.company().trim(),
				request.name().trim(),
				request.email().trim().toLowerCase(Locale.ROOT),
				request.country().trim(),
				normalizeOptional(request.phone()),
				companySize,
				normalizeOptional(request.message()),
				materials
		));

		return DemoRequestResponse.from(demoRequest);
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
