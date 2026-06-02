package se.iso27001platform.iso27001backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import se.iso27001platform.iso27001backend.membership.enums.MembershipRole;
import se.iso27001platform.iso27001backend.membership.model.OrganizationMembership;
import se.iso27001platform.iso27001backend.membership.repository.OrganizationMembershipRepository;
import se.iso27001platform.iso27001backend.organization.model.Organization;
import se.iso27001platform.iso27001backend.organization.service.OrganizationService;
import se.iso27001platform.iso27001backend.user.model.UserProfile;
import se.iso27001platform.iso27001backend.user.service.UserProfileService;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Iso27001backendApplicationTests {

	private static final UUID OWNER_SUPABASE_USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
	private static final UUID AUDITOR_SUPABASE_USER_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
	private static final UUID VIEWER_SUPABASE_USER_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");
	private static final UUID OUTSIDER_SUPABASE_USER_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationMembershipRepository membershipRepository;

	@Autowired
	private UserProfileService userProfileService;

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
	void createsPublicDemoRequestFromFrontendForm() throws Exception {
		mockMvc.perform(post("/demo-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "company":"  ACME AB  ",
								  "name":"  Jane Doe  ",
								  "email":"  SECURITY@ACME.COM  ",
								  "country":"Sweden (+46)",
								  "phone":"  555 123 4567  ",
								  "size":"11-50",
								  "message":"  We need help defining scope.  ",
								  "materials":["standard-forms","gap-analysis"]
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.company").value("ACME AB"))
				.andExpect(jsonPath("$.name").value("Jane Doe"))
				.andExpect(jsonPath("$.email").value("security@acme.com"))
				.andExpect(jsonPath("$.country").value("Sweden (+46)"))
				.andExpect(jsonPath("$.phone").value("555 123 4567"))
				.andExpect(jsonPath("$.size").value("11-50"))
				.andExpect(jsonPath("$.message").value("We need help defining scope."))
				.andExpect(jsonPath("$.materials[0]").value("gap-analysis"))
				.andExpect(jsonPath("$.materials[1]").value("standard-forms"))
				.andExpect(jsonPath("$.status").value("NEW"))
				.andExpect(jsonPath("$.createdAt").isNotEmpty());
	}

	@Test
	void rejectsInvalidPublicDemoRequestOptions() throws Exception {
		mockMvc.perform(post("/demo-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "company":"ACME AB",
								  "name":"Jane Doe",
								  "email":"security@acme.com",
								  "country":"Sweden (+46)",
								  "phone":"",
								  "size":"unknown",
								  "message":"",
								  "materials":[]
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Unsupported company size: unknown"));

		mockMvc.perform(post("/demo-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "company":"ACME AB",
								  "name":"Jane Doe",
								  "email":"security@acme.com",
								  "country":"Sweden (+46)",
								  "phone":"",
								  "size":"1-10",
								  "message":"",
								  "materials":["unknown-material"]
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Unsupported demo request material: unknown-material"));
	}

	@Test
	void allowsCorsPreflightForPublicDemoRequest() throws Exception {
		mockMvc.perform(options("/demo-requests")
						.header(HttpHeaders.ORIGIN, "https://frontend.example.com")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isOk())
				.andExpect(result -> assertTrue(
						"https://frontend.example.com".equals(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)),
						"Expected CORS response to allow the requesting frontend origin"
				));
	}

	@Test
	void businessEndpointsRequireJwtAcrossModules() throws Exception {
		mockMvc.perform(get("/auth/me"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/organizations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"No Auth AB"}
								"""))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/invitations/accept")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"token":"missing-token"}
								"""))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/demo-requests"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsOrganizationBootstrapWithoutUsableJwtIdentity() throws Exception {
		mockMvc.perform(post("/organizations")
						.with(authenticatedJwtWithoutEmail(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Missing Email AB"}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("JWT email claim is required"));

		mockMvc.perform(post("/organizations")
						.with(authenticatedJwtWithSubject("not-a-uuid", "owner@example.com"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Invalid Subject AB"}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("JWT subject must be a Supabase user UUID"));
	}

	@Test
	void createsOrganizationWithCurrentUserAsOwner() throws Exception {
		UUID organizationId = createOrganization("Bootstrap Owner AB");

		mockMvc.perform(get("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].organizationId").value(organizationId.toString()))
				.andExpect(jsonPath("$[0].email").value("owner@example.com"))
				.andExpect(jsonPath("$[0].supabaseUserId").value(OWNER_SUPABASE_USER_ID.toString()))
				.andExpect(jsonPath("$[0].role").value("OWNER"));
	}

	@Test
	void reusesUserProfileAcrossOrganizationMemberships() throws Exception {
		UUID firstOrganizationId = createOrganization("First Profile Organization AB");
		UUID secondOrganizationId = createOrganization("Second Profile Organization AB");

		JsonNode firstMembership = findMembership(firstOrganizationId, OWNER_SUPABASE_USER_ID);
		JsonNode secondMembership = findMembership(secondOrganizationId, OWNER_SUPABASE_USER_ID);

		assertTrue(
				firstMembership.get("userProfileId").asText().equals(secondMembership.get("userProfileId").asText()),
				"Expected the same Supabase identity to reuse one global user profile"
		);
		assertTrue(
				!firstMembership.get("id").asText().equals(secondMembership.get("id").asText()),
				"Expected separate organization memberships for the same global user profile"
		);
	}

	@Test
	void createsAssessmentFlow() throws Exception {
		UUID organizationId = createOrganizationWithMembership("Acme Security AB", AUDITOR_SUPABASE_USER_ID, MembershipRole.AUDITOR);

		UUID assessmentId = createAssessment(organizationId, "Initial ISO 27001 gap analysis", AUDITOR_SUPABASE_USER_ID);

		submitAnswer(assessmentId, "A.5.1", "YES", AUDITOR_SUPABASE_USER_ID);
		submitAnswer(assessmentId, "A.5.2", "PARTIAL", AUDITOR_SUPABASE_USER_ID);
		submitAnswer(assessmentId, "A.5.3", "NOT_APPLICABLE", AUDITOR_SUPABASE_USER_ID);

		mockMvc.perform(get("/assessments/{id}/questions", assessmentId)
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10))
				.andExpect(jsonPath("$[0].controlId").value("A.5.1"))
				.andExpect(jsonPath("$[0].answered").value(true))
				.andExpect(jsonPath("$[0].answer").value("YES"))
				.andExpect(jsonPath("$[3].controlId").value("A.5.4"))
				.andExpect(jsonPath("$[3].answered").value(false))
				.andExpect(jsonPath("$[3].answer").doesNotExist());

		mockMvc.perform(get("/assessments/{id}/summary", assessmentId)
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID)))
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
	void rejectsAssessmentForMissingOrganization() throws Exception {
		UUID missingOrganizationId = UUID.randomUUID();

		mockMvc.perform(post("/assessments")
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s","name":"Missing organization assessment"}
								""".formatted(missingOrganizationId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Organization not found: " + missingOrganizationId));
	}

	@Test
	void protectsAssessmentReadEndpointsByOrganizationMembership() throws Exception {
		UUID organizationId = createOrganizationWithMembership("Assessment Access AB", AUDITOR_SUPABASE_USER_ID, MembershipRole.AUDITOR);
		UUID assessmentId = createAssessment(organizationId, "Protected assessment", AUDITOR_SUPABASE_USER_ID);

		mockMvc.perform(get("/assessments/{id}", assessmentId)
						.with(authenticatedJwt(OUTSIDER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/assessments/{id}/questions", assessmentId)
						.with(authenticatedJwt(OUTSIDER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/assessments/{id}/summary", assessmentId)
						.with(authenticatedJwt(OUTSIDER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());
	}

	@Test
	void memberCanSubmitAnswersButCannotCreateAssessment() throws Exception {
		UUID organizationId = createOrganizationWithMembership("Member Answer AB", VIEWER_SUPABASE_USER_ID, MembershipRole.MEMBER);
		UUID assessmentId = createAssessment(organizationId, "Owner-created assessment", OWNER_SUPABASE_USER_ID);

		mockMvc.perform(post("/assessments")
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s","name":"Member-created assessment"}
								""".formatted(organizationId)))
				.andExpect(status().isForbidden());

		submitAnswer(assessmentId, "A.5.1", "PARTIAL", VIEWER_SUPABASE_USER_ID);

		mockMvc.perform(get("/assessments/{id}/summary", assessmentId)
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAnswers").value(1))
				.andExpect(jsonPath("$.score").value(0.5));
	}

	@Test
	void submittingSameControlUpdatesExistingAnswer() throws Exception {
		UUID organizationId = createOrganization("Answer Update AB");
		UUID assessmentId = createAssessment(organizationId, "Answer update assessment", OWNER_SUPABASE_USER_ID);

		submitAnswer(assessmentId, "a.5.1", "YES", OWNER_SUPABASE_USER_ID);

		mockMvc.perform(post("/assessments/{id}/answers", assessmentId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"controlId":"A.5.1","answer":"NO","comment":"Updated after review"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.controlId").value("A.5.1"))
				.andExpect(jsonPath("$.answer").value("NO"))
				.andExpect(jsonPath("$.comment").value("Updated after review"));

		mockMvc.perform(get("/assessments/{id}/questions", assessmentId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].answer").value("NO"))
				.andExpect(jsonPath("$[0].comment").value("Updated after review"));

		mockMvc.perform(get("/assessments/{id}/summary", assessmentId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.totalAnswers").value(1))
				.andExpect(jsonPath("$.answerCounts.YES").value(0))
				.andExpect(jsonPath("$.answerCounts.NO").value(1))
				.andExpect(jsonPath("$.score").value(0.0));
	}

	@Test
	void summaryHandlesOnlyNotApplicableAnswers() throws Exception {
		UUID organizationId = createOrganization("Not Applicable AB");
		UUID assessmentId = createAssessment(organizationId, "N/A assessment", OWNER_SUPABASE_USER_ID);

		submitAnswer(assessmentId, "A.5.1", "NOT_APPLICABLE", OWNER_SUPABASE_USER_ID);

		mockMvc.perform(get("/assessments/{id}/summary", assessmentId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAnswers").value(1))
				.andExpect(jsonPath("$.applicableAnswers").value(0))
				.andExpect(jsonPath("$.score").value(0.0))
				.andExpect(jsonPath("$.scorePercentage").value(0))
				.andExpect(jsonPath("$.gapPercentage").value(100))
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
		UUID organizationId = createOrganizationWithMembership("Unknown Control AB", AUDITOR_SUPABASE_USER_ID, MembershipRole.AUDITOR);

		UUID assessmentId = createAssessment(organizationId, "Control validation", AUDITOR_SUPABASE_USER_ID);

		mockMvc.perform(post("/assessments/{id}/answers", assessmentId)
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"controlId":"A.999.999","answer":"YES"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Control not found: A.999.999"));
	}

	@Test
	void returnsNotFoundForUnknownControlAndAssessment() throws Exception {
		UUID missingAssessmentId = UUID.randomUUID();

		mockMvc.perform(get("/controls/{id}", "A.999.999")
						.with(authenticatedJwt()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Control not found: A.999.999"));

		mockMvc.perform(get("/assessments/{id}", missingAssessmentId)
						.with(authenticatedJwt()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Assessment not found: " + missingAssessmentId));
	}

	@Test
	void createsOrganizationMembershipFlow() throws Exception {
		UUID organizationId = createOrganizationWithMembership("User Flow AB", OWNER_SUPABASE_USER_ID, MembershipRole.OWNER);

		String membershipResponse = mockMvc.perform(post("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"  AUDITOR@Example.COM  ","supabaseUserId":"%s","role":"AUDITOR"}
								""".formatted(AUDITOR_SUPABASE_USER_ID)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
				.andExpect(jsonPath("$.email").value("auditor@example.com"))
				.andExpect(jsonPath("$.supabaseUserId").value(AUDITOR_SUPABASE_USER_ID.toString()))
				.andExpect(jsonPath("$.role").value("AUDITOR"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID membershipId = readId(membershipResponse);

		mockMvc.perform(get("/memberships/{id}", membershipId)
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(membershipId.toString()))
				.andExpect(jsonPath("$.email").value("auditor@example.com"));

		mockMvc.perform(get("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[1].id").value(membershipId.toString()));

		String authMeResponse = mockMvc.perform(get("/auth/me")
						.with(authenticatedJwt("membership-token", AUDITOR_SUPABASE_USER_ID, UUID.randomUUID(), UUID.randomUUID().toString())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subject").value(AUDITOR_SUPABASE_USER_ID.toString()))
				.andExpect(jsonPath("$.profile.supabaseUserId").value(AUDITOR_SUPABASE_USER_ID.toString()))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertTrue(
				containsMembership(readJson(authMeResponse).get("memberships"), membershipId, organizationId),
				"Expected /auth/me to include the newly created organization membership"
		);
	}

	@Test
	void enforcesMembershipManagementRoleRulesAndSupabaseUniqueness() throws Exception {
		UUID organizationId = createOrganizationWithMembership("User Rules AB", VIEWER_SUPABASE_USER_ID, MembershipRole.VIEWER);

		mockMvc.perform(post("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"member@example.com","role":"MEMBER"}
								"""))
				.andExpect(status().isForbidden());

		JsonNode createdMembership = createMembership(
				organizationId,
				"auditor@example.com",
				AUDITOR_SUPABASE_USER_ID,
				MembershipRole.AUDITOR,
				OWNER_SUPABASE_USER_ID
		);
		UUID membershipId = UUID.fromString(createdMembership.get("id").asText());

		mockMvc.perform(post("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"second-auditor@example.com","supabaseUserId":"%s","role":"AUDITOR"}
								""".formatted(AUDITOR_SUPABASE_USER_ID)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Membership already exists in organization for Supabase user: " + AUDITOR_SUPABASE_USER_ID));

		mockMvc.perform(get("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/memberships/{id}", membershipId)
						.with(authenticatedJwt(OUTSIDER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());
	}

	@Test
	void createsAndAcceptsOrganizationInvitation() throws Exception {
		UUID organizationId = createOrganization("Invitation Flow AB");

		String invitationResponse = mockMvc.perform(post("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"  AUDITOR@example.com  ","role":"AUDITOR"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.invitation.organizationId").value(organizationId.toString()))
				.andExpect(jsonPath("$.invitation.email").value("auditor@example.com"))
				.andExpect(jsonPath("$.invitation.role").value("AUDITOR"))
				.andExpect(jsonPath("$.invitation.status").value("PENDING"))
				.andExpect(jsonPath("$.acceptanceToken").isNotEmpty())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String token = readJson(invitationResponse).get("acceptanceToken").asText();

		mockMvc.perform(post("/invitations/accept")
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"token":"%s"}
								""".formatted(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invitation.status").value("ACCEPTED"))
				.andExpect(jsonPath("$.invitation.acceptedByMembershipId").isNotEmpty())
				.andExpect(jsonPath("$.membership.organizationId").value(organizationId.toString()))
				.andExpect(jsonPath("$.membership.email").value("auditor@example.com"))
				.andExpect(jsonPath("$.membership.supabaseUserId").value(AUDITOR_SUPABASE_USER_ID.toString()))
				.andExpect(jsonPath("$.membership.role").value("AUDITOR"));

		mockMvc.perform(get("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[1].email").value("auditor@example.com"));

		mockMvc.perform(get("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].status").value("ACCEPTED"));
	}

	@Test
	void rejectsDuplicatePendingInvitationAndTokenReuse() throws Exception {
		UUID organizationId = createOrganization("Invitation Duplicate AB");
		String token = createInvitation(organizationId, "auditor@example.com", MembershipRole.AUDITOR, OWNER_SUPABASE_USER_ID)
				.get("acceptanceToken")
				.asText();

		mockMvc.perform(post("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"AUDITOR@example.com","role":"AUDITOR"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Invitation already pending for organization: auditor@example.com"));

		mockMvc.perform(post("/invitations/accept")
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"token":"%s"}
								""".formatted(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invitation.status").value("ACCEPTED"));

		mockMvc.perform(post("/invitations/accept")
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"token":"%s"}
								""".formatted(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invitation is not pending"));
	}

	@Test
	void rejectsInvitationForExistingUserAndUnknownToken() throws Exception {
		UUID organizationId = createOrganization("Invitation Existing User AB");
		createMembership(
				organizationId,
				"auditor@example.com",
				AUDITOR_SUPABASE_USER_ID,
				MembershipRole.AUDITOR,
				OWNER_SUPABASE_USER_ID
		);

		mockMvc.perform(post("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"AUDITOR@example.com","role":"AUDITOR"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Membership already exists in organization for email: auditor@example.com"));

		mockMvc.perform(post("/invitations/accept")
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"token":"unknown-token"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Invitation not found"));
	}

	@Test
	void protectsInvitationManagementEndpointsByRoleAndOrganization() throws Exception {
		UUID organizationId = createOrganizationWithMembership("Invitation Access AB", VIEWER_SUPABASE_USER_ID, MembershipRole.VIEWER);
		JsonNode invitation = createInvitation(organizationId, "auditor@example.com", MembershipRole.AUDITOR, OWNER_SUPABASE_USER_ID);
		UUID invitationId = UUID.fromString(invitation.get("invitation").get("id").asText());

		mockMvc.perform(get("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"member@example.com","role":"MEMBER"}
								"""))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/organizations/{organizationId}/invitations/{invitationId}", organizationId, invitationId)
						.with(authenticatedJwt(OUTSIDER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejectsInvitationAcceptForDifferentEmail() throws Exception {
		UUID organizationId = createOrganization("Invitation Email AB");
		String token = createInvitation(organizationId, "auditor@example.com", MembershipRole.AUDITOR, OWNER_SUPABASE_USER_ID)
				.get("acceptanceToken")
				.asText();

		mockMvc.perform(post("/invitations/accept")
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"token":"%s"}
								""".formatted(token)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Invitation email does not match current user"));
	}

	@Test
	void revokesPendingInvitation() throws Exception {
		UUID organizationId = createOrganization("Invitation Revoke AB");
		JsonNode invitationResponse = createInvitation(
				organizationId,
				"auditor@example.com",
				MembershipRole.AUDITOR,
				OWNER_SUPABASE_USER_ID
		);
		UUID invitationId = UUID.fromString(invitationResponse.get("invitation").get("id").asText());
		String token = invitationResponse.get("acceptanceToken").asText();

		mockMvc.perform(delete("/organizations/{organizationId}/invitations/{invitationId}", organizationId, invitationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/invitations/accept")
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"token":"%s"}
								""".formatted(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invitation is not pending"));

		mockMvc.perform(get("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("REVOKED"));
	}

	@Test
	void preventsAdminFromInvitingOwner() throws Exception {
		UUID organizationId = createOrganizationWithMembership("Invitation Roles AB", AUDITOR_SUPABASE_USER_ID, MembershipRole.ADMIN);

		mockMvc.perform(post("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(AUDITOR_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"viewer@example.com","role":"OWNER"}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Admins cannot invite owners"));
	}

	@Test
	void rejectsDuplicateOrganizationMembership() throws Exception {
		UUID organizationId = createOrganizationWithMembership("Duplicate User AB", OWNER_SUPABASE_USER_ID, MembershipRole.OWNER);

		mockMvc.perform(post("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"admin@example.com","role":"ADMIN"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"ADMIN@example.com","role":"ADMIN"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Membership already exists in organization for email: admin@example.com"));
	}

	@Test
	void rejectsMembershipForMissingOrganization() throws Exception {
		UUID missingOrganizationId = UUID.randomUUID();

		mockMvc.perform(post("/organizations/{organizationId}/memberships", missingOrganizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID))
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
		RequestPostProcessor revocableJwt = authenticatedJwt("revocable-token", OWNER_SUPABASE_USER_ID, sessionId, jwtId);

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

	@Test
	void revokesCurrentSessionForAllTokensInSession() throws Exception {
		UUID sessionId = UUID.randomUUID();
		RequestPostProcessor firstToken = authenticatedJwt(
				"session-token-a-" + UUID.randomUUID(),
				OWNER_SUPABASE_USER_ID,
				sessionId,
				UUID.randomUUID().toString()
		);
		RequestPostProcessor secondTokenInSession = authenticatedJwt(
				"session-token-b-" + UUID.randomUUID(),
				OWNER_SUPABASE_USER_ID,
				sessionId,
				UUID.randomUUID().toString()
		);

		mockMvc.perform(get("/auth/me")
						.with(secondTokenInSession))
				.andExpect(status().isOk());

		mockMvc.perform(post("/auth/revocations/current-session")
						.with(firstToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"reason":"session compromise"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("SESSION"))
				.andExpect(jsonPath("$.jwtId").doesNotExist())
				.andExpect(jsonPath("$.sessionId").value(sessionId.toString()));

		mockMvc.perform(get("/auth/me")
						.with(secondTokenInSession))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsSessionRevocationWithoutSessionClaim() throws Exception {
		mockMvc.perform(post("/auth/revocations/current-session")
						.with(authenticatedJwtWithoutSession(OWNER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"reason":"missing session"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("JWT does not contain a session_id claim"));
	}

	@Test
	void rejectsCrossOrganizationAccess() throws Exception {
		UUID organizationAId = createOrganizationWithMembership("Organization A", OWNER_SUPABASE_USER_ID, MembershipRole.OWNER);
		UUID organizationBId = createOrganization("Organization B", AUDITOR_SUPABASE_USER_ID);

		mockMvc.perform(get("/organizations/{id}", organizationAId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(organizationAId.toString()));

		mockMvc.perform(get("/organizations/{id}", organizationBId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/organizations/{id}", organizationAId)
						.with(authenticatedJwt(OUTSIDER_SUPABASE_USER_ID)))
				.andExpect(status().isForbidden());
	}

	@Test
	void enforcesAssessmentRoleRules() throws Exception {
		UUID organizationId = createOrganizationWithMembership("Role Rules AB", AUDITOR_SUPABASE_USER_ID, MembershipRole.AUDITOR);
		addMembership(organizationId, VIEWER_SUPABASE_USER_ID, MembershipRole.VIEWER);

		mockMvc.perform(post("/assessments")
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s","name":"Viewer-created assessment"}
								""".formatted(organizationId)))
				.andExpect(status().isForbidden());

		UUID assessmentId = createAssessment(organizationId, "Role protected assessment", AUDITOR_SUPABASE_USER_ID);

		mockMvc.perform(post("/assessments/{id}/answers", assessmentId)
						.with(authenticatedJwt(VIEWER_SUPABASE_USER_ID))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"controlId":"A.5.1","answer":"YES"}
								"""))
				.andExpect(status().isForbidden());

		submitAnswer(assessmentId, "A.5.1", "YES", AUDITOR_SUPABASE_USER_ID);
	}

	private UUID createOrganization(String name) throws Exception {
		return createOrganization(name, OWNER_SUPABASE_USER_ID);
	}

	private UUID createOrganization(String name, UUID supabaseUserId) throws Exception {
		String organizationResponse = mockMvc.perform(post("/organizations")
						.with(authenticatedJwt(supabaseUserId))
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

	private UUID createOrganizationWithMembership(String name, UUID supabaseUserId, MembershipRole role) throws Exception {
		UUID organizationId = createOrganization(name);
		addMembership(organizationId, supabaseUserId, role);
		return organizationId;
	}

	private UUID createAssessment(UUID organizationId, String name, UUID supabaseUserId) throws Exception {
		String assessmentResponse = mockMvc.perform(post("/assessments")
						.with(authenticatedJwt(supabaseUserId))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s","name":"%s"}
								""".formatted(organizationId, name)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		return readId(assessmentResponse);
	}

	private void addMembership(UUID organizationId, UUID supabaseUserId, MembershipRole role) {
		if (membershipRepository.existsByOrganization_IdAndUserProfile_SupabaseUserId(organizationId, supabaseUserId)) {
			return;
		}
		Organization organization = organizationService.getRequired(organizationId);
		String email = emailFor(supabaseUserId);
		UserProfile userProfile = userProfileService.getOrCreateForMembership(supabaseUserId, email);
		membershipRepository.save(new OrganizationMembership(organization, userProfile, role));
	}

	private void submitAnswer(UUID assessmentId, String controlId, String answer, UUID supabaseUserId) throws Exception {
		mockMvc.perform(post("/assessments/{id}/answers", assessmentId)
						.with(authenticatedJwt(supabaseUserId))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"controlId":"%s","answer":"%s","comment":"Initial answer"}
				""".formatted(controlId, answer)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.controlId").value(controlId.trim().toUpperCase(Locale.ROOT)))
				.andExpect(jsonPath("$.answer").value(answer));
	}

	private UUID readId(String json) throws Exception {
		return UUID.fromString(readJson(json).get("id").asText());
	}

	private JsonNode createMembership(UUID organizationId, String email, UUID supabaseUserId, MembershipRole role, UUID actorSupabaseUserId)
			throws Exception {
		String membershipResponse = mockMvc.perform(post("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(actorSupabaseUserId))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","supabaseUserId":"%s","role":"%s"}
								""".formatted(email, supabaseUserId, role)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return readJson(membershipResponse);
	}

	private JsonNode createInvitation(UUID organizationId, String email, MembershipRole role, UUID inviterSupabaseUserId) throws Exception {
		String invitationResponse = mockMvc.perform(post("/organizations/{organizationId}/invitations", organizationId)
						.with(authenticatedJwt(inviterSupabaseUserId))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","role":"%s"}
								""".formatted(email, role)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return readJson(invitationResponse);
	}

	private JsonNode readJson(String json) throws Exception {
		return objectMapper.readTree(json);
	}

	private JsonNode findMembership(UUID organizationId, UUID supabaseUserId) throws Exception {
		String membershipsResponse = mockMvc.perform(get("/organizations/{organizationId}/memberships", organizationId)
						.with(authenticatedJwt(OWNER_SUPABASE_USER_ID)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		for (JsonNode membership : readJson(membershipsResponse)) {
			if (supabaseUserId.toString().equals(membership.get("supabaseUserId").asText())) {
				return membership;
			}
		}
		throw new AssertionError("Expected membership for Supabase user: " + supabaseUserId);
	}

	private boolean containsMembership(JsonNode memberships, UUID membershipId, UUID organizationId) {
		for (JsonNode membership : memberships) {
			if (membershipId.toString().equals(membership.get("membershipId").asText())
					&& organizationId.toString().equals(membership.get("organizationId").asText())) {
				return true;
			}
		}
		return false;
	}

	private RequestPostProcessor authenticatedJwt() {
		return authenticatedJwt(OWNER_SUPABASE_USER_ID);
	}

	private RequestPostProcessor authenticatedJwt(UUID subject) {
		return authenticatedJwt("test-token-" + UUID.randomUUID(), subject, UUID.randomUUID(), UUID.randomUUID().toString());
	}

	private RequestPostProcessor authenticatedJwt(String tokenValue, UUID subject, UUID sessionId, String jwtId) {
		Jwt token = Jwt.withTokenValue(tokenValue)
				.header("alg", "RS256")
				.subject(subject.toString())
				.claim("email", emailFor(subject))
				.claim("role", "authenticated")
				.claim("session_id", sessionId.toString())
				.claim("jti", jwtId)
				.issuedAt(Instant.now().minusSeconds(60))
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();

		return jwt().jwt(token);
	}

	private RequestPostProcessor authenticatedJwtWithoutEmail(UUID subject) {
		Jwt token = Jwt.withTokenValue("test-token-without-email-" + UUID.randomUUID())
				.header("alg", "RS256")
				.subject(subject.toString())
				.claim("role", "authenticated")
				.claim("session_id", UUID.randomUUID().toString())
				.claim("jti", UUID.randomUUID().toString())
				.issuedAt(Instant.now().minusSeconds(60))
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();

		return jwt().jwt(token);
	}

	private RequestPostProcessor authenticatedJwtWithoutSession(UUID subject) {
		Jwt token = Jwt.withTokenValue("test-token-without-session-" + UUID.randomUUID())
				.header("alg", "RS256")
				.subject(subject.toString())
				.claim("email", emailFor(subject))
				.claim("role", "authenticated")
				.claim("jti", UUID.randomUUID().toString())
				.issuedAt(Instant.now().minusSeconds(60))
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();

		return jwt().jwt(token);
	}

	private RequestPostProcessor authenticatedJwtWithSubject(String subject, String email) {
		Jwt token = Jwt.withTokenValue("test-token-subject-" + UUID.randomUUID())
				.header("alg", "RS256")
				.subject(subject)
				.claim("email", email)
				.claim("role", "authenticated")
				.claim("session_id", UUID.randomUUID().toString())
				.claim("jti", UUID.randomUUID().toString())
				.issuedAt(Instant.now().minusSeconds(60))
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();

		return jwt().jwt(token);
	}

	private String emailFor(UUID subject) {
		if (OWNER_SUPABASE_USER_ID.equals(subject)) {
			return "owner@example.com";
		}
		if (AUDITOR_SUPABASE_USER_ID.equals(subject)) {
			return "auditor@example.com";
		}
		if (VIEWER_SUPABASE_USER_ID.equals(subject)) {
			return "viewer@example.com";
		}
		return "outsider@example.com";
	}

}
