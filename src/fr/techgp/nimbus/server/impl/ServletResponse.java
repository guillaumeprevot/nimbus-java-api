package fr.techgp.nimbus.server.impl;

import jakarta.servlet.http.HttpServletResponse;

import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Response;

public class ServletResponse implements Response {

	private final HttpServletResponse response;
	private Render body;

	public ServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HttpServletResponse raw() {
		return this.response;
	}

	@Override
	public int status() {
		return this.response.getStatus();
	}

	@Override
	public ServletResponse status(int status) {
		this.response.setStatus(status);
		return this;
	}

	@Override
	public String type() {
		return this.response.getContentType();
	}

	@Override
	public ServletResponse type(String contentType) {
		this.response.setContentType(contentType);
		return this;
	}

	@Override
	public Render body() {
		return this.body;
	}

	@Override
	public ServletResponse body(Render body) {
		this.body = body;
		return this;
	}

	@Override
	public String header(String name) {
		return this.response.getHeader(name);
	}

	@Override
	public ServletResponse header(String name, String value) {
		this.response.setHeader(name, value);
		return this;
	}

	@Override
	public ServletResponse addHeader(String name, String value) {
		this.response.addHeader(name, value);
		return this;
	}

	@Override
	public ServletResponse intHeader(String name, int value) {
		this.response.setIntHeader(name, value);
		return this;
	}

	@Override
	public ServletResponse addIntHeader(String name, int value) {
		this.response.addIntHeader(name, value);
		return this;
	}

	@Override
	public ServletResponse dateHeader(String name, long value) {
		this.response.setDateHeader(name, value);
		return this;
	}

	@Override
	public ServletResponse addDateHeader(String name, long value) {
		this.response.addDateHeader(name, value);
		return this;
	}

	@Override
	public ServletResponse length(long length) {
		this.response.setContentLengthLong(length);
		return this;
	}

	@Override
	public ServletResponse cookie(String name, String value) {
		cookie(name, "/", value, "", -1, true, true);
		return this;
	}

	@Override
	public ServletResponse cookie(String name, String path, String value, String domain, int maxAge, boolean secure, boolean httpOnly) {
		ServletCookie cookie = new ServletCookie(name, value);
		if (path != null)
			cookie.path(path);
		if (domain != null)
			cookie.domain(domain);
		cookie.maxAge(maxAge);
		cookie.secure(secure);
		cookie.httpOnly(httpOnly);
		this.response.addCookie(cookie.raw());
		return this;
	}

	@Override
	public ServletResponse removeCookie(String name) {
		cookie(name, "", "", null, 0, true, true);
		return this;
	}

	@Override
	public Render redirect(String location) {
		this.status(HttpServletResponse.SC_FOUND);
		this.header("Location", this.response.encodeRedirectURL(location));
		this.header("Connection", "close");
		return Render.EMPTY;
	}

}
