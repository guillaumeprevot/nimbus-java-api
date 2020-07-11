package fr.techgp.nimbus.server;

public interface Session {

	/** returns the unique identifier assigned to this session */
	public String id();

	/** returns the time when this session was created */
	public long creationTime();

	/** returns the last time the client sent a request associated with this session */
	public long lastAccessedTime();

	/** returns true if the client does not yet know about the session or if the client chooses not to join the session */
	public boolean isNew();

	/** returns the object bound with the specified name, or null if no object is bound under the name yet */
	public <T> T attribute(String name);

	/** binds an object to this session, using the specified name */
	public void attribute(String name, Object value);

	/** removes the object bound with the specified name from this session */
	public void removeAttribute(String name);

	/** returns the maximum time interval, in seconds, that the servlet container will keep this session open between client accesses */
	public int maxInactiveInterval();

	/** specifies the time, in seconds, between client requests before the servlet container will invalidate this session */
	public void maxInactiveInterval(int interval);

	/** invalidates this session then unbinds any objects bound to it */
	public void invalidate();

	/** Marker interface for server-side sessions */
	public static interface ServerSession extends Session {
		//
	}

	/** Marker interface for client-side sessions */
	public static interface ClientSession extends Session {
		//
	}

}
