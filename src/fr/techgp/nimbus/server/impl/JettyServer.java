package fr.techgp.nimbus.server.impl;

import java.security.InvalidParameterException;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.SessionTrackingMode;

import org.eclipse.jetty.http.HttpCookie.SameSite;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

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
	private Consumer<Request> invalidSNIHandler;
	private MultipartConfigElement multipart = null;
	private SessionConfig session = new SessionConfig();
	private boolean showStackTraces = false;
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

	/** then configures an optional "Invalid SNI" error handler **/
	public JettyServer invalidSNIHandler(Consumer<Request> invalidSNIHandler) {
		this.invalidSNIHandler = invalidSNIHandler;
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

	/** then configures error management (limited to stack traces for now) */
	public JettyServer errors(boolean showStackTraces) {
		this.showStackTraces = showStackTraces;
		return this;
	}

	/** starts the Jetty server using with a special {@link Handler} that will use the {@link Router} to handle requests */
	public JettyServer start(Router router) throws Exception {
		this.server = createAndStartServer(router, this.port, this.keystoreFile, this.keystorePassword, this.invalidSNIHandler, this.multipart, this.session, this.showStackTraces);
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
	protected static final Server createAndStartServer(Router router, int port, String keystore, String keystorePassword, Consumer<Request> invalidSNIHandler,
			MultipartConfigElement multipart, SessionConfig session, boolean showStackTraces) throws Exception {
		// Create server
		Server server = new Server();

		// Create connector, with optional HTTPS
		ServerConnector connector;
		if (keystore != null) {
			HttpConfiguration httpConfiguration = new HttpConfiguration();
			if (invalidSNIHandler != null)
				httpConfiguration.addCustomizer(new JettyCustomSecureRequestCustomizer(invalidSNIHandler));
			HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfiguration);

			SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setKeyStorePath(keystore);
			if (keystorePassword != null)
				sslContextFactory.setKeyStorePassword(keystorePassword);
			connector = new ServerConnector(server, sslContextFactory, httpConnectionFactory);
		} else {
			connector = new ServerConnector(server);
		}

		// Activate fast MultiPartFormInputStream rather than legacy MultiPartInputStreamParser
		// https://webtide.com/fast-multipart-formdata/
		// This setting is important in JettyOptimizedUpload to ensure an optimal upload's handling
		HttpConnectionFactory cf = connector.getConnectionFactory(HttpConnectionFactory.class);
		cf.getHttpConfiguration().setMultiPartFormDataCompliance(MultiPartFormDataCompliance.RFC7578);

		// Register the connector with the specified port number
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		// Add handler
		ServletContextHandler handler = new ServletContextHandler(server, "/");
		server.setHandler(handler);

		// Ensure that JettyWebSocketServletContainerInitializer is initialized,
		// to setup the JettyWebSocketServerContainer for this web application context.
		JettyWebSocketServletContainerInitializer.configure(handler, null);

		// Add router Servlet
		handler.addServlet(new ServletHolder(new JettyRouterServlet(router, multipart, session, showStackTraces)), "/*");

		// Configure session management
		SessionHandler shandler = new SessionHandler();
		shandler.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
		shandler.getSessionCookieConfig().setName("nimbus-server-session"); // instead of JSESSIONID
		shandler.getSessionCookieConfig().setHttpOnly(true); // no usage in JavaScript
		shandler.getSessionCookieConfig().setSecure(true); // if HTTPS is enabled
		shandler.getSessionCookieConfig().setMaxAge(session.getTimeout());
		shandler.getSessionCookieConfig().setPath(session.getCookiePath());
		shandler.getSessionCookieConfig().setDomain(session.getCookieDomain());
		shandler.setSameSite(SameSite.STRICT);
		handler.setSessionHandler(shandler);

		// Start
		server.start();
		// server.join();
		return server;
	}

	/** This static method can extend {@link MimeTypes} to include Jetty in MIME type detection */
	public static final void registerToMimeTypes() {
		MimeTypes.register((extension) -> org.eclipse.jetty.http.MimeTypes.getDefaultMimeByExtension("file." + extension));
	}

}
