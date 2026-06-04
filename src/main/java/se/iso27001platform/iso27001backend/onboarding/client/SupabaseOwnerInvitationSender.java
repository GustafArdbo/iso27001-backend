package se.iso27001platform.iso27001backend.onboarding.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class SupabaseOwnerInvitationSender implements OwnerInvitationSender {

	private final String supabaseUrl;
	private final String secretKey;
	private final String redirectUrl;
	private final RestClient restClient;

	public SupabaseOwnerInvitationSender(
			RestClient.Builder restClientBuilder,
			@Value("${app.supabase.url:}") String supabaseUrl,
			@Value("${app.supabase.secret-key:}") String secretKey,
			@Value("${app.supabase.invite-redirect-url:}") String redirectUrl
	) {
		this.supabaseUrl = removeTrailingSlash(supabaseUrl);
		this.secretKey = secretKey;
		this.redirectUrl = redirectUrl;
		RestClient.Builder configuredBuilder = restClientBuilder
				.defaultHeader("apikey", secretKey)
				.defaultHeader("Authorization", "Bearer " + secretKey);
		if (!this.supabaseUrl.isBlank()) {
			configuredBuilder.baseUrl(this.supabaseUrl);
		}
		this.restClient = configuredBuilder.build();
	}

	@Override
	public OwnerInvitationResult send(String email, String ownerName, UUID organizationId) {
		requireConfigured();

		try {
			SupabaseInviteUserResponse response = restClient.post()
					.uri(uriBuilder -> uriBuilder
							.path("/auth/v1/invite")
							.queryParam("redirect_to", redirectUrl)
							.build())
					.body(Map.of(
							"email", email,
							"data", Map.of(
									"owner_name", ownerName,
									"organization_id", organizationId.toString()
							)
					))
					.retrieve()
					.body(SupabaseInviteUserResponse.class);

			if (response == null || response.id() == null) {
				throw new OwnerInvitationDeliveryException("Supabase invite response did not contain a user id");
			}
			return new OwnerInvitationResult(response.id());
		}
		catch (HttpClientErrorException exception) {
			if (isExistingUserConflict(exception)) {
				sendMagicLink(restClient, email);
				return new OwnerInvitationResult(null);
			}
			throw deliveryException(exception);
		}
		catch (RestClientException exception) {
			throw new OwnerInvitationDeliveryException("Supabase owner invitation request failed", exception);
		}
	}

	private void sendMagicLink(RestClient restClient, String email) {
		try {
			restClient.post()
					.uri(uriBuilder -> uriBuilder
							.path("/auth/v1/otp")
							.queryParam("redirect_to", redirectUrl)
							.build())
					.body(Map.of("email", email, "create_user", false))
					.retrieve()
					.toBodilessEntity();
		}
		catch (RestClientException exception) {
			throw new OwnerInvitationDeliveryException("Supabase magic link request failed", exception);
		}
	}

	private boolean isExistingUserConflict(HttpClientErrorException exception) {
		if (exception.getStatusCode() != HttpStatus.BAD_REQUEST
				&& exception.getStatusCode() != HttpStatus.CONFLICT
				&& exception.getStatusCode().value() != 422) {
			return false;
		}

		String responseBody = exception.getResponseBodyAsString().toLowerCase(Locale.ROOT);
		return responseBody.contains("already registered")
				|| responseBody.contains("already exists")
				|| responseBody.contains("already been registered");
	}

	private OwnerInvitationDeliveryException deliveryException(HttpClientErrorException exception) {
		return new OwnerInvitationDeliveryException(
				"Supabase owner invitation failed with HTTP " + exception.getStatusCode().value(),
				exception
		);
	}

	private void requireConfigured() {
		if (supabaseUrl.isBlank() || secretKey.isBlank() || redirectUrl.isBlank()) {
			throw new OwnerInvitationDeliveryException(
					"Supabase owner invitation is not configured"
			);
		}
	}

	private String removeTrailingSlash(String value) {
		return value == null ? "" : value.replaceAll("/+$", "");
	}

	private record SupabaseInviteUserResponse(UUID id) {
	}
}
