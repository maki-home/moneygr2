package am.ik.moneygr2.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final String uaaUrl;
	private final RestTemplateBuilder restTemplateBuilder;
	private final ObjectMapper objectMapper;

	public SecurityConfig(@Value("${uaa-url}") String uaaUrl,
			RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
		this.uaaUrl = uaaUrl;
		this.restTemplateBuilder = restTemplateBuilder;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests() //
				.requestMatchers(EndpointRequest.to("health", "info", "prometheus"))
				.permitAll() //
				.anyRequest().hasRole("USERS") //
				.and() //
				.logout() //
				.logoutSuccessUrl(uaaUrl + "/logout.do") //
				.and() //
				.oauth2Login() //
				.userInfoEndpoint() //
				.oidcUserService(new MoneygrOidcUserService(restTemplateBuilder.build(),
						uaaUrl, objectMapper));
	}
}