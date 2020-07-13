package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.jetty.http.MultiPartFormInputStream;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import fr.techgp.nimbus.server.MimeTypes;
import fr.techgp.nimbus.server.Router;
import fr.techgp.nimbus.utils.ConversionUtils;

/**
 * This class is an embedded Jetty server with the ability to use a {@link Router} a it's main {@link Handler}
 *
 * @see Server
 * @see Handler
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
	public JettyServer session(String secretKeyHex, int timeout, String cookiePath, String cookieDomain) {
		if (secretKeyHex.length() != 64)
			throw new InvalidParameterException("AES key should be 256 bits (i.e. 32 bytes, i.e. 64 hexadecimal characters)");
		this.session.setSecretKey(ConversionUtils.hex2bytes(secretKeyHex));
		this.session.setTimeout(timeout);
		this.session.setCookiePath(cookiePath);
		this.session.setCookieDomain(cookieDomain);
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

	/** This {@link Handler} uses a {@link Router} to handle incoming request and associated answers. */
	public static final class JettyRouterHandler extends SessionHandler {

		private final Router router;
		private final MultipartConfigElement multipart;
		private final SessionConfig session;

		public JettyRouterHandler(Router router, MultipartConfigElement multipart, SessionConfig session) {
			this.router = router;
			this.multipart = multipart;
			this.session = session;
		}

		@Override
		public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			ServletRequest req = new JettyServletRequest(request, this.session, this.multipart);
			ServletResponse res = new ServletResponse(response);
			this.router.process(req, res);
			// Save client session, if any
			JSONClientSession.save(req.clientSession(false), res);
			try {
				// Write response
				res.body().render(req, res, StandardCharsets.UTF_8, () -> {
					try {
						return response.getOutputStream();
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				});
			} catch (Exception ex) {
				// The connection may have been closed by client.
				// Shouldn't it be an EofException ?
				// System.out.println(ex.getClass().getName() + " on " + req.path());
			}
			baseRequest.setHandled(true);
		}

	}

	/** This {@link ServletRequest} optimizes uploads by overriding {@link ServletRequest#loadUploads} */
	public static class JettyServletRequest extends ServletRequest {

		private final MultipartConfigElement multipart;

		public JettyServletRequest(HttpServletRequest request, SessionConfig session, MultipartConfigElement multipart) {
			super(request, session);
			this.multipart = multipart;
		}

		@Override
		protected List<ServletUpload> loadUploads() {
			try {
				this.attribute("org.eclipse.jetty.multipartConfig", this.multipart);
				Collection<Part> parts = this.raw().getParts();
				List<ServletUpload> uploads = new ArrayList<>();
				for (Part part : parts) {
					ServletUpload upload = new ServletUpload(part);
					configureMultiPartWithJettyInternal(upload, part);
					uploads.add(upload);
				}
				return uploads;
			} catch (IOException | ServletException ex) {
				throw new RuntimeException(ex);
			}
		}

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

	/** This method exposes internal file or bytes from Jetty's {@link MultiPartFormInputStream.MultiPart} implementation to optimize uploads. */
	protected static final boolean configureMultiPartWithJettyInternal(ServletUpload upload, Part part) {
		if (part instanceof MultiPartFormInputStream.MultiPart) {
			MultiPartFormInputStream.MultiPart mpart = (MultiPartFormInputStream.MultiPart) part;
			upload.setFile(mpart.getFile());
			upload.setBytes(mpart.getBytes());
			return true;
		}
		return false;
	}

	/** This method exposes internal file or bytes from Jetty's deprecated MultiPart implementation to optimize uploads. */
	@SuppressWarnings("deprecation")
	protected static final boolean configureMultiPartWithJettyDeprecated(ServletUpload upload, Part part) {
		if (part instanceof org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart) {
			org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart mpart = (org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart) part;
			upload.setFile(mpart.getFile());
			upload.setBytes(mpart.getBytes());
			return true;
		}
		return false;
	}

	/** This static method can extend {@link MimeTypes} to include Jetty in MIME type detection */
	public static final void registerToMimeTypes() {
		MimeTypes.register((extension) -> org.eclipse.jetty.http.MimeTypes.getDefaultMimeByExtension("file." + extension));
	}

}
