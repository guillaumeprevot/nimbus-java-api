package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
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

import fr.techgp.nimbus.server.Router;

// https://www.eclipse.org/jetty/documentation/current/
// https://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
public class JettyServer {

	private int port;
	private String keystoreFile;
	private String keystorePassword;
	private MultipartConfigElement multipart = null;
	private Server server;

	public JettyServer(int port) {
		this.port = port;
	}

	public JettyServer https(String keystoreFile, String keystorePassword) {
		this.keystoreFile = keystoreFile;
		this.keystorePassword = keystorePassword;
		return this;
	}

	public JettyServer multipart(String uploadFolder, long maxFileSize, long maxRequestSize, int fileSizeThreshold) {
		this.multipart = new MultipartConfigElement(uploadFolder, maxFileSize, maxRequestSize, fileSizeThreshold);
		return this;
	}

	public JettyServer start(Router router) throws Exception {
		this.server = init(router, this.port, this.keystoreFile, this.keystorePassword, this.multipart);
		return this;
	}

	public JettyServer stop() throws Exception {
		this.server.stop();
		this.server = null;
		return this;
	}

	/** This {@link Handler} uses a {@link Router} to handle incomming request and associated answers. */
	public static final class JettyRouterHandler extends SessionHandler {

		private final Router router;
		private final MultipartConfigElement multipart;

		public JettyRouterHandler(Router router, MultipartConfigElement multipart) {
			this.router = router;
			this.multipart = multipart;
		}

		@Override
		public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			ServletRequest req = new JettyServletRequest(request, false, this.multipart);
			ServletResponse res = new ServletResponse(response);
			this.router.process(req, res);
			try {
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

		public JettyServletRequest(HttpServletRequest request, boolean checkProxy, MultipartConfigElement multipart) {
			super(request, checkProxy);
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

	@SuppressWarnings("resource")
	protected static final Server init(Router router, int port, String keystore, String keystorePassword, MultipartConfigElement multipart) throws Exception {
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
		JettyRouterHandler handler = new JettyRouterHandler(router, multipart);
		handler.getSessionCookieConfig().setHttpOnly(true);
		server.setHandler(handler);

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

}
