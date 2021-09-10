package fr.techgp.nimbus.server;

import java.util.ArrayList;
import java.util.List;

import fr.techgp.nimbus.server.impl.JettyServer;

/**
 * <p>This class plays a center role in the routing API :</p>
 * <ul>
 * <li>first, we create a {@link Router}</li>
 * <li>then, we configure the different {@link Route} with associated {@link Matcher} for the application</li>
 * <li>then, we can start a server (for instance the included {@link JettyServer}) to serve HTTP request</li>
 * </ul>
 *
 * <p>The {@link Router} includes 3 kind of {@link Route} to handle 3 steps in the routing :</p>
 * <ul>
 * <li><i>before filters</i> : they are all executed at the beginning, when a request comes in</li>
 * <li><i>routes handlers</i> : they are executed in order, until one of them sets the response body</li>
 * <li><i>after filters</i> : they are all executed at the end, in a "finally" block</li>
 * </ul>
 * <p>The {@link Router} also supports {@link WebSocket} entries</p>
 */
public class Router {

	/** This class is a simple container for a {@link Route} and his associated {@link Matcher} */
	public static final class RouteEntry {
		public Matcher matcher;
		public Route route;
	}

	/** This class is a simple container for a {@link WebSocket} and his associated {@link String} path */
	public static final class WebSocketEntry {
		public String path;
		public WebSocket ws;
	}

	/** This interface is used to walk through any of the router collection, until consume return true */
	public static interface EntryConsumer<T> {
		public boolean consume(T entry) throws Exception;
	}

	private final List<RouteEntry> beforeFilters = new ArrayList<>();
	private final List<RouteEntry> routeHandlers = new ArrayList<>();
	private final List<RouteEntry> afterFilters = new ArrayList<>();
	private final List<WebSocketEntry> websockets = new ArrayList<>();

	/** walks through the list of {@link RouteEntry} to find matching {@link Route} using {@link Matcher} */
	public void processList(Request request, Response response, List<RouteEntry> entries, boolean processAll) throws Exception {
		for (RouteEntry entry : entries) {
			if (entry.matcher.matches(request)) {
				try {
					// Call the route
					Render body = entry.route.handle(request, response);
					// The route may return null or return a Render
					if (body != null)
						response.body(body);
				} catch (Render.Exception ex) {
					// The route can also throw an exception providing the Render
					response.body(ex.get());
				}
				// Stop when the body is set, if asked to
				if (response.body() != null && !processAll)
					break;
			}
		}
	}

	/** adds a <i>before filter</i> matching the specified "path" */
	public Router before(String path, Route filter) {
		return before(Matcher.Path.of(path), filter);
	}

	/** adds a <i>before filter</i> matching the specified "path" and specified HTTP "method" */
	public Router before(String method, String path, Route filter) {
		return before(Matcher.Method.is(method).and(Matcher.Path.of(path)), filter);
	}

	/** adds a <i>before filter</i> with a custom {@link Matcher} */
	public Router before(Matcher matcher, Route filter) {
		RouteEntry e = new RouteEntry();
		e.matcher = matcher;
		e.route = filter;
		this.beforeFilters.add(e);
		return this;
	}

	/** returns the collection of <i>before filters</i> */
	public List<RouteEntry> beforeFilters() {
		return this.beforeFilters;
	}

	/** adds a <i>route handler</i> matching the specified "path" */
	public Router route(String path, Route route) {
		return route(Matcher.Path.of(path), route);
	}

	/** adds a <i>route handler</i> matching the specified "path" and specified HTTP "method" */
	public Router route(String method, String path, Route route) {
		return route(Matcher.Method.is(method).and(Matcher.Path.of(path)), route);
	}

	/** adds a <i>route handler</i> with a custom {@link Matcher} */
	public Router route(Matcher matcher, Route route) {
		RouteEntry e = new RouteEntry();
		e.matcher = matcher;
		e.route = route;
		this.routeHandlers.add(e);
		return this;
	}

	/** returns the collection of <i>route handlers</i> */
	public List<RouteEntry> routeHandlers() {
		return this.routeHandlers;
	}

	/** adds an <i>after filter</i> matching the specified "path" */
	public Router after(String path, Route filter) {
		return after(Matcher.Path.of(path), filter);
	}

	/** adds an <i>after filter</i> matching the specified "path" and specified HTTP "method" */
	public Router after(String method, String path, Route filter) {
		return after(Matcher.Method.is(method).and(Matcher.Path.of(path)), filter);
	}

	/** adds an <i>after filter</i> with a custom {@link Matcher} */
	public Router after(Matcher matcher, Route filter) {
		RouteEntry e = new RouteEntry();
		e.matcher = matcher;
		e.route = filter;
		this.afterFilters.add(e);
		return this;
	}

	/** returns the collection of <i>after filters</i> */
	public List<RouteEntry> afterFilters() {
		return this.afterFilters;
	}

	/** adds a <i>route handler</i> matching the specified "path" and GET HTTP "method" */
	public Router get(String path, Route route) {
		return route(Matcher.Method.GET.and(Matcher.Path.of(path)), route);
	}

	/** adds a <i>route handler</i> matching the specified "path" and POST HTTP "method" */
	public Router post(String path, Route route) {
		return route(Matcher.Method.POST.and(Matcher.Path.of(path)), route);
	}

	/** adds a <i>route handler</i> matching the specified "path" and PUT HTTP "method" */
	public Router put(String path, Route route) {
		return route(Matcher.Method.PUT.and(Matcher.Path.of(path)), route);
	}

	/** adds a <i>route handler</i> matching the specified "path" and DELETE HTTP "method" */
	public Router delete(String path, Route route) {
		return route(Matcher.Method.DELETE.and(Matcher.Path.of(path)), route);
	}

	/** adds a <i>route handler</i> that redirects from one path to another, whatever the HTTP method */
	public Router redirect(String from, String to) {
		return route(Matcher.Path.is(from), (req, resp) -> Render.redirect(to));
	}

	/** registers a WebSocket accessible at the specified path */
	public Router websocket(String path, WebSocket webSocket) {
		WebSocketEntry e = new WebSocketEntry();
		e.path = path;
		e.ws = webSocket;
		this.websockets.add(e);
		return this;
	}

	/** returns the collection of <i>websockets</i> */
	public List<WebSocketEntry> websockets() {
		return this.websockets;
	}

}
