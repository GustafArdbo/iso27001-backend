package se.iso27001platform.iso27001backend.control.service;

import org.springframework.stereotype.Service;
import se.iso27001platform.iso27001backend.common.exception.ResourceNotFoundException;
import se.iso27001platform.iso27001backend.control.enums.ControlDomain;
import se.iso27001platform.iso27001backend.control.model.ControlDefinition;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ControlCatalogService {

	private final List<ControlDefinition> controls = List.of(
			new ControlDefinition(
					"A.5.1",
					ControlDomain.ORGANIZATIONAL,
					"Information security policy",
					"Does the organization have approved information security policies that are communicated and reviewed?",
					501
			),
			new ControlDefinition(
					"A.5.2",
					ControlDomain.ORGANIZATIONAL,
					"Security roles and responsibilities",
					"Are information security responsibilities clearly assigned and understood?",
					502
			),
			new ControlDefinition(
					"A.5.3",
					ControlDomain.ORGANIZATIONAL,
					"Segregation of duties",
					"Are conflicting duties separated to reduce fraud, error, and misuse risks?",
					503
			),
			new ControlDefinition(
					"A.5.4",
					ControlDomain.ORGANIZATIONAL,
					"Management responsibilities",
					"Does management require people to follow security policies and procedures?",
					504
			),
			new ControlDefinition(
					"A.5.7",
					ControlDomain.ORGANIZATIONAL,
					"Threat intelligence",
					"Does the organization collect and use threat information relevant to its context?",
					507
			),
			new ControlDefinition(
					"A.5.9",
					ControlDomain.ORGANIZATIONAL,
					"Asset inventory",
					"Are information assets and related assets identified and recorded?",
					509
			),
			new ControlDefinition(
					"A.6.3",
					ControlDomain.PEOPLE,
					"Security awareness and training",
					"Do people receive appropriate information security awareness, education, and training?",
					603
			),
			new ControlDefinition(
					"A.7.1",
					ControlDomain.PHYSICAL,
					"Physical security perimeter",
					"Are physical areas protected according to their security needs?",
					701
			),
			new ControlDefinition(
					"A.8.5",
					ControlDomain.TECHNOLOGICAL,
					"Secure authentication",
					"Are secure authentication methods used to protect systems and information?",
					805
			),
			new ControlDefinition(
					"A.8.15",
					ControlDomain.TECHNOLOGICAL,
					"Logging",
					"Are logs produced, protected, reviewed, and retained where needed?",
					815
			)
	);

	private final Map<String, ControlDefinition> controlsById = controls.stream()
			.collect(Collectors.toUnmodifiableMap(control -> normalize(control.id()), Function.identity()));

	public List<ControlDefinition> findAll() {
		return findAll(Optional.empty());
	}

	public List<ControlDefinition> findAll(Optional<ControlDomain> domain) {
		return controls.stream()
				.filter(control -> domain.map(value -> control.domain() == value).orElse(true))
				.sorted(Comparator.comparing(ControlDefinition::sortOrder))
				.toList();
	}

	public ControlDefinition getRequired(String id) {
		String normalizedId = normalize(id);
		ControlDefinition controlDefinition = controlsById.get(normalizedId);
		if (controlDefinition == null) {
			throw new ResourceNotFoundException("Control not found: " + id);
		}
		return controlDefinition;
	}

	public String normalize(String id) {
		return id.trim().toUpperCase(Locale.ROOT);
	}
}
