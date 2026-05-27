package se.iso27001platform.iso27001backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException exception, HttpServletRequest request) {
		return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
		return buildError(HttpStatus.FORBIDDEN, exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));

		return buildError(HttpStatus.BAD_REQUEST, message, request);
	}

	private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request) {
		ApiError apiError = new ApiError(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(apiError);
	}
}
