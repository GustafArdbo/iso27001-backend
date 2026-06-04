package se.iso27001platform.iso27001backend.onboarding.enums;

import se.iso27001platform.iso27001backend.common.exception.BadRequestException;

import java.util.Arrays;
import java.util.Locale;

public enum RequestedMaterial {
	STANDARD_FORMS("standard-forms"),
	CHECKLIST("checklist"),
	GAP_ANALYSIS("gap-analysis");

	private final String clientValue;

	RequestedMaterial(String clientValue) {
		this.clientValue = clientValue;
	}

	public String getClientValue() {
		return clientValue;
	}

	public static RequestedMaterial fromClientValue(String value) {
		String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
		return Arrays.stream(values())
				.filter(material -> material.clientValue.equals(normalizedValue))
				.findFirst()
				.orElseThrow(() -> new BadRequestException("Unsupported requested material: " + value));
	}
}
