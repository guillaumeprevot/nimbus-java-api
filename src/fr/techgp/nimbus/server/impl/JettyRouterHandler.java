package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.websocket.server.NativeWebSocketConfiguration;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;

import fr.techgp.nimbus.server.MimeTypes;
import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Router;
import fr.techgp.nimbus.server.Router.WebSocketEntry;
import fr.techgp.nimbus.server.render.RenderWebSocketUpgrade;

/** This {@link Handler} uses a {@link Router} to handle incoming request and associated answers. */
public class JettyRouterHandler extends SessionHandler {

	private final Router router;
	private final MultipartConfigElement multipart;
	private final SessionConfig session;
	private final WebSocketServerFactory wsFactory;

	public JettyRouterHandler(Router router, MultipartConfigElement multipart, SessionConfig session) throws Exception {
		this.router = router;
		this.multipart = multipart;
		this.session = session;
		if (! router.websockets().isEmpty()) {
			WebSocketServerFactory factory = new WebSocketServerFactory();
			//factory.getPolicy().setIdleTimeout/setMaxTextMessageSize/setMaxBinaryMessageSize/setInputBufferSize(...);
			factory.start();
			NativeWebSocketConfiguration configuration = new NativeWebSocketConfiguration(factory);
			for (WebSocketEntry e : router.websockets()) {
				configuration.addMapping(new ServletPathSpec(e.path), (req, res) -> new JettyWebSocket(e.ws));
			}
			this.wsFactory = factory;
		} else {
			this.wsFactory = null;
		}
	}

	@Override
	public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		ServletRequest req = new JettyServletRequest(request, this.session, this.multipart);
		ServletResponse res = new ServletResponse(response);
		try {
			// Process ALL before filters
			this.router.processList(req, res, this.router.beforeFilters(), true);
			try {
				// Process routes if body is not set yet, and stop as soon as a body is set
				if (res.body() == null)
					this.router.processList(req, res, this.router.routeHandlers(), false);

				// Process WebSockets, if any
				if (res.body() == null && this.wsFactory != null) {
					if (this.wsFactory.isUpgradeRequest(request, response)) {
						for (WebSocketEntry e : this.router.websockets()) {
							if (e.path.equalsIgnoreCase(req.path())) {
								if (this.wsFactory.acceptWebSocket((req2, res2) -> new JettyWebSocket(e.ws), request, response)) {
									// Set body as marker to avoid "NotFound" response
									res.body(new RenderWebSocketUpgrade());
								}
								break;
							}
						}
					}
				}
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
			res.body(Render.throwable(ex));
		}
		if (res.body() == null)
			return; // next handler

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
		baseRequest.setHandled(true);
	}

}
