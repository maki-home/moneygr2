package am.ik.moneygr2.security;

import java.io.Serializable;

public class Member implements Serializable {
	private final String userName;
	private final String familyName;
	private final String givenName;

	public Member(String userName, String familyName, String givenName) {
		this.userName = userName;
		this.familyName = familyName;
		this.givenName = givenName;
	}

	public String getUserName() {
		return userName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getGivenName() {
		return givenName;
	}

	@Override
	public String toString() {
		return "Member{" + "userName='" + userName + '\'' + ", familyName='" + familyName
				+ '\'' + ", givenName='" + givenName + '\'' + '}';
	}

	public String getFullName() {
		return this.familyName + " " + this.givenName;
	}
}
