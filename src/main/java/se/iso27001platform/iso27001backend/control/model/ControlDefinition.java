package se.iso27001platform.iso27001backend.control.model;

import se.iso27001platform.iso27001backend.control.enums.ControlDomain;

public record ControlDefinition(
		String id,
		ControlDomain domain,
		String title,
		String question,
		int sortOrder
) {
}
