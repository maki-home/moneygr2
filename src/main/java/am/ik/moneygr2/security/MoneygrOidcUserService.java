package am.ik.moneygr2.security;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class MoneygrOidcUserService
		implements OAuth2UserService<OidcUserRequest, OidcUser> {
	private final OidcUserService delegate;
	private final RestTemplate restTemplate;
	private final String uaaUrl;

	public MoneygrOidcUserService(RestTemplate restTemplate, String uaaUrl) {
		this.delegate = new OidcUserService();
		this.restTemplate = restTemplate;
		this.uaaUrl = uaaUrl;
	}

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest)
			throws OAuth2AuthenticationException {
		String accessToken = userRequest.getAccessToken().getTokenValue();
		RequestEntity<Void> req = RequestEntity.get(URI.create(this.uaaUrl + "/Users"))
				.header(AUTHORIZATION, "Bearer " + accessToken).build();
		JsonNode node = this.restTemplate.exchange(req, JsonNode.class).getBody();
		Stream<JsonNode> stream = StreamSupport
				.stream(node.get("resources").spliterator(), false);
		Map<String, Member> memberMap = stream
				.filter(n -> !"admin".equals(n.get("userName").asText()))
				.collect(Collectors.toMap(n -> n.get("userName").asText(),
						n -> new Member(n.get("userName").asText(),
								n.get("name").get("familyName").asText(),
								n.get("name").get("givenName").asText())));
		return new MoneygrOidcUser(this.delegate.loadUser(userRequest), memberMap);
	}
}
