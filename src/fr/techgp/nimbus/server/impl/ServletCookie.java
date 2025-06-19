package fr.techgp.nimbus.server.impl;

import java.util.Objects;

import fr.techgp.nimbus.server.Cookie;

public class ServletCookie implements Cookie {

	private final jakarta.servlet.http.Cookie cookie;

	public ServletCookie(String name, String value) {
		super();
		this.cookie = new jakarta.servlet.http.Cookie(name, value);
	}

	public ServletCookie(jakarta.servlet.http.Cookie cookie) {
		super();
		this.cookie = cookie;
	}

	public jakarta.servlet.http.Cookie raw() {
		return this.cookie;
	}

	@Override
	public String name() {
		return this.cookie.getName();
	}

	@Override
	public String path() {
		return this.cookie.getPath();
	}

	@Override
	public ServletCookie path(String path) {
		this.cookie.setPath(Objects.requireNonNull(path));
		return this;
	}

	@Override
	public String value() {
		return this.cookie.getValue();
	}

	@Override
	public ServletCookie value(String value) {
		this.cookie.setValue(Objects.requireNonNull(value));
		return this;
	}

	@Override
	public String domain() {
		return this.cookie.getDomain();
	}

	@Override
	public ServletCookie domain(String domain) {
		this.cookie.setDomain(Objects.requireNonNull(domain));
		return this;
	}

	@Override
	public int maxAge() {
		return this.cookie.getMaxAge();
	}

	@Override
	public ServletCookie maxAge(int maxAge) {
		this.cookie.setMaxAge(maxAge);
		return this;
	}

	@Override
	public boolean secure() {
		return this.cookie.getSecure();
	}

	@Override
	public ServletCookie secure(boolean secure) {
		this.cookie.setSecure(secure);
		return this;
	}

	@Override
	public boolean httpOnly() {
		return this.cookie.isHttpOnly();
	}

	@Override
	public ServletCookie httpOnly(boolean httpOnly) {
		this.cookie.setHttpOnly(httpOnly);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name()).append("=").append(this.value());
		if (this.path() != null)
			sb.append("; Path=").append(this.path());
		if (this.domain() != null)
			sb.append("; Domain=").append(this.domain());
		if (this.maxAge() >= 0)
			sb.append("; MaxAge=").append(this.maxAge());
		if (this.secure())
			sb.append("; Secure");
		if (this.httpOnly())
			sb.append("; HttpOnly");
		return sb.toString();
	}

}
