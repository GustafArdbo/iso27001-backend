package se.iso27001platform.iso27001backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Iso27001backendApplicationTests {

	private static final UUID TEST_SUPABASE_USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();


	@Test
	void contextLoads() {
	}

	@Test
	void healthIsPublicButBusinessApiRequiresJwt() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));

		mockMvc.perform(get("/controls"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createsAssessmentFlow() throws Exception {
		String organizationResponse = mockMvc.perform(post("/organizations")
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Acme Security AB"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Acme Security AB"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID organizationId = readId(organizationResponse);

		String assessmentResponse = mockMvc.perform(post("/assessments")
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s","name":"Initial ISO 27001 gap analysis"}
								""".formatted(organizationId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID assessmentId = readId(assessmentResponse);

		submitAnswer(assessmentId, "A.5.1", "YES");
		submitAnswer(assessmentId, "A.5.2", "PARTIAL");
		submitAnswer(assessmentId, "A.5.3", "NOT_APPLICABLE");

		mockMvc.perform(get("/assessments/{id}/questions", assessmentId)
						.with(authenticatedJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10))
				.andExpect(jsonPath("$[0].controlId").value("A.5.1"))
				.andExpect(jsonPath("$[0].answered").value(true))
				.andExpect(jsonPath("$[0].answer").value("YES"))
				.andExpect(jsonPath("$[3].controlId").value("A.5.4"))
				.andExpect(jsonPath("$[3].answered").value(false))
				.andExpect(jsonPath("$[3].answer").doesNotExist());

		mockMvc.perform(get("/assessments/{id}/summary", assessmentId)
						.with(authenticatedJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalControls").value(10))
				.andExpect(jsonPath("$.answeredControls").value(3))
				.andExpect(jsonPath("$.unansweredControls").value(7))
				.andExpect(jsonPath("$.completionPercentage").value(30))
				.andExpect(jsonPath("$.totalAnswers").value(3))
				.andExpect(jsonPath("$.applicableAnswers").value(2))
				.andExpect(jsonPath("$.score").value(0.75))
				.andExpect(jsonPath("$.scorePercentage").value(75))
				.andExpect(jsonPath("$.gapPercentage").value(25))
				.andExpect(jsonPath("$.answerCounts.YES").value(1))
				.andExpect(jsonPath("$.answerCounts.PARTIAL").value(1))
				.andExpect(jsonPath("$.answerCounts.NOT_APPLICABLE").value(1));
	}

	@Test
	void listsControlCatalog() throws Exception {
		mockMvc.perform(get("/controls")
						.with(authenticatedJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("A.5.1"))
				.andExpect(jsonPath("$[0].domain").value("ORGANIZATIONAL"))
				.andExpect(jsonPath("$[0].question").isNotEmpty());

		mockMvc.perform(get("/controls/{id}", "a.5.1")
						.with(authenticatedJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("A.5.1"))
				.andExpect(jsonPath("$.title").value("Information security policy"));

		mockMvc.perform(get("/controls")
						.with(authenticatedJwt())
						.param("domain", "TECHNOLOGICAL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].domain").value("TECHNOLOGICAL"));
	}

	@Test
	void rejectsUnknownControlAnswer() throws Exception {
		String organizationResponse = mockMvc.perform(post("/organizations")
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Unknown Control AB"}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID organizationId = readId(organizationResponse);

		String assessmentResponse = mockMvc.perform(post("/assessments")
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s","name":"Control validation"}
								""".formatted(organizationId)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID assessmentId = readId(assessmentResponse);

		mockMvc.perform(post("/assessments/{id}/answers", assessmentId)
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"controlId":"A.999.999","answer":"YES"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Control not found: A.999.999"));
	}

	@Test
	void createsOrganizationUserFlow() throws Exception {
		UUID organizationId = createOrganization("User Flow AB");

		String userResponse = mockMvc.perform(post("/organizations/{organizationId}/users", organizationId)
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"  AUDITOR@Example.COM  ","supabaseUserId":"%s","role":"AUDITOR"}
								""".formatted(TEST_SUPABASE_USER_ID)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
				.andExpect(jsonPath("$.email").value("auditor@example.com"))
				.andExpect(jsonPath("$.supabaseUserId").value(TEST_SUPABASE_USER_ID.toString()))
				.andExpect(jsonPath("$.role").value("AUDITOR"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID userId = readId(userResponse);

		mockMvc.perform(get("/users/{id}", userId)
						.with(authenticatedJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId.toString()))
				.andExpect(jsonPath("$.email").value("auditor@example.com"));

		mockMvc.perform(get("/organizations/{organizationId}/users", organizationId)
						.with(authenticatedJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(userId.toString()));

		mockMvc.perform(get("/auth/me")
						.with(authenticatedJwt("membership-token", TEST_SUPABASE_USER_ID, UUID.randomUUID(), UUID.randomUUID().toString())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subject").value(TEST_SUPABASE_USER_ID.toString()))
				.andExpect(jsonPath("$.memberships.length()").value(1))
				.andExpect(jsonPath("$.memberships[0].userId").value(userId.toString()));
	}

	@Test
	void rejectsDuplicateOrganizationUser() throws Exception {
		UUID organizationId = createOrganization("Duplicate User AB");

		mockMvc.perform(post("/organizations/{organizationId}/users", organizationId)
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"owner@example.com","role":"OWNER"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/organizations/{organizationId}/users", organizationId)
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"OWNER@example.com","role":"ADMIN"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("User already exists in organization: owner@example.com"));
	}

	@Test
	void rejectsUserForMissingOrganization() throws Exception {
		UUID missingOrganizationId = UUID.randomUUID();

		mockMvc.perform(post("/organizations/{organizationId}/users", missingOrganizationId)
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"missing@example.com","role":"MEMBER"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Organization not found: " + missingOrganizationId));
	}

	@Test
	void revokesCurrentJwt() throws Exception {
		UUID sessionId = UUID.randomUUID();
		String jwtId = UUID.randomUUID().toString();
		RequestPostProcessor revocableJwt = authenticatedJwt("revocable-token", TEST_SUPABASE_USER_ID, sessionId, jwtId);

		mockMvc.perform(get("/auth/me")
						.with(revocableJwt))
				.andExpect(status().isOk());

		mockMvc.perform(post("/auth/revocations/current-token")
						.with(revocableJwt)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"reason":"test revocation"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("TOKEN"))
				.andExpect(jsonPath("$.jwtId").value(jwtId))
				.andExpect(jsonPath("$.sessionId").value(sessionId.toString()));

		mockMvc.perform(get("/auth/me")
						.with(revocableJwt))
				.andExpect(status().isUnauthorized());
	}

	private UUID createOrganization(String name) throws Exception {
		String organizationResponse = mockMvc.perform(post("/organizations")
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return readId(organizationResponse);
	}

	private void submitAnswer(UUID assessmentId, String controlId, String answer) throws Exception {
		mockMvc.perform(post("/assessments/{id}/answers", assessmentId)
						.with(authenticatedJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"controlId":"%s","answer":"%s","comment":"Initial answer"}
								""".formatted(controlId, answer)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.controlId").value(controlId))
				.andExpect(jsonPath("$.answer").value(answer));
	}

	private UUID readId(String json) throws Exception {
		JsonNode response = objectMapper.readTree(json);
		return UUID.fromString(response.get("id").asText());
	}

	private RequestPostProcessor authenticatedJwt() {
		return authenticatedJwt("test-token-" + UUID.randomUUID(), TEST_SUPABASE_USER_ID, UUID.randomUUID(), UUID.randomUUID().toString());
	}

	private RequestPostProcessor authenticatedJwt(String tokenValue, UUID subject, UUID sessionId, String jwtId) {
		Jwt token = Jwt.withTokenValue(tokenValue)
				.header("alg", "RS256")
				.subject(subject.toString())
				.claim("email", "auditor@example.com")
				.claim("role", "authenticated")
				.claim("session_id", sessionId.toString())
				.claim("jti", jwtId)
				.issuedAt(Instant.now().minusSeconds(60))
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();

		return jwt().jwt(token);
	}

}
