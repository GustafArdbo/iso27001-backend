package se.iso27001platform.iso27001backend.user.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.user.dto.CreateUserRequest;
import se.iso27001platform.iso27001backend.user.dto.UserResponse;
import se.iso27001platform.iso27001backend.user.service.UserService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/organizations/{organizationId}/users")
	public ResponseEntity<UserResponse> create(
			@PathVariable UUID organizationId,
			@Valid @RequestBody CreateUserRequest request
	) {
		UserResponse response = userService.create(organizationId, request);
		return ResponseEntity.created(URI.create("/users/" + response.id())).body(response);
	}

	@GetMapping("/organizations/{organizationId}/users")
	public List<UserResponse> findByOrganization(@PathVariable UUID organizationId) {
		return userService.findByOrganization(organizationId);
	}

	@GetMapping("/users/{id}")
	public UserResponse findById(@PathVariable UUID id) {
		return userService.findById(id);
	}
}
