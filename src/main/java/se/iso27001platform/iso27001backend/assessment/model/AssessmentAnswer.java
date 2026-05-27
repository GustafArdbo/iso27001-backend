package se.iso27001platform.iso27001backend.assessment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import se.iso27001platform.iso27001backend.assessment.enums.AnswerStatus;
import se.iso27001platform.iso27001backend.common.model.BaseEntity;

@Entity
@Table(name = "assessment_answers")
public class AssessmentAnswer extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "assessment_id", nullable = false)
	private Assessment assessment;

	@Column(name = "control_id", nullable = false)
	private String controlId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnswerStatus answer;

	@Column(columnDefinition = "TEXT")
	private String comment;

	protected AssessmentAnswer() {
	}

	public AssessmentAnswer(Assessment assessment, String controlId, AnswerStatus answer, String comment) {
		this.assessment = assessment;
		this.controlId = controlId;
		this.answer = answer;
		this.comment = comment;
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public String getControlId() {
		return controlId;
	}

	public AnswerStatus getAnswer() {
		return answer;
	}

	public String getComment() {
		return comment;
	}

	public void updateAnswer(AnswerStatus answer, String comment) {
		this.answer = answer;
		this.comment = comment;
	}
}
