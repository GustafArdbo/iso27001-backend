package se.iso27001platform.iso27001backend.common.exception;

import java.time.Instant;

public record ApiError(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path
) {
}
