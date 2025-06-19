package fr.techgp.nimbus.server;

/** This is an interface for a cookie matching the class {@link jakarta.servlet.http.Cookie} */
public interface Cookie {

	/** returns the name of the cookie */
	public String name();

	/** returns the path on the server to which the browser returns this cookie */
	public String path();

	/** specifies a path for the cookie to which the client should return the cookie */
	public Cookie path(String path);

	/** gets the current value of this cookie */
	public String value();

	/** assigns a new value to this cookie */
	public Cookie value(String value);

	/** gets the domain name of this Cookie */
	public String domain();

	/** specifies the domain within which this cookie should be presented */
	public Cookie domain(String domain);

	/** gets the maximum age in seconds of this cookie */
	public int maxAge();

	/** specifies the maximum age in seconds for this cookie */
	public Cookie maxAge(int maxAge);

	/** returns true if the browser is sending cookies only over a secure protocol, or false if the browser can send cookies using any protocol */
	public boolean secure();

	/** indicates to the browser whether the cookie should only be sent using a secure protocol, such as HTTPS or SSL */
	public Cookie secure(boolean secure);

	/** returns true if this cookie has been marked as HttpOnly */
	public boolean httpOnly();

	/** marks or unmarks this cookie as HttpOnly */
	public Cookie httpOnly(boolean httpOnly);

}
