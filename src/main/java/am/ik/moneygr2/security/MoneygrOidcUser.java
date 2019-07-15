package am.ik.moneygr2.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class MoneygrOidcUser implements OidcUser {
	private final OidcUser delegate;
	private final Map<String, Member> memberMap;
	private final Collection<? extends GrantedAuthority> authorities;

	public MoneygrOidcUser(OidcUser delegate, List<String> roles,
			Map<String, Member> memberMap) {
		this.delegate = delegate;
		this.memberMap = Collections.unmodifiableMap(memberMap);
		this.authorities = AuthorityUtils.createAuthorityList(roles.stream()
				.map(x -> "ROLE_" + x.toUpperCase()).toArray(String[]::new));
	}

	public Member asMember() {
		return this.memberMap.get(this.getName());
	}

	public Map<String, Member> getMemberMap() {
		return this.memberMap;
	}

	@Override
	public Map<String, Object> getClaims() {
		return this.delegate.getClaims();
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return this.delegate.getUserInfo();
	}

	@Override
	public OidcIdToken getIdToken() {
		return this.delegate.getIdToken();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.delegate.getAttributes();
	}

	@Override
	public String getName() {
		return this.delegate.getName();
	}

	@Override
	public String toString() {
		return this.delegate.toString();
	}
}
