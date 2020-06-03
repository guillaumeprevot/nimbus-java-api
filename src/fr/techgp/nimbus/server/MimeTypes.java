package fr.techgp.nimbus.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * This is a class to provide extensible and reusable MIME type management based on file extensions.
 *
 * @see https://developer.mozilla.org/fr/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
 * @see https://svn.apache.org/repos/asf/tomcat/tc9.0.x/branches/gsoc-jaspic/conf/web.xml
 * @see https://www.iana.org/assignments/media-types/media-types.xhtml
 * @see https://stackoverflow.com/questions/7854909/interface-enum-listing-standard-mime-type-constants
 * @see https://github.com/eclipse/jetty.project/blob/jetty-9.4.x/jetty-http/src/main/java/org/eclipse/jetty/http/MimeTypes.java
 */
public final class MimeTypes {

	private static final Map<String, String> mimetypes = new HashMap<>();
	private static final List<Function<String, String>> resolvers = new ArrayList<>(1);
	private static String defaultMimeType = null;

	public static final String TEXT = "text/plain";
	public static final String HTML = "text/html";
	public static final String CSS = "text/css";
	public static final String JS = "application/javascript";
	public static final String JSON = "application/json";
	public static final String BINARY = "application/octet-stream";

	/** loads some file extension MIME types from default resource <i>/fr/techgp/nimbus/server/mimetypes.conf</i> */
	public static final void loadDefaultMimeTypes() throws IOException {
		synchronized (mimetypes) {
			try (InputStream is = Utils.class.getResourceAsStream("mimetypes.properties")) {
				Properties p = new Properties();
				p.load(is);
				for (String extension : p.stringPropertyNames()) {
					if (extension != null && !extension.isBlank()) {
						String mimetype = p.getProperty(extension);
						if (mimetype != null && !mimetype.isBlank())
							mimetypes.put(extension.toLowerCase(), mimetype.toLowerCase());
					}
				}
			}
		}
	}

	/** sets the default MIME type when no type is found (defaults to null but "application/octet-stream" is common) */
	public static final void registerDefault(String mimetype) {
		defaultMimeType = mimetype;
	}

	/** registers a custom "extension to MIME type" function, for instance to include an external library */
	public static final void register(Function<String, String> resolver) {
		synchronized (resolvers) {
			resolvers.add(resolver);
		}
	}

	/** registers a MIME type for the specified extension, for instance <code>register("text/css", "css")</code> */
	public static final void register(String mimetype, String extension) {
		synchronized (mimetypes) {
			mimetypes.put(extension, mimetype);
		}
	}

	/** registers a MIME type for severals extensions, for instance <code>register("text/html", "html", "html")</code> */
	public static final void register(String mimetype, String extension, String... others) {
		synchronized (mimetypes) {
			mimetypes.put(extension, mimetype);
			for (String other : others) {
				mimetypes.put(other, mimetype);
			}
		}
	}

	/** returns the MIME type of the specified file, for instance <code>MimeTypes.byFilePath("c:\\path\\to\\file.html")</code> */
	public static final String byFilePath(String path) {
		return byPath(path, File.separatorChar);
	}

	/** returns the MIME type of the specified resource, for instance <code>MimeTypes.byResourcePath("/path/to/file.html")</code> */
	public static final String byResourcePath(String path) {
		return byPath(path, '/');
	}

	/** returns the MIME type of the "separator"-delimited path, for instance <code>MimeTypes.byPath("/path/to/file.html", '/')</code> */
	public static final String byPath(String path, char separator) {
		if (path == null || path.isBlank())
			throw new InvalidParameterException("path is required");
		int i = path.lastIndexOf(separator);
		return byName(path.substring(i + 1));
	}

	/** returns the MIME type of the specified file or resource name, for instance <code>MimeTypes.byName("file.html")</code> */
	public static final String byName(String name) {
		if (name == null || name.isBlank())
			throw new InvalidParameterException("name is required");
		int i = name.lastIndexOf('.');
		return byExtension(name.substring(i + 1));
	}

	/** returns the MIME type of the specified extension, for instance <code>MimeTypes.byExtension("html")</code> */
	public static final String byExtension(String extension) {
		if (extension == null || extension.isBlank())
			throw new InvalidParameterException("extension is required");
		String e = extension.toLowerCase();
		String r = mimetypes.get(e);
		if (r == null && ! resolvers.isEmpty()) {
			for (Function<String, String> resolver : resolvers) {
				r = resolver.apply(e);
				if (r != null)
					break;
			}
		}
		return r == null ? defaultMimeType : r;
	}

	/** returns the MIME type of the specified "Content-Type", for instance <code>MimeTypes.byContentType("text/html; charset=utf-8")</code> */
	public static final String byContentType(String type) {
		if (type == null || type.isBlank())
			throw new InvalidParameterException("type is required");
		int i = type.indexOf(';');
		return (i == -1 ? type : type.substring(0, i)).trim().toLowerCase();
	}

}
