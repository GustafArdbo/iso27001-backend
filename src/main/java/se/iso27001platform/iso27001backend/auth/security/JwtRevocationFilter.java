package se.iso27001platform.iso27001backend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.iso27001platform.iso27001backend.auth.service.AuthRevocationService;

import java.io.IOException;

@Component
public class JwtRevocationFilter extends OncePerRequestFilter {

	private final AuthRevocationService authRevocationService;

	public JwtRevocationFilter(AuthRevocationService authRevocationService) {
		this.authRevocationService = authRevocationService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken
				&& authRevocationService.isRevoked(jwtAuthenticationToken.getToken())) {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write("""
					{"status":401,"error":"Unauthorized","message":"JWT has been revoked"}
					""");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
