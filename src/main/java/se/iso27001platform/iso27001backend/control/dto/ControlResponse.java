package se.iso27001platform.iso27001backend.control.dto;

import se.iso27001platform.iso27001backend.control.enums.ControlDomain;
import se.iso27001platform.iso27001backend.control.model.ControlDefinition;

public record ControlResponse(
		String id,
		ControlDomain domain,
		String title,
		String question,
		int sortOrder
) {

	public static ControlResponse from(ControlDefinition controlDefinition) {
		return new ControlResponse(
				controlDefinition.id(),
				controlDefinition.domain(),
				controlDefinition.title(),
				controlDefinition.question(),
				controlDefinition.sortOrder()
		);
	}
}
