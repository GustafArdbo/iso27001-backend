package se.iso27001platform.iso27001backend.assessment.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentAnswerResponse;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentQuestionResponse;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentResponse;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentSummaryResponse;
import se.iso27001platform.iso27001backend.assessment.dto.CreateAssessmentRequest;
import se.iso27001platform.iso27001backend.assessment.dto.SubmitAnswerRequest;
import se.iso27001platform.iso27001backend.assessment.service.AssessmentService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

	private final AssessmentService assessmentService;

	public AssessmentController(AssessmentService assessmentService) {
		this.assessmentService = assessmentService;
	}

	@PostMapping
	public ResponseEntity<AssessmentResponse> create(@Valid @RequestBody CreateAssessmentRequest request) {
		AssessmentResponse response = assessmentService.create(request);
		return ResponseEntity.created(URI.create("/assessments/" + response.id())).body(response);
	}

	@GetMapping("/{id}")
	public AssessmentResponse findById(@PathVariable UUID id) {
		return assessmentService.findById(id);
	}

	@GetMapping("/{id}/questions")
	public List<AssessmentQuestionResponse> findQuestions(@PathVariable UUID id) {
		return assessmentService.findQuestions(id);
	}

	@PostMapping("/{id}/answers")
	public AssessmentAnswerResponse submitAnswer(
			@PathVariable UUID id,
			@Valid @RequestBody SubmitAnswerRequest request
	) {
		return assessmentService.submitAnswer(id, request);
	}

	@GetMapping("/{id}/summary")
	public AssessmentSummaryResponse summarize(@PathVariable UUID id) {
		return assessmentService.summarize(id);
	}
}
