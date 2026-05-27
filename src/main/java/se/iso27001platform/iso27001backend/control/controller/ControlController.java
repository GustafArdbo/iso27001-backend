package se.iso27001platform.iso27001backend.control.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.iso27001platform.iso27001backend.control.dto.ControlResponse;
import se.iso27001platform.iso27001backend.control.enums.ControlDomain;
import se.iso27001platform.iso27001backend.control.service.ControlCatalogService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/controls")
public class ControlController {

	private final ControlCatalogService controlCatalogService;

	public ControlController(ControlCatalogService controlCatalogService) {
		this.controlCatalogService = controlCatalogService;
	}

	@GetMapping
	public List<ControlResponse> findAll(@RequestParam(required = false) ControlDomain domain) {
		return controlCatalogService.findAll(Optional.ofNullable(domain)).stream()
				.map(ControlResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public ControlResponse findById(@PathVariable String id) {
		return ControlResponse.from(controlCatalogService.getRequired(id));
	}
}
