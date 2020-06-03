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
 */
public class Router {

	public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";

	/** This class is a simple container for a {@link Route} and his associated {@link Matcher} */
	private static final class RouteEntry {
		public Matcher matcher;
		public Route route;
	}

	private List<RouteEntry> beforeFilters = new ArrayList<>();
	private List<RouteEntry> routeHandlers = new ArrayList<>();
	private List<RouteEntry> afterFilters = new ArrayList<>();

	/** handles a {@link Request} to prepare the {@link Response} using registered {@link Route} lists */
	public void process(Request request, Response response) {
		try {
			// Process ALL before filters
			processList(request, response, this.beforeFilters, true);
			try {
				// Process routes if body is not set yet, and stop as soon as a body is set
				if (response.body() == null)
					processList(request, response, this.routeHandlers, false);
			} finally {
				// Process ALL after filters
				processList(request, response, this.afterFilters, true);
			}

			// Reply 404 Not Found if no route matches request
			if (response.body() == null) {
				response.type(DEFAULT_CONTENT_TYPE);
				response.body(Render.notFound());
			}

			// Write response to output stream
			if (response.type() == null)
				response.type(DEFAULT_CONTENT_TYPE);

		} catch (Exception ex) {
			// Reply 500 for exceptions
			response.body(Render.throwable(ex));
		}
	}

	/** walks through the list of {@link RouteEntry} to find matching {@link Route} using {@link Matcher} */
	private void processList(Request request, Response response, List<RouteEntry> entries, boolean processAll) throws Exception {
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

	/** adds a <i>route handler</i> matching the specified "path" and GET HTTP "method" */
	public Router get(String path, Route route) {
		return route(Matcher.Method.GET.and(Matcher.Path.of(path)), route);
	}

	/** adds a <i>route handler</i> matching the specified "path" and POST HTTP "method" */
	public Router post(String path, Route route) {
		return route(Matcher.Method.POST.and(Matcher.Path.of(path)), route);
	}

	/** adds a <i>route handler</i> that redirects from one path to another, whatever the HTTP method */
	public Router redirect(String from, String to) {
		return route(Matcher.Path.is(from), (req, resp) -> Render.redirect(to));
	}

}
