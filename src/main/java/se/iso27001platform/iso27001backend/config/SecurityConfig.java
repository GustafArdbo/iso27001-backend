package se.iso27001platform.iso27001backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import se.iso27001platform.iso27001backend.auth.security.JwtRevocationFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRevocationFilter jwtRevocationFilter) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/health", "/actuator/health").permitAll()
						.requestMatchers(HttpMethod.POST, "/organization-applications").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
				)
				.addFilterAfter(jwtRevocationFilter, BearerTokenAuthenticationFilter.class)
				.build();
	}

	private Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

		return jwt -> {
			Collection<GrantedAuthority> authorities = new ArrayList<>(grantedAuthoritiesConverter.convert(jwt));
			String providerRole = jwt.getClaimAsString("role");
			if (providerRole != null && !providerRole.isBlank()) {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + providerRole.toUpperCase(Locale.ROOT)));
			}

			return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
		};
	}
}
