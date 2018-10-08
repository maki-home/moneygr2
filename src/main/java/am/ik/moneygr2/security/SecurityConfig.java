package am.ik.moneygr2.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final String uaaUrl;
	private final RestTemplateBuilder restTemplateBuilder;

	public SecurityConfig(@Value("${uaa-url}") String uaaUrl,
			RestTemplateBuilder restTemplateBuilder) {
		this.uaaUrl = uaaUrl;
		this.restTemplateBuilder = restTemplateBuilder;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests() //
				.anyRequest().authenticated() //
				.and() //
				.logout() //
				.logoutSuccessUrl(uaaUrl + "/logout.do") //
				.and() //
				.oauth2Login() //
				.userInfoEndpoint() //
				.oidcUserService(
						new MoneygrOidcUserService(restTemplateBuilder.build(), uaaUrl));
	}
}