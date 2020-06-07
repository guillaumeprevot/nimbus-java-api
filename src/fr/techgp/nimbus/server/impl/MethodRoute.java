package fr.techgp.nimbus.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import fr.techgp.nimbus.server.Cookie;
import fr.techgp.nimbus.server.Matcher;
import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Request;
import fr.techgp.nimbus.server.Response;
import fr.techgp.nimbus.server.Route;
import fr.techgp.nimbus.server.Session;
import fr.techgp.nimbus.server.Upload;
import fr.techgp.nimbus.server.Utils;

public class MethodRoute implements Route {

	private final Method method;
	private final Extractor<?>[] extractors;

	public MethodRoute(Method method) {
		super();
		this.method = method;
		this.extractors = new Extractor<?>[method.getParameterCount()];
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < this.extractors.length; i++) {
			Parameter parameter = parameters[i];
			Type type = parameter.getParameterizedType();
			Extractor<?> extractor = findExtractor(type);
			if (extractor == null)
				throw new UnsupportedOperationException("Extracting " + type + " from request is not supported");
			this.extractors[i] = extractor;
		}
	}

	@Override
	public Render handle(Request request, Response response) throws Exception {
		Parameter[] parameters = this.method.getParameters();
		Object[] values = new Object[this.extractors.length];
		for (int i = 0; i < this.extractors.length; i++) {
			try {
				values[i] = this.extractors[i].extract(request, response, parameters[i].getName());
			} catch (Exception ex) {
				System.err.println("Extracting value failed for " + parameters[i].getName() + " (" + parameters[i].getType() + ")");
				ex.printStackTrace();
				values[i] = null;
			}
		}
		Object result = this.method.invoke(null, values);
		if (result instanceof Render)
			return (Render) result;
		return null;
	}

	public static final MethodRoute to(String method) throws ClassNotFoundException, NoSuchMethodException {
		int i = method.lastIndexOf(".");
		String className = method.substring(0, i);
		String methodName = method.substring(i + 1);
		Class<?> methodClass = Class.forName(className);
		return to(methodClass, methodName);
	}

	public static final MethodRoute to(Class<?> methodClass, String methodName) throws NoSuchMethodException {
		Method[] methods = methodClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)
					&& Modifier.isStatic(methods[i].getModifiers())
					&& Modifier.isPublic(methods[i].getModifiers()))
				return new MethodRoute(methods[i]);
		}
		throw new NoSuchMethodException(methodClass.getName() + "." + methodName);
	}

	@FunctionalInterface
	public static interface Extractor<T> {
		public T extract(Request request, Response response, String name);
	}

	@FunctionalInterface
	public static interface ValueExtractor<T> {
		public T extract(Optional<String> input);
	}

	private static final Map<Type, Extractor<?>> extractorMap = new HashMap<>();
	private static final Map<Class<?>, ValueExtractor<?>> valueExtractorMap = new HashMap<>();

	private static final void addExtractor(Type type, Extractor<?> extractor) {
		extractorMap.put(type, extractor);
	}

	private static final <T> void addValueExtractor(Class<T> clazz, ValueExtractor<T> extractor) {
		valueExtractorMap.put(clazz, extractor);
	}

	static {
		// Add special support for execution context injection
		addExtractor(Request.class, (req, res, name) -> req);
		addExtractor(Response.class, (req, res, name) -> res);
		addExtractor(Upload.class, (req, res, name) -> req.upload(name));
		addExtractor(Upload[].class, (req, res, name) -> req.uploads(name).toArray(new Upload[0]));
		addExtractor(Cookie.class, (req, res, name) -> req.cookie(name));
		addExtractor(Cookie[].class, (req, res, name) -> req.cookies().toArray(new Cookie[0]));
		addExtractor(Session.class, (req, res, name) -> req.session(true));
		// see buildExtractor for Optional<Session>

		// Add support for String values
		addValueExtractor(String.class, (s) -> s.orElse(null));

		// Add support for basic java type values
		// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
		addValueExtractor(Byte.class, (s) -> s.map(Byte::valueOf).orElse(null));
		addValueExtractor(byte.class, (s) -> s.map(Byte::valueOf).orElseThrow());
		addValueExtractor(Short.class, (s) -> s.map(Short::valueOf).orElse(null));
		addValueExtractor(short.class, (s) -> s.map(Short::valueOf).orElseThrow());
		addValueExtractor(Integer.class, (s) -> s.map(Integer::valueOf).orElse(null));
		addValueExtractor(int.class, (s) -> s.map(Integer::valueOf).orElseThrow());
		addValueExtractor(Long.class, (s) -> s.map(Long::valueOf).orElse(null));
		addValueExtractor(long.class, (s) -> s.map(Long::valueOf).orElseThrow());
		addValueExtractor(Float.class, (s) -> s.map(Float::valueOf).orElse(null));
		addValueExtractor(float.class, (s) -> s.map(Float::valueOf).orElseThrow());
		addValueExtractor(Double.class, (s) -> s.map(Double::valueOf).orElse(null));
		addValueExtractor(double.class, (s) -> s.map(Double::valueOf).orElseThrow());
		addValueExtractor(Boolean.class, (s) -> s.map((v) -> "1".equals(v) || "true".equals(v) || "yes".equals(v)).orElse(null));
		addValueExtractor(boolean.class, (s) -> s.map((v) -> "1".equals(v) || "true".equals(v) || "yes".equals(v)).orElse(Boolean.FALSE));
		addValueExtractor(Character.class, (s) -> s.map((v) -> v.length() == 1 ? v.charAt(0) : null).orElse(null));
		addValueExtractor(char.class, (s) -> s.map((v) -> v.length() == 1 ? v.charAt(0) : null).orElseThrow());

		// Add support for classes easily created from strings
		addValueExtractor(BigDecimal.class, (s) -> s.map(BigDecimal::new).orElse(null));
		addValueExtractor(BigInteger.class, (s) -> s.map(BigInteger::new).orElse(null));
		addValueExtractor(Time.class, (s) -> s.map(Time::valueOf).orElse(null));
		addValueExtractor(Date.class, (s) -> s.map(Date::valueOf).orElse(null));
		addValueExtractor(Timestamp.class, (s) -> s.map(Timestamp::valueOf).orElse(null));
		//addValueExtractor(Class.class, (s) -> s.map(Class::forName).orElse(null));
		//addValueExtractor(File.class, (s) -> s.map(File::new).orElse(null));
		//addValueExtractor(URL.class, (s) -> s.map(URL::new).orElse(null));
	}

	private static final Extractor<?> findExtractor(Type type) {
		// Search for known extractors (Request, Response, Upload, Cookie, Session or cached later)
		Extractor<?> e = extractorMap.get(type);
		if (e == null) {
			// Search for simple value extractors (String, Integer, int, Double, double, ...)
			ValueExtractor<?> ve = valueExtractorMap.get(type);
			if (ve != null) {
				// Transform simple value extractors to parameter extractor
				e = (req, res, name) -> ve.extract(anyParameter(req, name));
			} else {
				// Try to dynamically build an extractor for "type"
				e = buildExtractor(type);
			}
			// Cache newly created Extractor<?> each time the same type is found
			if (e != null)
				extractorMap.put(type, e);
		}
		return e;
	}

	public static final Extractor<?> buildExtractor(Type type) {
		// Special parameter types to get content from context
		if (Request.class.equals(type))
			return (req, res, name) -> req;
		if (Response.class.equals(type))
			return (req, res, name) -> res;
		if (Upload.class.equals(type))
			return (req, res, name) -> req.upload(name);
		if (Upload[].class.equals(type))
			return (req, res, name) -> req.uploads(name).toArray(new Upload[0]);
		if (Cookie.class.equals(type))
			return (req, res, name) -> req.cookie(name);
		if (Cookie[].class.equals(type))
			return (req, res, name) -> req.cookies().toArray(new Cookie[0]);
		if (Session.class.equals(type))
			return (req, res, name) -> req.session(true);
		if (isParameterized(type, Optional.class, Session.class))
			return (req, res, name) -> Optional.ofNullable(req.session(true));

		// Try to build an extractor for ParameterizedType
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			if (Optional.class.equals(pt.getRawType())) {
				// Wrap Extractor<T> to create an extractor for Extractor<Optional<T>>
				Extractor<?> oe = findExtractor(pt.getActualTypeArguments()[0]);
				if (oe != null) {
					return (req, res, name) -> {
						try {
							Object v = oe.extract(req, res, name);
							return Optional.ofNullable(v);
						} catch (NoSuchElementException ex) {
							// missing "byte" will throw a NoSuchElementException
							return Optional.empty();
						}
					};
				}
			}
		}
		return null;
	}

	private static final boolean isParameterized(Type type, Type rawType, Type actualTypeArgument) {
		return type instanceof ParameterizedType
				&& ((ParameterizedType) type).getRawType().equals(rawType)
				&& ((ParameterizedType) type).getActualTypeArguments()[0].equals(actualTypeArgument);
	}

	/** searches for a parameter value, either in path parameters, query parameters or upload parts */
	private static final Optional<String> anyParameter(Request request, String name) {
		String s = request.pathParameter(Matcher.Path.PARAMS_PREFIX + name);
		if (s == null)
			s = request.queryParameter(name);
		if (s == null) {
			Upload u = request.upload(name);
			if (u != null) {
				try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
					try (InputStream is = u.getInputStream()) {
						Utils.copy(is, os);
						s = new String(os.toByteArray(), StandardCharsets.UTF_8);
					}
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return Optional.ofNullable(s);
	}

}
