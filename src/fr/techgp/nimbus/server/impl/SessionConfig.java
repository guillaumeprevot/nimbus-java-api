package fr.techgp.nimbus.server.impl;

import java.util.Objects;

public class SessionConfig {

	/** The secret key used for client session encryption */
	private byte[] secretKey = null;
	/** The default timeout for sessions, either client-side or server-side, either cookie's max age or session's maxInactiveInterval */
	private int timeout = 60 * 60;
	/** The "path" attribute of the session cookies */
	private String cookiePath = "/";
	/** The "domain" attribute of the session cookies */
	private String cookieDomain = "";

	public byte[] getSecretKey() {
		return this.secretKey;
	}

	public void setSecretKey(byte[] secretKey) {
		this.secretKey = secretKey;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getCookiePath() {
		return this.cookiePath;
	}

	public void setCookiePath(String cookiePath) {
		this.cookiePath = Objects.requireNonNull(cookiePath);
	}

	public String getCookieDomain() {
		return this.cookieDomain;
	}

	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = Objects.requireNonNull(cookieDomain);
	}

}
