package fr.techgp.nimbus.server.impl;

import java.security.InvalidParameterException;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.SessionTrackingMode;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import fr.techgp.nimbus.server.MimeTypes;
import fr.techgp.nimbus.server.Router;
import fr.techgp.nimbus.utils.ConversionUtils;

/**
 * This class is an embedded Jetty {@link Server} wrapping a {@link Router} into it's main {@link Handler}
 *
 * @see https://www.eclipse.org/jetty/documentation/current/
 * @see https://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
 */
public class JettyServer {

	private int port;
	private String keystoreFile;
	private String keystorePassword;
	private MultipartConfigElement multipart = null;
	private SessionConfig session = new SessionConfig();
	private Server server;

	/** creates a Jetty server wrapper that will use the specified port when started */
	public JettyServer(int port) {
		this.port = port;
	}

	/** turns HTTPS on by specifying a keystore file and password */
	public JettyServer https(String keystoreFile, String keystorePassword) {
		this.keystoreFile = keystoreFile;
		this.keystorePassword = keystorePassword;
		return this;
	}

	/** then configures the "multipart/form-data" request body parsing */
	public JettyServer multipart(String uploadFolder, long maxFileSize, long maxRequestSize, int fileSizeThreshold) {
		this.multipart = new MultipartConfigElement(uploadFolder, maxFileSize, maxRequestSize, fileSizeThreshold);
		return this;
	}

	/** then configures the session cookie attributes */
	public JettyServer session(int timeout, String cookiePath, String cookieDomain, String secretKeyHex) {
		this.session.setTimeout(timeout);
		if (cookiePath != null)
			this.session.setCookiePath(cookiePath);
		if (cookieDomain != null)
			this.session.setCookieDomain(cookieDomain);
		if (secretKeyHex != null) {
			if (secretKeyHex.length() != 64)
				throw new InvalidParameterException("Secret key should be 256 bits (i.e. 32 bytes, i.e. 64 hexadecimal characters)");
			this.session.setSecretKey(ConversionUtils.hex2bytes(secretKeyHex));
		}
		return this;
	}

	/** starts the Jetty server using with a special {@link Handler} that will use the {@link Router} to handle requests */
	public JettyServer start(Router router) throws Exception {
		this.server = createAndStartServer(router, this.port, this.keystoreFile, this.keystorePassword, this.multipart, this.session);
		return this;
	}

	/** stops the Jetty server */
	public JettyServer stop() throws Exception {
		this.server.stop();
		this.server = null;
		return this;
	}

	/** This method creates a Jetty {@link Server} using specified handler and port and optional keystore */
	@SuppressWarnings("resource")
	protected static final Server createAndStartServer(Router router, int port, String keystore, String keystorePassword, MultipartConfigElement multipart, SessionConfig session) throws Exception {
		// Create server
		Server server = new Server();

		// Add connector
		ServerConnector connector = createConnector(server, keystore, keystorePassword);
		// Utilisation de MultiPartFormInputStream (rapide) au lieu de MultiPartInputStreamParser (legacy)
		// https://webtide.com/fast-multipart-formdata/
		connector.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration().setMultiPartFormDataCompliance(MultiPartFormDataCompliance.RFC7578);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		// Add handler
		JettyRouterHandler handler = new JettyRouterHandler(router, multipart, session);
		server.setHandler(handler);

		// Configure session management
		// https://www.eclipse.org/jetty/documentation/9.2.22.v20170531/session-management.html
		handler.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
		handler.getSessionCookieConfig().setName("nimbus-server-session"); // instead of JSESSIONID
		handler.getSessionCookieConfig().setHttpOnly(true); // no usage in JavaScript
		handler.getSessionCookieConfig().setSecure(true); // if HTTPS is enabled
		handler.getSessionCookieConfig().setMaxAge(session.getTimeout());
		handler.getSessionCookieConfig().setPath(session.getCookiePath());
		handler.getSessionCookieConfig().setDomain(session.getCookieDomain());

		// Start
		server.start();
		// server.join();
		return server;
	}

	/** This method creates an HTTPS connector if a keystore is specified, or an HTTP connector otherwise. */
	protected static final ServerConnector createConnector(Server server, String keystore, String keystorePassword) {
		if (keystore != null) {
			SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setKeyStorePath(keystore);
			if (keystorePassword != null)
				sslContextFactory.setKeyStorePassword(keystorePassword);
			return new ServerConnector(server, sslContextFactory);
		}
		return new ServerConnector(server);
	}

	/** This static method can extend {@link MimeTypes} to include Jetty in MIME type detection */
	public static final void registerToMimeTypes() {
		MimeTypes.register((extension) -> org.eclipse.jetty.http.MimeTypes.getDefaultMimeByExtension("file." + extension));
	}

}
