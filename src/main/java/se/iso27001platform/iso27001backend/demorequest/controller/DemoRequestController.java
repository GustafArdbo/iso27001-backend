package se.iso27001platform.iso27001backend.demorequest.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.demorequest.dto.CreateDemoRequestRequest;
import se.iso27001platform.iso27001backend.demorequest.dto.DemoRequestResponse;
import se.iso27001platform.iso27001backend.demorequest.service.DemoRequestService;

import java.net.URI;

@RestController
public class DemoRequestController {

	private final DemoRequestService demoRequestService;

	public DemoRequestController(DemoRequestService demoRequestService) {
		this.demoRequestService = demoRequestService;
	}

	@PostMapping("/demo-requests")
	public ResponseEntity<DemoRequestResponse> create(@Valid @RequestBody CreateDemoRequestRequest request) {
		DemoRequestResponse response = demoRequestService.create(request);
		return ResponseEntity.created(URI.create("/demo-requests/" + response.id())).body(response);
	}
}
