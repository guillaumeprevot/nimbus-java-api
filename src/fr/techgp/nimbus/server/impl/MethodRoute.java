package fr.techgp.nimbus.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import fr.techgp.nimbus.server.Cookie;
import fr.techgp.nimbus.server.Matcher;
import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Request;
import fr.techgp.nimbus.server.Response;
import fr.techgp.nimbus.server.Route;
import fr.techgp.nimbus.server.Session.ClientSession;
import fr.techgp.nimbus.server.Session.ServerSession;
import fr.techgp.nimbus.server.Upload;
import fr.techgp.nimbus.server.Utils;

public class MethodRoute implements Route {

	private final Method method;
	private final Object instance;
	private final Extractor<?>[] extractors;

	public MethodRoute(Method method, Object instance) {
		super();
		if (method == null)
			throw new UnsupportedOperationException("The method is required");
		if (! Modifier.isPublic(method.getModifiers()))
			throw new UnsupportedOperationException("The method must be public");
		if (instance == null && !Modifier.isStatic(method.getModifiers()))
			throw new UnsupportedOperationException("The method must be static when the instance is null");
		if (instance != null && Modifier.isStatic(method.getModifiers()))
			throw new UnsupportedOperationException("The method can not be static when the instance is set");
		this.method = method;
		this.instance = instance;
		this.extractors = new Extractor<?>[method.getParameterCount()];
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < this.extractors.length; i++) {
			Parameter parameter = parameters[i];
			Type type = parameter.getParameterizedType();
			Extractor<?> extractor = findExtractor(type);
			if (extractor == null)
				throw new UnsupportedOperationException("The parameter type \"" + type + "\" is not supported");
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
				throw new ReflectiveOperationException("Failed to extract parameter " + parameters[i].getName(), ex);
			}
		}
		try {
			Object result = this.method.invoke(this.instance, values);
			if (result instanceof Render)
				return (Render) result;
			return null;
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
			throw new ReflectiveOperationException("Failed to invoke method " + this.method.getDeclaringClass().getSimpleName() + "." + this.method.getName(), ex);
		}
	}

	public static final MethodRoute to(String method) throws ClassNotFoundException, NoSuchMethodException {
		int i = method.lastIndexOf(".");
		String className = method.substring(0, i);
		String methodName = method.substring(i + 1);
		Class<?> methodClass = Class.forName(className);
		return new MethodRoute(find(methodClass, methodName, true), null);
	}

	public static final MethodRoute to(Class<?> methodClass, String methodName) throws NoSuchMethodException {
		return new MethodRoute(find(methodClass, methodName, true), null);
	}

	public static final MethodRoute to(Object instance, String methodName) throws NoSuchMethodException {
		return new MethodRoute(find(instance.getClass(), methodName, false), instance);
	}

	private static final Method find(Class<?> methodClass, String methodName, boolean isStatic) throws NoSuchMethodException {
		Method[] methods = methodClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)
					&& isStatic == Modifier.isStatic(methods[i].getModifiers())
					&& Modifier.isPublic(methods[i].getModifiers()))
				return methods[i];
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
	private static final Map<Class<? extends Collection<?>>, Supplier<Collection<Object>>> collectionSupplierMap = new HashMap<>();

	public static final void addExtractor(Type type, Extractor<?> extractor) {
		extractorMap.put(type, extractor);
	}

	public static final <T> void addValueExtractor(Class<T> clazz, ValueExtractor<T> extractor) {
		valueExtractorMap.put(clazz, extractor);
	}

	public static final <T extends Collection<?>> void addCollectionSupplier(Class<T> clazz, Supplier<Collection<Object>> supplier) {
		collectionSupplierMap.put(clazz, supplier);
	}

	static {
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

		// Add some collection suppliers
		addCollectionSupplier(Set.class, HashSet::new);
		addCollectionSupplier(HashSet.class, HashSet::new);
		addCollectionSupplier(SortedSet.class, TreeSet::new);
		addCollectionSupplier(TreeSet.class, TreeSet::new);
		addCollectionSupplier(List.class, ArrayList::new);
		addCollectionSupplier(ArrayList.class, ArrayList::new);
		addCollectionSupplier(Collection.class, ArrayList::new);
	}

	private static final Extractor<?> findExtractor(Type type) {
		// Search for known extractors (Request, Response, Upload, Cookie, Session or cached later)
		Extractor<?> e = extractorMap.get(type);
		if (e == null) {
			// Try to dynamically build an extractor for "type"
			e = buildExtractor(type);
			// Cache newly created Extractor<?> each time the same type is found
			if (e != null)
				extractorMap.put(type, e);
		}
		return e;
	}

	private static final Extractor<?> buildExtractor(Type type) {
		// Special extractors for context
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
		if (ServerSession.class.equals(type))
			return (req, res, name) -> req.session(true);
		if (isParameterized(type, Optional.class, ServerSession.class))
			return (req, res, name) -> Optional.ofNullable(req.session(false));
		if (ClientSession.class.equals(type))
			return (req, res, name) -> req.clientSession(true);
		if (isParameterized(type, Optional.class, ClientSession.class))
			return (req, res, name) -> Optional.ofNullable(req.clientSession(false));

		// Try to build an extractor for Class
		if (type instanceof Class<?>) {
			Class<?> classType = (Class<?>) type;

			// Extractors for simple values
			ValueExtractor<?> valueExtractor = valueExtractorMap.get(classType);
			if (valueExtractor != null)
				return (req, res, name) -> valueExtractor.extract(anyParameter(req, name).filter((s) -> !s.isBlank()));

			// Extractors for Enum values
			if (classType.isEnum())
				return (req, res, name) -> anyParameter(req, name).map((s) -> asEnum(classType, s)).orElse(null);

			// Extractors for simple value arrays
			if (classType.isArray()) {
				ValueExtractor<?> elementExtractor = valueExtractorMap.get(classType.getComponentType());
				if (elementExtractor != null) {
					return (req, res, name) -> {
						String[] parameterValues = req.queryParameterValues(name);
						if (parameterValues == null)
							return null;
						Object array = Array.newInstance(classType.getComponentType(), parameterValues.length);
						for (int i = 0; i < parameterValues.length; i++) {
							Object element = elementExtractor.extract(Optional.ofNullable(parameterValues[i]).filter((s) -> !s.isBlank()));
							Array.set(array, i, element);
						}
						return array;
					};
				}
			}
		}

		// Try to build an extractor for ParameterizedType
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			// Extractors for Optional<T> using Extractors for T
			if (Optional.class.equals(parameterizedType.getRawType())) {
				Extractor<?> optionalExtractor = findExtractor(parameterizedType.getActualTypeArguments()[0]);
				if (optionalExtractor != null) {
					return (req, res, name) -> {
						try {
							Object optionalValue = optionalExtractor.extract(req, res, name);
							return Optional.ofNullable(optionalValue);
						} catch (NoSuchElementException ex) {
							// missing "byte" will throw a NoSuchElementException
							return Optional.empty();
						}
					};
				}
			}

			// Extractors for Collections
			if (parameterizedType.getRawType() instanceof Class<?>) {
				Class<?> collectionClass = (Class<?>) parameterizedType.getRawType();
				Supplier<Collection<Object>> collectionSupplier = collectionSupplierMap.get(collectionClass);

				if ((collectionSupplier != null) && (parameterizedType.getActualTypeArguments()[0] instanceof Class<?>)) {
					Class<?> elementClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
					ValueExtractor<?> elementExtractor = valueExtractorMap.get(elementClass);

					if (elementExtractor != null) {
						return (req, res, name) -> {
							String[] parameterValues = req.queryParameterValues(name);
							if (parameterValues == null)
								return null;
							Collection<Object> collection = collectionSupplier.get();
							for (int i = 0; i < parameterValues.length; i++) {
								Object element = elementExtractor.extract(Optional.ofNullable(parameterValues[i]).filter((s) -> !s.isBlank()));
								collection.add(element);
							}
							return collection;
						};
					}
				}
			}
		}
		// TODO MethodRoute : POJO, GenericArrayType ?, WildcardType ?
		return null;
	}

	@SuppressWarnings("unchecked")
	private static final <T extends Enum<T>> T asEnum(Class<?> clazz, String value) {
		return Enum.valueOf((Class<T>) clazz, value);
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
