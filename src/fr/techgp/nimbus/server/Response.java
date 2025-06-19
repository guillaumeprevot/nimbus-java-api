package fr.techgp.nimbus.server;

/**
 * This interface represents the outgoing response.
 */
public interface Response {

	/** returns the current status code of the response */
	public int status();
	/** sets a new status code for the response */
	public Response status(int status);

	/** returns the current content type of the response */
	public String type();
	/** sets a new content type for the response */
	public Response type(String contentType);

	/** returns the current body renderer of the response */
	public Render body();
	/** sets a new body renderer for the response */
	public Response body(Render body);

	/** returns the current value of the response header with the specified name, or null if not set yet */
	public String header(String name);
	/** sets the value of the response header with the specified name */
	public Response header(String name, String value);
	/** adds a value to the response header with the specified name, or create a new header with this value if not set yet */
	public Response addHeader(String name, String value);
	/** sets the integer value of the response header with the specified name */
	public Response intHeader(String name, int value);
	/** adds an integer value to the response header with the specified name, or create a new header with this value if not set yet */
	public Response addIntHeader(String name, int value);
	/** sets the date value of the response header with the specified name */
	public Response dateHeader(String name, long value);
	/** adds a date value to the response header with the specified name, or create a new header with this value if not set yet */
	public Response addDateHeader(String name, long value);

	/** sets the content length of the response body, i.e. specify the "Content-Length" response header for servlets */
	public Response length(long length);

	/** adds a cookie to the response with specified name and value */
	public Response cookie(String name, String value);
	/** adds a cookie to the response with specified name and value, optional path and domain, and maxAge, secure and httpOnly parameters */
	public Response cookie(String name, String path, String value, String domain, int maxAge, boolean secure, boolean httpOnly);
	/** requests a cookie to be removed on client side by setting it's value to "" and maxAge to 0 */
	public Response removeCookie(String name);

	/** prepares the response to send a temporary redirect of the client to the specified location */
	public Render redirect(String location);

}
