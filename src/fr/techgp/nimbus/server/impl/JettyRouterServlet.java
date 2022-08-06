package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;

import fr.techgp.nimbus.server.MimeTypes;
import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Router;
import fr.techgp.nimbus.server.Router.WebSocketEntry;

/** This {@link Handler} uses a {@link Router} to handle incoming request and associated answers. */
public class JettyRouterServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final Router router;
	private final MultipartConfigElement multipart;
	private final SessionConfig session;
	private final boolean showStackTraces;

	public JettyRouterServlet(Router router, MultipartConfigElement multipart, SessionConfig session, boolean showStackTraces) throws Exception {
		this.router = router;
		this.multipart = multipart;
		this.session = session;
		this.showStackTraces = showStackTraces;
	}

	@Override
	public void init() throws ServletException {
		if (! this.router.websockets().isEmpty()) {
			// Retrieve the JettyWebSocketServerContainer.
			JettyWebSocketServerContainer container = JettyWebSocketServerContainer.getContainer(getServletContext());

			// Configure the JettyWebSocketServerContainer.
			//container.setMaxTextMessageSize/setMaxBinaryMessageSize/setMaxFrameSize/setIdleTimeout/setInputBufferSize/setOutputBufferSize

			// Register WebSocket endpoints.
			for (WebSocketEntry e : this.router.websockets()) {
				container.addMapping(e.path, (upgradeRequest, upgradeResponse) -> new JettyWebSocket(e.ws));
			}
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletRequest req = new JettyServletRequest(request, this.session, this.multipart);
		ServletResponse res = new ServletResponse(response);
		try {
			// Process ALL before filters
			this.router.processList(req, res, this.router.beforeFilters(), true);
			try {
				// Process routes if body is not set yet, and stop as soon as a body is set
				if (res.body() == null)
					this.router.processList(req, res, this.router.routeHandlers(), false);
			} finally {
				// Process ALL after filters
				this.router.processList(req, res, this.router.afterFilters(), true);
			}

			// Reply 404 Not Found if no route matches request
			if (res.body() == null) {
				res.type(MimeTypes.TEXT);
				res.body(Render.notFound());
			}

			// Write response to output stream
			if (res.type() == null)
				res.type(MimeTypes.HTML);

		} catch (Exception ex) {
			// Reply 500 for exceptions
			if (this.showStackTraces)
				res.body(Render.throwable(ex));
			else
				res.body(Render.string(ex.toString()));
		}

		// Save client session, if any
		JWTClientSession.save(req.clientSession(false), res);
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
	}

}
