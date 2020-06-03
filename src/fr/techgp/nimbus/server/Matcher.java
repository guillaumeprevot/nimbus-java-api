package fr.techgp.nimbus.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>The {@link Router} is using {@link Matcher} to select the proper {@link Route} for incoming {@link Request}. If
 * a {@link Matcher} returns true, the {@link Router} will allow the corresponding {@link Route} to alter the
 * {@link Response} and to provide a {@link Render}. It a matching {@link Route} does not provide a {@link Render},
 * the {@link Router} will look for another {@link Matcher} for the {@link Request}.</p>
 *
 * <p>The {@link Router} provides helper methods to register {@link Route} for common matching (path, get+path,
 * post+path, redirect...) but also provides generic methods to register {@link Route} using {@link Matcher} for
 * uncommon matching (based on request parameters, on the value of a header...)</p>
 *
 * @see {@link Matcher.Method} provides {@link Matcher} based on the HTTP method GET, POST...
 * @see {@link Matcher.Path} provides {@link Matcher} based on the HTTP request path
 * @see {@link Matcher.Type} provides {@link Matcher} based on the HTTP request "Accept" header
 * @see {@link Matcher#and(Matcher)} provides a matcher matching both matchers
 * @see {@link Matcher#or(Matcher)} provides a matcher matching any of the two matchers
 * @see static method {@link Matcher#not(Matcher)} provides a matcher that is the opposite of a matcher
 * @see static method {@link Matcher#all(Matcher...)} provides a matcher matching if all of the matchers return true
 * @see static method {@link Matcher#any(Matcher...)} provides a matcher matching if any of the matchers return true
 */
@FunctionalInterface
public interface Matcher {

	/** @see Matcher */
	public boolean matches(Request request);

	/** returns a new {@link Matcher} matching both the current {@link Matcher} and another {@link Matcher} */
	default public Matcher and(Matcher other) {
		return (req) -> this.matches(req) && other.matches(req);
	}

	/** returns a new {@link Matcher} matching any of the current {@link Matcher} or another {@link Matcher} */
	default public Matcher or(Matcher other) {
		return (req) -> this.matches(req) || other.matches(req);
	}

	/**
	 * This class provides pre-built {@link Matcher} instances based on HTTP method and utilitity
	 * method to build {@link Matcher} instances based on HTTP method.
	 * <ul>
	 * <li>Matcher.Method.GET : matcher instance for GET request</li>
	 * <li>Matcher.Method.POST : matcher instance for POST request</li>
	 * <li>...</li>
	 * <li>Matcher.Method.is(method) : creates a new instance matching a custom method</li>
	 * <li>Matcher.Method.in(methods...) : creates a new instance matching any of the methods</li>
	 * </ul>
	 * There is no need for matchers like "Matcher.Method.any" or "Matcher.Method.is("*")" because if all methods
	 * are accepted, simply do not filter using HTTP method. For instance :
	 * <pre>router.before("/*", (req, res) -> trace(req));</pre>
	 *
	 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
	 * @see https://tools.ietf.org/html/rfc7231#section-4 (Specifies GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE)
	 * @see https://tools.ietf.org/html/rfc5789#section-2 (Specifies PATCH)
	 * @see https://tools.ietf.org/html/rfc4918#section-9 (Specifies PROPFIND, PROPPATCH, MKCOL, COPY, MOVE, LOCK, UNLOCK for WEBDAV)
	 */
	public static class Method {

		public static final Matcher GET = is("GET");
		public static final Matcher HEAD = is("HEAD");
		public static final Matcher POST = is("POST");
		public static final Matcher PUT = is("PUT");
		public static final Matcher DELETE = is("DELETE");
		public static final Matcher CONNECT = is("CONNECT");
		public static final Matcher OPTIONS = is("OPTIONS");
		public static final Matcher TRACE = is("TRACE");
		public static final Matcher PATCH = is("PATCH");

		public static Matcher is(String method) {
			return (req) -> method.equals(req.method());
		}

		public static Matcher in(String... methods) {
			Set<String> set = new HashSet<>(Arrays.asList(methods));
			return (req) -> set.contains(req.method());
		}

	}

	/**
	 * This class provides pre-built {@link Matcher} instances and utilitity method to build {@link Matcher} instances
	 * based on request accepted types.
	 * <ul>
	 * <li>Matcher.Type.HTML : matcher instance for request accepting HTML</li>
	 * <li>Matcher.Type.JSON : matcher instance for request accepting JSON</li>
	 * <li>...</li>
	 * <li>Matcher.Type.is(type) : creates a new instance matching a custom content type</li>
	 * <li>Matcher.Type.in(types...) : creates a new instance matching any of the content types</li>
	 * </ul>
	 *
	 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept
	 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type
	 */
	public static class Type {

		public static final Matcher TEXT = is(MimeTypes.TEXT);
		public static final Matcher HTML = is(MimeTypes.HTML);
		public static final Matcher CSS = is(MimeTypes.CSS);
		public static final Matcher JS = is(MimeTypes.JS);
		public static final Matcher JSON = is(MimeTypes.JSON);

		public static Matcher is(String type) {
			return (req) -> type.equals(req.acceptType());
		}

		public static Matcher in(String... types) {
			Set<String> set = new HashSet<>(Arrays.asList(types));
			return (req) -> set.contains(req.acceptType());
		}

	}

	/**
	 * This class provides pre-built {@link Matcher} instances based on request path and utilitity
	 * method to build {@link Matcher} instances based on request path.
	 * <ul>
	 * <li>Matcher.Path.is(path) : creates a new instance matching this exact "path"</li>
	 * <li>Matcher.Path.startsWith(prefix) : creates a new instance matching path starting with "prefix"</li>
	 * <li>Matcher.Path.endsWith(suffix) : creates a new instance matching path endind with "suffix"</li>
	 * <li>Matcher.Path.params(path) : creates a new instance matching a path where dynamic parameters are inserted</li>
	 * <li>Matcher.Path.like(regexp) : creates a new instance matching the specified regexp {@link Pattern}</li>
	 * </ul>
	 * A generic method {@link Matcher.Path#of(String)} will determine what rule to apply
	 * <ul>
	 * <li>Matcher.Path.of("/hello") will resolve to Matcher.Path.is("/hello")</li>
	 * <li>Matcher.Path.of("/hello/*") will resolve to Matcher.Path.startsWith("/hello/")</li>
	 * <li>Matcher.Path.of("*.json") will resolve to Matcher.Path.endsWith(".json")</li>
	 * <li>Matcher.Path.of("/hello/:name") will resolve to Matcher.Path.params("/hello/:name")</li>
	 * </ul>
	 */
	public static class Path {

		public static final String WILDCARD = "*";
		public static final char PARAMS_PREFIX = ':';

		public static Matcher of(String path) {
			if (path.startsWith(WILDCARD))
				return endsWith(path.substring(WILDCARD.length()));
			if (path.endsWith(WILDCARD))
				return startsWith(path.substring(0, path.length() - WILDCARD.length()));
			if (path.indexOf(PARAMS_PREFIX) == -1)
				return is(path);
			return params(path);
		}

		public static Matcher is(String path) {
			return (req) -> path.equals(req.path());
		}

		public static Matcher startsWith(String prefix) {
			return (req) -> req.path().startsWith(prefix);
		}

		public static Matcher endsWith(String suffix) {
			return (req) -> req.path().endsWith(suffix);
		}

		public static Matcher params(String path) {
			String[] model = path.substring(1).split("/");
			return (req) -> {
				// System.out.println("Checking " + req.pathInfo().substring(1) + " against " + path.substring(1));
				String[] value = req.path().substring(1).split("/");
				if (value.length != model.length)
					return false;
				Map<String, String> m = new HashMap<>();
				for (int i = 0; i < model.length; i++) {
					if (model[i].charAt(0) == PARAMS_PREFIX)
						m.put(model[i], value[i]); // keep track of param values
					else if (!model[i].equals(value[i]))
						return false; // and check to fixed chunck match to model
				}
				m.entrySet().forEach(e -> req.addPathParameter(e.getKey(), e.getValue()));
				return true;
			};
		}

		public static Matcher like(String regexp) {
			Pattern r = Pattern.compile(regexp);
			return (req) -> r.matcher(req.path()).matches();
		}
	}

	/** returns a new {@link Matcher} that is the opposite of the specified {@link Matcher} */
	public static Matcher not(Matcher matcher) {
		return (req) -> !matcher.matches(req);
	}

	/** returns a new {@link Matcher} that matches a request if all of the specified matchers accept it */
	public static Matcher all(Matcher... matchers) {
		return (req) -> {
			for (Matcher m : matchers) {
				if (!m.matches(req))
					return false;
			}
			return true;
		};
	}

	/** returns a new {@link Matcher} that matches a request if any of the specified matchers accept it */
	public static Matcher any(Matcher... matchers) {
		return (req) -> {
			for (Matcher m : matchers) {
				if (m.matches(req))
					return true;
			}
			return false;
		};
	}

}
