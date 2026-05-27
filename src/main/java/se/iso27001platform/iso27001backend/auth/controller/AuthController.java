package se.iso27001platform.iso27001backend.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.auth.dto.AuthUserResponse;
import se.iso27001platform.iso27001backend.auth.dto.RevokeCurrentTokenRequest;
import se.iso27001platform.iso27001backend.auth.dto.RevocationResponse;
import se.iso27001platform.iso27001backend.auth.service.AuthCurrentUserService;
import se.iso27001platform.iso27001backend.auth.service.AuthRevocationService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthCurrentUserService authCurrentUserService;
	private final AuthRevocationService authRevocationService;

	public AuthController(AuthCurrentUserService authCurrentUserService, AuthRevocationService authRevocationService) {
		this.authCurrentUserService = authCurrentUserService;
		this.authRevocationService = authRevocationService;
	}

	@GetMapping("/me")
	public AuthUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
		return authCurrentUserService.currentUser(jwt);
	}

	@PostMapping("/revocations/current-token")
	public RevocationResponse revokeCurrentToken(
			@AuthenticationPrincipal Jwt jwt,
			@RequestBody(required = false) RevokeCurrentTokenRequest request
	) {
		return authRevocationService.revokeToken(jwt, request == null ? null : request.reason());
	}

	@PostMapping("/revocations/current-session")
	public RevocationResponse revokeCurrentSession(
			@AuthenticationPrincipal Jwt jwt,
			@RequestBody(required = false) RevokeCurrentTokenRequest request
	) {
		return authRevocationService.revokeSession(jwt, request == null ? null : request.reason());
	}
}
