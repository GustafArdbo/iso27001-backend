package se.iso27001platform.iso27001backend.assessment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentAnswerResponse;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentQuestionResponse;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentResponse;
import se.iso27001platform.iso27001backend.assessment.dto.AssessmentSummaryResponse;
import se.iso27001platform.iso27001backend.assessment.dto.CreateAssessmentRequest;
import se.iso27001platform.iso27001backend.assessment.dto.SubmitAnswerRequest;
import se.iso27001platform.iso27001backend.assessment.enums.AnswerStatus;
import se.iso27001platform.iso27001backend.assessment.model.Assessment;
import se.iso27001platform.iso27001backend.assessment.model.AssessmentAnswer;
import se.iso27001platform.iso27001backend.assessment.repository.AssessmentAnswerRepository;
import se.iso27001platform.iso27001backend.assessment.repository.AssessmentRepository;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.control.model.ControlDefinition;
import se.iso27001platform.iso27001backend.control.service.ControlCatalogService;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.service.OrganizationService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AssessmentService {

	private final AssessmentRepository assessmentRepository;
	private final AssessmentAnswerRepository assessmentAnswerRepository;
	private final OrganizationService organizationService;
	private final ControlCatalogService controlCatalogService;

	public AssessmentService(
			AssessmentRepository assessmentRepository,
			AssessmentAnswerRepository assessmentAnswerRepository,
			OrganizationService organizationService,
			ControlCatalogService controlCatalogService
	) {
		this.assessmentRepository = assessmentRepository;
		this.assessmentAnswerRepository = assessmentAnswerRepository;
		this.organizationService = organizationService;
		this.controlCatalogService = controlCatalogService;
	}

	public AssessmentResponse create(CreateAssessmentRequest request) {
		Organization organization = organizationService.getRequired(request.organizationId());
		Assessment assessment = assessmentRepository.save(new Assessment(organization, request.name()));
		return AssessmentResponse.from(assessment);
	}

	@Transactional(readOnly = true)
	public AssessmentResponse findById(UUID id) {
		return AssessmentResponse.from(getRequired(id));
	}

	@Transactional(readOnly = true)
	public List<AssessmentQuestionResponse> findQuestions(UUID id) {
		getRequired(id);
		Map<String, AssessmentAnswer> answersByControlId = assessmentAnswerRepository.findByAssessment_Id(id).stream()
				.collect(java.util.stream.Collectors.toMap(AssessmentAnswer::getControlId, answer -> answer));

		return controlCatalogService.findAll().stream()
				.map(control -> AssessmentQuestionResponse.from(control, answersByControlId.get(control.id())))
				.toList();
	}

	public AssessmentAnswerResponse submitAnswer(UUID assessmentId, SubmitAnswerRequest request) {
		Assessment assessment = getRequired(assessmentId);
		String controlId = controlCatalogService.getRequired(request.controlId()).id();
		AssessmentAnswer assessmentAnswer = assessmentAnswerRepository
				.findByAssessment_IdAndControlId(assessmentId, controlId)
				.orElseGet(() -> new AssessmentAnswer(assessment, controlId, request.answer(), request.comment()));

		assessmentAnswer.updateAnswer(request.answer(), request.comment());
		assessment.markInProgress();

		return AssessmentAnswerResponse.from(assessmentAnswerRepository.save(assessmentAnswer));
	}

	@Transactional(readOnly = true)
	public AssessmentSummaryResponse summarize(UUID id) {
		Assessment assessment = getRequired(id);
		List<AssessmentAnswer> answers = assessmentAnswerRepository.findByAssessment_Id(id);
		List<ControlDefinition> controls = controlCatalogService.findAll();
		Map<AnswerStatus, Long> answerCounts = countAnswers(answers);
		int totalControls = controls.size();
		int answeredControls = answers.size();
		int unansweredControls = Math.max(totalControls - answeredControls, 0);
		int completionPercentage = percentage(answeredControls, totalControls);

		double totalScore = answers.stream()
				.filter(answer -> answer.getAnswer() != AnswerStatus.NOT_APPLICABLE)
				.mapToDouble(answer -> scoreFor(answer.getAnswer()))
				.sum();
		int applicableAnswers = (int) answers.stream()
				.filter(answer -> answer.getAnswer() != AnswerStatus.NOT_APPLICABLE)
				.count();
		double score = applicableAnswers == 0 ? 0.0 : totalScore / applicableAnswers;
		int scorePercentage = (int) Math.round(score * 100);

		return new AssessmentSummaryResponse(
				assessment.getId(),
				assessment.getOrganization().getId(),
				assessment.getName(),
				assessment.getStatus(),
				totalControls,
				answeredControls,
				unansweredControls,
				completionPercentage,
				answers.size(),
				applicableAnswers,
				score,
				scorePercentage,
				100 - scorePercentage,
				answerCounts
		);
	}

	private Assessment getRequired(UUID id) {
		return assessmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + id));
	}

	private Map<AnswerStatus, Long> countAnswers(List<AssessmentAnswer> answers) {
		Map<AnswerStatus, Long> counts = new EnumMap<>(AnswerStatus.class);
		for (AnswerStatus status : AnswerStatus.values()) {
			counts.put(status, 0L);
		}
		for (AssessmentAnswer answer : answers) {
			counts.compute(answer.getAnswer(), (status, count) -> count == null ? 1L : count + 1L);
		}
		return counts;
	}

	private double scoreFor(AnswerStatus answerStatus) {
		return switch (answerStatus) {
			case YES -> 1.0;
			case PARTIAL -> 0.5;
			case NO, NOT_APPLICABLE -> 0.0;
		};
	}

	private int percentage(int value, int total) {
		return total == 0 ? 0 : (int) Math.round((double) value / total * 100);
	}
}
