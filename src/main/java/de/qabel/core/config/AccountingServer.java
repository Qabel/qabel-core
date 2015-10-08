package de.qabel.core.config;

import java.net.URI;

public class AccountingServer extends SyncSettingItem {

	private URI uri;
	private String username;
	private String password;

	private String authToken;

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public AccountingServer(URI uri, String username, String password) {
		this.uri = uri;
		this.username = username;
		this.password = password;
	}
}
