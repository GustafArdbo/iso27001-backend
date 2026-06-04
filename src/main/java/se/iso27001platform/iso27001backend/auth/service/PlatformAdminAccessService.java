package se.iso27001platform.iso27001backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlatformAdminAccessService {

	private final CurrentUserService currentUserService;
	private final Set<UUID> platformAdminUserIds;

	public PlatformAdminAccessService(
			CurrentUserService currentUserService,
			@Value("${app.security.platform-admin-user-ids:}") String platformAdminUserIds
	) {
		this.currentUserService = currentUserService;
		this.platformAdminUserIds = parseUserIds(platformAdminUserIds);
	}

	public UUID requirePlatformAdmin() {
		UUID supabaseUserId = currentUserService.currentSupabaseUserId();
		if (!isPlatformAdmin(supabaseUserId)) {
			throw new AccessDeniedException("Platform administrator access is required");
		}
		return supabaseUserId;
	}

	public boolean isPlatformAdmin(UUID supabaseUserId) {
		return supabaseUserId != null && platformAdminUserIds.contains(supabaseUserId);
	}

	private Set<UUID> parseUserIds(String userIds) {
		return Arrays.stream(userIds.split(","))
				.map(String::trim)
				.filter(userId -> !userId.isBlank())
				.map(UUID::fromString)
				.collect(Collectors.toUnmodifiableSet());
	}
}
