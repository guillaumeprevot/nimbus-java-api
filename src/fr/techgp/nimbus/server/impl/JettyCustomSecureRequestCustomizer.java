package fr.techgp.nimbus.server.impl;

import java.util.function.Consumer;

import javax.net.ssl.SSLEngine;

import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.server.QuietServletException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;

/**
 * <p>When accessing an SSL protected domain like "example.org" using an invalid SNI or it's IP address, the SecureRequestCustomizer [1] throws a
 * BadMessageException [2] error. The exception is catch by HttpChannel [3] to log an error and returns an "Invalid SNI" error to the client.</p>
 * <ol>
 * <li>https://github.com/eclipse/jetty.project/blob/574ad3b4daacf0d992f40ab780f2425ecbbac7bb/jetty-server/src/main/java/org/eclipse/jetty/server/SecureRequestCustomizer.java#L254
 * <li>https://github.com/eclipse/jetty.project/blob/574ad3b4daacf0d992f40ab780f2425ecbbac7bb/jetty-http/src/main/java/org/eclipse/jetty/http/BadMessageException.java#L22
 * <li>https://github.com/eclipse/jetty.project/blob/574ad3b4daacf0d992f40ab780f2425ecbbac7bb/jetty-server/src/main/java/org/eclipse/jetty/server/HttpChannel.java#L799
 * </ol>
 *
 * <p>Unfortunately, there is no way to customize the log format, as HttpChannel skips any error handlers</p>
 * <code><pre>
 * if (LOG.isDebugEnabled())
 * 	LOG.warn("handleException {}", _request.getRequestURI(), failure)
 * else
 * 	LOG.warn("handleException {} {}", _request.getRequestURI(), noStack.toString()); // trace comes from this line
 * </pre></code>
 *
 * <p>This results in a line like this in log file :</p>
 * <pre>
 * 29/03/2023 12:28:12 [WARN] HttpChannel - handleException /index.html org.eclipse.jetty.http.BadMessageException: 400: Invalid SNI
 * </pre>
 *
 * <p>This class will overwrite the default SecureRequestCustomizer to catch BadMessageException, then allow a custom "Invalid SNI" error
 * handler, and finally throw an embedded QuietServletException to avoid the default log behaviour. Of course, this is not an ideal
 * solution but jetty should implement a way to customize this error.</p>
 *
 * <p>See also :</p>
 * <ul>
 * <li>https://stackoverflow.com/questions/69945173/org-eclipse-jetty-http-badmessageexception-400-invalid-sni
 * <li>https://www.eclipse.org/forums/index.php/t/1109140/
 * </ul>
 */
public class JettyCustomSecureRequestCustomizer extends SecureRequestCustomizer {

	private final Consumer<Request> invalidSNIHandler;

	public JettyCustomSecureRequestCustomizer(Consumer<Request> invalidSNIHandler) {
		super();
		this.invalidSNIHandler = invalidSNIHandler;
	}

	@Override
	protected void customize(SSLEngine sslEngine, Request request) {
		try {
			super.customize(sslEngine, request);
		} catch (BadMessageException ex) {
			if (ex.getCode() == 400 && "Invalid SNI".equals(ex.getReason())) {
				// throw new BadMessageException(400, "Invalid SNI from client " + request.getRemoteAddr());
				this.invalidSNIHandler.accept(request);
				throw new RuntimeException(new QuietServletException(ex));
			}
			throw ex;
		}
	}

}
