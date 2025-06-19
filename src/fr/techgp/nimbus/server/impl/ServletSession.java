package fr.techgp.nimbus.server.impl;

import jakarta.servlet.http.HttpSession;

import fr.techgp.nimbus.server.Session.ServerSession;

public class ServletSession implements ServerSession {

	private final ServletRequest request;
	private final HttpSession session;

	public ServletSession(ServletRequest request, HttpSession session) {
		this.request = request;
		this.session = session;
		if (this.session.isNew())
			this.session.setMaxInactiveInterval(request.getSessionConfig().getTimeout());
	}

	public HttpSession raw() {
		return this.session;
	}

	@Override
	public String id() {
		return this.session.getId();
	}

	@Override
	public long creationTime() {
		return this.session.getCreationTime();
	}

	@Override
	public long lastAccessedTime() {
		return this.session.getLastAccessedTime();
	}

	@Override
	public boolean isNew() {
		return this.session.isNew();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T attribute(String name) {
		return (T) this.session.getAttribute(name);
	}

	@Override
	public ServletSession attribute(String name, Object value) {
		this.session.setAttribute(name, value);
		return this;
	}

	@Override
	public ServletSession removeAttribute(String name) {
		this.session.removeAttribute(name);
		return this;
	}

	@Override
	public int maxInactiveInterval() {
		return this.session.getMaxInactiveInterval();
	}

	@Override
	public ServletSession maxInactiveInterval(int interval) {
		this.session.setMaxInactiveInterval(interval);
		return this;
	}

	@Override
	public void invalidate() {
		this.request.invalidateSession();
		this.session.invalidate();
	}

}
