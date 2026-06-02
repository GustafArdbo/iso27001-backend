package se.iso27001platform.iso27001backend.demorequest.enums;

import se.iso27001platform.iso27001backend.common.exception.BadRequestException;

import java.util.Arrays;
import java.util.Locale;

public enum DemoRequestMaterial {
	STANDARD_FORMS("standard-forms"),
	CHECKLIST("checklist"),
	GAP_ANALYSIS("gap-analysis");

	private final String clientValue;

	DemoRequestMaterial(String clientValue) {
		this.clientValue = clientValue;
	}

	public String getClientValue() {
		return clientValue;
	}

	public static DemoRequestMaterial fromClientValue(String value) {
		String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
		return Arrays.stream(values())
				.filter(material -> material.clientValue.equals(normalizedValue))
				.findFirst()
				.orElseThrow(() -> new BadRequestException("Unsupported demo request material: " + value));
	}
}
