package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import fr.techgp.nimbus.server.MimeTypes;
import fr.techgp.nimbus.server.Request;
import fr.techgp.nimbus.server.Upload;

public class ServletRequest implements Request {

	/** The wrapped request */
	private final HttpServletRequest request;
	/** The parameters, extracted from path when ":" is found, during route selection */
	private final Map<String, String> params = new HashMap<>();
	/** The cookie collections */
	private List<ServletCookie> cookies;
	/** The upload collections */
	private List<ServletUpload> uploads;
	/** The session wrapper */
	private ServletSession session;

	public ServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest raw() {
		return this.request;
	}

	@Override
	public String method() {
		return this.request.getMethod();
	}

	@Override
	public String acceptType() {
		return this.request.getHeader("Accept");
	}

	public String protocol() { // HTTP/1.1
		return this.request.getProtocol();
	}

	public String scheme() { // http
		return this.request.getScheme();
	}

	public String host() { // localhost
		return this.request.getHeader("Host");
	}

	public String referer() {
		return this.request.getHeader("Referer");
	}

	public String userAgent() {
		return this.request.getHeader("User-Agent");
	}

	public int port() { // 8080
		return this.request.getServerPort();
	}

	public String contextPath() { // /webapp
		return this.request.getContextPath();
	}

	public String servletPath() { // /servlet
		return this.request.getServletPath();
	}

	public String uri() { // /webapp/servlet/item/info/2
		return this.request.getRequestURI();
	}

	public String url() { // http://localhost:8080/webapp/servlet/item/info/2
		return this.request.getRequestURL().toString();
	}

	@Override
	public String path() {
		return this.request.getPathInfo();
	}

	@Override
	public String pathParameter(String name) {
		return this.params.get(name);
	}

	@Override
	public String pathParameter(String name, String defaultValue) {
		return this.params.getOrDefault(name, defaultValue);
	}

	@Override
	public void addPathParameter(String name, String value) {
		this.params.put(name, value);
	}

	@Override
	public String query() {
		return this.request.getQueryString();
	}

	@Override
	public String queryParameter(String name) {
		return this.request.getParameter(name);
	}

	@Override
	public String queryParameter(String name, String defaultValue) {
		return Optional.ofNullable(this.request.getParameter(name)).orElse(defaultValue);
	}

	@Override
	public String[] queryParameterValues(String name) {
		return this.request.getParameterValues(name);
	}

	@Override
	public String contentType() {
		return this.request.getContentType();
	}

	@Override
	public long contentLength() {
		return this.request.getContentLengthLong();
	}

	@Override
	public String characterEncoding() {
		return this.request.getCharacterEncoding();
	}

	@Override
	public String ip() {
		return this.request.getRemoteAddr();
	}

	@Override
	public String header(String name) {
		return this.request.getHeader(name);
	}

	@Override
	public int intHeader(String name) {
		return this.request.getIntHeader(name);
	}

	@Override
	public long dateHeader(String name) {
		return this.request.getDateHeader(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T attribute(String name) {
		return (T) this.request.getAttribute(name);
	}

	@Override
	public void attribute(String name, Object value) {
		this.request.setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		this.request.removeAttribute(name);
	}

	@Override
	public ServletCookie cookie(String name) {
		return this.cookies().stream().filter((c) -> c.name().equals(name)).findAny().orElse(null);
	}

	@Override
	public ServletCookie cookie(String name, String path) {
		return this.cookies().stream().filter((c) -> c.name().equals(name) && c.path().equals(path)).findAny().orElse(null);
	}

	@Override
	public List<ServletCookie> cookies() {
		if (this.cookies == null) {
			Cookie[] cookies = this.request.getCookies();
			this.cookies = cookies == null ? Collections.emptyList() : Arrays.stream(cookies).map(ServletCookie::new).collect(Collectors.toList());
		}
		return this.cookies;
	}

	@Override
	public Upload upload(String name) {
		return this.uploads().stream().filter((u) -> u.name().equals(name)).findAny().orElse(null);
	}

	@Override
	public List<? extends Upload> uploads(String name) {
		return this.uploads().stream().filter((u) -> u.name().equals(name)).collect(Collectors.toList());
	}

	@Override
	public List<? extends Upload> uploads() {
		if (this.uploads == null) {
			String mimetype = MimeTypes.byContentType(this.contentType());
			this.uploads = MimeTypes.MULTIPART_FORMDATA.equals(mimetype) ? loadUploads() : Collections.emptyList();
		}
		return this.uploads;
	}

	@Override
	public ServletSession session() {
		if (this.session == null)
			this.session = new ServletSession(this, this.request.getSession());
		return this.session;
	}

	@Override
	public ServletSession session(boolean create) {
		if (this.session == null)
			this.session = Optional.ofNullable(this.request.getSession(create))
					.map((s) -> new ServletSession(this, s))
					.orElse(null);
		return this.session;
	}

	/** Cette méthode peut être surchargée */
	protected List<ServletUpload> loadUploads() {
		try {
			Collection<Part> parts = this.request.getParts();
			return parts.stream().map(ServletUpload::new).collect(Collectors.toList());
		} catch (IOException | ServletException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void invalidateSession() {
		this.session = null;
	}

}
