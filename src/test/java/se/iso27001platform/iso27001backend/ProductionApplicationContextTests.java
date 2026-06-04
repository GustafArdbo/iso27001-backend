package se.iso27001platform.iso27001backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.iso27001platform.iso27001backend.onboarding.client.OwnerInvitationSender;
import se.iso27001platform.iso27001backend.onboarding.client.SupabaseOwnerInvitationSender;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
class ProductionApplicationContextTests {

	@Autowired
	private OwnerInvitationSender ownerInvitationSender;

	@Test
	void loadsProductionOwnerInvitationSender() {
		assertInstanceOf(SupabaseOwnerInvitationSender.class, ownerInvitationSender);
	}
}
