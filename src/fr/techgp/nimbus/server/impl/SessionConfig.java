package fr.techgp.nimbus.server.impl;

public class SessionConfig {

	/** The secret key used for client session encryption */
	private byte[] secretKey = null;
	/** The default timeout for sessions, either client-side or server-side, either cookie's max age or session's maxInactiveInterval */
	private int timeout = 60 * 60;
	/** The "path" attribute of the session cookies (leave it to null to use default vaule) */
	private String cookiePath = null;
	/** The "domain" attribute of the session cookies (leave it to null to use default vaule) */
	private String cookieDomain = null;

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
		return this.cookiePath == null ? "/" : this.cookiePath;
	}

	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	public String getCookieDomain() {
		return this.cookieDomain == null ? "" : this.cookieDomain;
	}

	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
	}

}
