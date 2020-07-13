package fr.techgp.nimbus.server.impl;

import javax.servlet.http.HttpSession;

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
	public void attribute(String name, Object value) {
		this.session.setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		this.session.removeAttribute(name);
	}

	@Override
	public int maxInactiveInterval() {
		return this.session.getMaxInactiveInterval();
	}

	@Override
	public void maxInactiveInterval(int interval) {
		this.session.setMaxInactiveInterval(interval);
	}

	@Override
	public void invalidate() {
		this.request.invalidateSession();
		this.session.invalidate();
	}

}
