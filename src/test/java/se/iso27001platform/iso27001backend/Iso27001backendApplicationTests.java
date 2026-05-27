package se.iso27001platform.iso27001backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Iso27001backendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();


	@Test
	void contextLoads() {
	}

	@Test
	void createsAssessmentFlow() throws Exception {
		String organizationResponse = mockMvc.perform(post("/organizations")
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

		mockMvc.perform(get("/assessments/{id}/summary", assessmentId))
				.andExpect(status().isOk())
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
		mockMvc.perform(get("/controls"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("A.5.1"))
				.andExpect(jsonPath("$[0].domain").value("ORGANIZATIONAL"))
				.andExpect(jsonPath("$[0].question").isNotEmpty());

		mockMvc.perform(get("/controls/{id}", "a.5.1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("A.5.1"))
				.andExpect(jsonPath("$.title").value("Information security policy"));

		mockMvc.perform(get("/controls")
						.param("domain", "TECHNOLOGICAL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].domain").value("TECHNOLOGICAL"));
	}

	@Test
	void rejectsUnknownControlAnswer() throws Exception {
		String organizationResponse = mockMvc.perform(post("/organizations")
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
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"controlId":"A.999.999","answer":"YES"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Control not found: A.999.999"));
	}

	private void submitAnswer(UUID assessmentId, String controlId, String answer) throws Exception {
		mockMvc.perform(post("/assessments/{id}/answers", assessmentId)
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

}
