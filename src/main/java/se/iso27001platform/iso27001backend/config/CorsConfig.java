package se.iso27001platform.iso27001backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig {

	private final String[] allowedOriginPatterns;

	public CorsConfig(@Value("${app.security.cors-allowed-origin-patterns}") String allowedOriginPatterns) {
		this.allowedOriginPatterns = Arrays.stream(allowedOriginPatterns.split(","))
				.map(String::trim)
				.filter(pattern -> !pattern.isBlank())
				.toArray(String[]::new);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOriginPatterns(allowedOriginPatterns)
						.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
						.allowedHeaders("*");
			}
		};
	}
}
