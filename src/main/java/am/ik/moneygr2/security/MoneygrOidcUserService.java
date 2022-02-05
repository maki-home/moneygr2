package am.ik.moneygr2.security;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class MoneygrOidcUserService
		implements OAuth2UserService<OidcUserRequest, OidcUser> {
	private final OidcUserService delegate;

	private final RestTemplate restTemplate;

	private final String uaaUrl;

	private final ObjectMapper objectMapper;

	private final Logger log = LoggerFactory.getLogger(MoneygrOidcUserService.class);

	public MoneygrOidcUserService(RestTemplate restTemplate, String uaaUrl,
			ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.delegate = new OidcUserService();
		this.restTemplate = restTemplate;
		this.uaaUrl = uaaUrl;
	}

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest)
			throws OAuth2AuthenticationException {
		final String accessToken = userRequest.getAccessToken().getTokenValue();
		final JsonNode idToken;
		try {
			final String token = userRequest.getIdToken().getTokenValue();
			idToken = objectMapper.readValue(
					Base64Utils.decodeFromString(token.split("\\.")[1]), JsonNode.class);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		final List<String> roles = idToken.has("roles")
				? StreamSupport.stream(idToken.get("roles").spliterator(), false)
				.map(JsonNode::asText).collect(Collectors.toList())
				: Collections.emptyList();
		final RequestEntity<Void> req = RequestEntity
				.get(URI.create(this.uaaUrl + "/Users"))
				.header(AUTHORIZATION, "Bearer " + accessToken).build();
		final JsonNode node = this.restTemplate.exchange(req, JsonNode.class).getBody();
		log.info("Users = {}", node);
		final Stream<JsonNode> stream = StreamSupport
				.stream(node.get("resources").spliterator(), false);
		final Map<String, Member> memberMap = stream.filter(n -> n.has("externalId"))
				.filter(n -> n.get("externalId").asText()
						.endsWith("ou=people,dc=ik,dc=am"))
				.peek(n -> log.info("User = {}", n))
				.collect(Collectors.toMap(n -> n.get("userName").asText(),
						n -> {
							final JsonNode familyName = n.get("name").get("familyName");
							final JsonNode givenName = n.get("name").get("givenName");
							return new Member(n.get("userName").asText(),
									familyName == null ? "" : familyName.asText(),
									givenName == null ? "" : givenName.asText());
						}));
		return new MoneygrOidcUser(this.delegate.loadUser(userRequest), roles, memberMap);
	}
}
