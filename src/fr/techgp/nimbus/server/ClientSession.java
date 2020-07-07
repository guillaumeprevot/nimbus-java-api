package fr.techgp.nimbus.server;

import com.google.gson.JsonElement;

public interface ClientSession {

	/** returns the unique identifier assigned to this session */
	public String id();

	/** returns the time when this session was created */
	public long creationTime();

	/** returns the last time the client sent a request associated with this session */
	public long lastAccessedTime();

	/** returns true if the client does not yet know about the session or if the client chooses not to join the session */
	public boolean isNew();

	/** returns the JSON element bound with the specified name, or null if no element is bound under the name yet */
	public <T extends JsonElement> T attribute(String name);
	/** binds a JSON element to this session, using the specified name */
	public void attribute(String name, JsonElement value);

	/** returns the String bound with the specified name, or null if no string is bound under the name yet */
	public String stringAttribute(String name);
	/** binds a String to this session, using the specified name */
	public void stringAttribute(String name, String value);

	/** returns the Boolean bound with the specified name, or null if no Boolean is bound under the name yet */
	public Boolean booleanAttribute(String name);
	/** binds a Boolean to this session, using the specified name */
	public void booleanAttribute(String name, Boolean value);

	/** returns the Number bound with the specified name, or null if no Number is bound under the name yet */
	public Number numberAttribute(String name);
	/** binds a Number to this session, using the specified name */
	public void numberAttribute(String name, Number value);

	/** removes the object bound with the specified name from this session */
	public void removeAttribute(String name);

	/** returns the maximum time interval, in seconds, that the servlet container will keep this session open between client accesses */
	public int maxInactiveInterval();

	/** specifies the time, in seconds, between client requests before the servlet container will invalidate this session */
	public void maxInactiveInterval(int interval);

	/** invalidates this session then unbinds any objects bound to it */
	public void invalidate();

}
