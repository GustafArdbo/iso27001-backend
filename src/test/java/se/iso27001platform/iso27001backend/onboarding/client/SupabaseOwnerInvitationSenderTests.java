package se.iso27001platform.iso27001backend.onboarding.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SupabaseOwnerInvitationSenderTests {

	private static final String SUPABASE_URL = "https://test.supabase.co";
	private static final String SECRET_KEY = "test-secret";
	private static final String REDIRECT_URL = "https://frontend.example.com/auth/callback";

	@Test
	void sendsSupabaseAdminInviteAndReturnsCreatedUserId() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		SupabaseOwnerInvitationSender sender = sender(restClientBuilder);
		UUID organizationId = UUID.randomUUID();
		UUID supabaseUserId = UUID.randomUUID();

		server.expect(once(), requestTo(SUPABASE_URL + "/auth/v1/invite?redirect_to=" + REDIRECT_URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("apikey", SECRET_KEY))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + SECRET_KEY))
				.andExpect(queryParam("redirect_to", REDIRECT_URL))
				.andExpect(jsonPath("$.email").value("owner@example.com"))
				.andExpect(jsonPath("$.data.owner_name").value("Jane Doe"))
				.andExpect(jsonPath("$.data.organization_id").value(organizationId.toString()))
				.andRespond(withSuccess(
						"{\"id\":\"" + supabaseUserId + "\"}",
						MediaType.APPLICATION_JSON
				));

		OwnerInvitationResult result = sender.send("owner@example.com", "Jane Doe", organizationId);

		assertEquals(supabaseUserId, result.supabaseUserId());
		server.verify();
	}

	@Test
	void sendsMagicLinkWhenOwnerAlreadyExistsInSupabase() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		SupabaseOwnerInvitationSender sender = sender(restClientBuilder);

		server.expect(once(), requestTo(SUPABASE_URL + "/auth/v1/invite?redirect_to=" + REDIRECT_URL))
				.andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT)
						.contentType(MediaType.APPLICATION_JSON)
						.body("{\"message\":\"A user with this email address has already been registered\"}"));
		server.expect(once(), requestTo(SUPABASE_URL + "/auth/v1/otp?redirect_to=" + REDIRECT_URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(queryParam("redirect_to", REDIRECT_URL))
				.andExpect(jsonPath("$.email").value("owner@example.com"))
				.andExpect(jsonPath("$.create_user").value(false))
				.andRespond(withSuccess());

		OwnerInvitationResult result = sender.send("owner@example.com", "Jane Doe", UUID.randomUUID());

		assertNull(result.supabaseUserId());
		server.verify();
	}

	@Test
	void rejectsDeliveryWhenSupabaseConfigurationIsMissing() {
		SupabaseOwnerInvitationSender sender = new SupabaseOwnerInvitationSender(
				RestClient.builder(),
				"",
				"",
				""
		);

		OwnerInvitationDeliveryException exception = assertThrows(
				OwnerInvitationDeliveryException.class,
				() -> sender.send("owner@example.com", "Jane Doe", UUID.randomUUID())
		);

		assertEquals("Supabase owner invitation is not configured", exception.getMessage());
	}

	private SupabaseOwnerInvitationSender sender(RestClient.Builder restClientBuilder) {
		return new SupabaseOwnerInvitationSender(
				restClientBuilder,
				SUPABASE_URL,
				SECRET_KEY,
				REDIRECT_URL
		);
	}
}
