package fr.techgp.nimbus.server;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import fr.techgp.nimbus.server.Session.ClientSession;
import fr.techgp.nimbus.server.Session.ServerSession;

/**
 * This interface represents the incoming request.
 *
 * Let's say http://localhost:8080/webapp/servlet/item/info/2,param1=value1&param2=21&param2=22
 */
public interface Request {

	/** returns the name of the HTTP method used for this request (https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) */
	public String method();

	/** returns the value of the "Accept" HTTP header (https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept) */
	public String acceptType();

	/** returns the path for this request, i.e. "/item/info/2" in the example above (see {@link HttpServletRequest#getPathInfo()} */
	public String path();

	/** returns the parameter value extracted from the path or null if not found, i.e. :itemId=>2 and :toto=>null. */
	public String pathParameter(String name);
	/** returns the parameter value extracted from the path or "defaultValue" if not found, i.e. :itemId=>2 and :toto=>defaultValue. */
	public String pathParameter(String name, String defaultValue);
	/** sets a parameter value as extracted by the current matching {@link Matcher} in {@link Matcher.Path#params(String)} */
	public void addPathParameter(String name, String value);

	/** returns the query for this request, i.e. "param1=value1&param2=21&param2=22" in the exemple above (see {@link HttpServletRequest#getQueryString()} */
	public String query();

	/** returns the parameter value extracted from the query or null if not found, i.e. param1=>value1, param2=>21 and toto=>null. */
	public String queryParameter(String name);
	/** returns the parameter value extracted from the query or "defaultValue" if not found, i.e. param1=>value1, param2=>21 and toto=>defaultValue. */
	public String queryParameter(String name, String defaultValue);
	/** returns the parameter values extracted from the query or null if no value is found, i.e. param1=>[value1], param2=>[21,22] and toto=>null. */
	public String[] queryParameterValues(String name);

	/** default implementation to extract a parameter into an {@link Object} using a mapping {@link Function} and default value */
	default <T> T queryParameterObject(String name, Function<String, T> map, T defaultValue) {
		return Optional.ofNullable(queryParameter(name)).filter(s -> s.trim().length() > 0).map(map).orElse(defaultValue);
	}
	/** default implementation to extract a boolean parameter */
	default boolean queryParameterBoolean(String name, boolean defaultValue) {
		return defaultValue ? !"false".equals(queryParameter(name)) : "true".equals(queryParameter(name));
	}
	/** default implementation to extract a {@link Boolean} parameter */
	default Boolean queryParameterBoolean(String name, Boolean defaultValue) {
		return queryParameterObject(name, Boolean::valueOf, defaultValue);
	}
	/** default implementation to extract a long parameter */
	default long queryParameterLong(String name, long defaultValue) {
		return Optional.ofNullable(queryParameter(name)).stream().mapToLong(Long::parseLong).findAny().orElse(defaultValue);
	}
	/** default implementation to extract a {@link Long} parameter */
	default Long queryParameterLong(String name, Long defaultValue) {
		return queryParameterObject(name, Long::valueOf, defaultValue);
	}
	/** default implementation to extract a int parameter */
	default int queryParameterInteger(String name, int defaultValue) {
		return Optional.ofNullable(queryParameter(name)).stream().mapToInt(Integer::parseInt).findAny().orElse(defaultValue);
	}
	/** default implementation to extract a {@link Integer} parameter */
	default Integer queryParameterInteger(String name, Integer defaultValue) {
		return queryParameterObject(name, Integer::valueOf, defaultValue);
	}

	/** returns the content type of the of the body of the request, or null if the type is not known (i.e. {@link ServletRequest#getContentType()}) */
	public String contentType();
	/** returns the available length, in bytes, of the request body, or -1L if the length is not known (i.e. {@link ServletRequest#getContentLengthLong()} */
	public long contentLength();
	/** returns the name of the character encoding used in the body of this request (i.e. {@link ServletRequest#getCharacterEncoding()} */
	public String characterEncoding();
	/** returns the client address (i.e. {@link ServletRequest#getRemoteAddr()}) */
	public String ip();

	/** returns the value of the specified request header as a String */
	public String header(String name);
	/** returns the value of the specified request header as an int */
	public int intHeader(String name);
	/** returns the value of the specified request header as a long value that represents a Date object */
	public long dateHeader(String name);

	/** returns the object bound with the specified name, or null if no object is bound under the name yet */
	public <T> T attribute(String name);
	/** binds an object to this request, using the specified name */
	public void attribute(String name, Object value);
	/** removes the object bound with the specified name from this request */
	public void removeAttribute(String name);

	/** returns the cookie with the specified name, or null if no cookie exists with this name */
	public Cookie cookie(String name);
	/** returns the cookie with the specified name and path, or null if no cookie exists with this name and path */
	public Cookie cookie(String name, String path);
	/** returns the list of {@link Cookie} objects sent with this request */
	public List<? extends Cookie> cookies();

	/** returns the {@link Upload} part with the specified name, or null if no part exists with this name in the request body */
	public Upload upload(String name);
	/** returns the list of {@link Upload} parts with the specified name extracted from the request body */
	public List<? extends Upload> uploads(String name);
	/** returns the list of {@link Upload} parts extracted from the request body */
	public List<? extends Upload> uploads();

	/** returns the current {@link Session} associated with this request, or if the request does not have a session, creates one */
	public ServerSession session();
	/** returns the current {@link Session} associated with this request, or if there is no current session and create is true, returns a new session */
	public ServerSession session(boolean create);

	/** returns the current client {@link Session} associated with this request, or if the request does not have a session, creates one */
	public ClientSession clientSession();
	/** returns the current client {@link Session} associated with this request, or if there is no current session and create is true, returns a new session */
	public ClientSession clientSession(boolean create);

}
