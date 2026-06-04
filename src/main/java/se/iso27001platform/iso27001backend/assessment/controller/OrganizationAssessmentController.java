package se.iso27001platform.iso27001backend.assessment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentResponse;
import se.iso27001platform.iso27001backend.assessment.service.AssessmentService;

import java.util.List;
import java.util.UUID;

@RestController
public class OrganizationAssessmentController {

	private final AssessmentService assessmentService;

	public OrganizationAssessmentController(AssessmentService assessmentService) {
		this.assessmentService = assessmentService;
	}

	@GetMapping("/organizations/{organizationId}/assessments")
	public List<AssessmentResponse> findByOrganization(@PathVariable UUID organizationId) {
		return assessmentService.findByOrganization(organizationId);
	}
}
