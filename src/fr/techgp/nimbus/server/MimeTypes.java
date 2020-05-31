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
 * Gestion personnalisée des types MIME par extension.
 * Customized MIME type managment depending on file's extensions.
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

	/** Charge les types MIME depuis la ressource par défaut <i>/fr/techgp/nimbus/server/mimetypes.conf</i> */
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

	/** Enregistre une type MIME à retourner par défaut (null au départ mais "application/octet-stream" est courant) */
	public static final void registerDefault(String mimetype) {
		defaultMimeType = mimetype;
	}

	/** Enregistre une méthode externe à prendre en compte pour la résolution des types MIME à partir d'une extension */
	public static final void register(Function<String, String> resolver) {
		synchronized (resolvers) {
			resolvers.add(resolver);
		}
	}

	/** Enregistre un type MIME pour une extension, par exemple <code>register("text/css", "css")</code> */
	public static final void register(String mimetype, String extension) {
		synchronized (mimetypes) {
			mimetypes.put(extension, mimetype);
		}
	}

	/** Enregistre un même type MIME pour plusieurs extensions, par exemple <code>register("text/html", "html", "html")</code> */
	public static final void register(String mimetype, String extension, String... others) {
		synchronized (mimetypes) {
			mimetypes.put(extension, mimetype);
			for (String other : others) {
				mimetypes.put(other, mimetype);
			}
		}
	}

	/** Cherche le type MIME du fichier indiqué, par exemple <code>MimeTypes.byFilePath("c:\\path\\to\\file.html")</code> */
	public static final String byFilePath(String path) {
		return byPath(path, File.separatorChar);
	}

	/** Cherche le type MIME de ressource indiquée, par exemple <code>MimeTypes.byResourcePath("/path/to/file.html")</code> */
	public static final String byResourcePath(String path) {
		return byPath(path, '/');
	}

	/** Cherche le type MIME du chemin indiqué, par exemple <code>MimeTypes.byPath("/path/to/file.html", '/')</code> */
	public static final String byPath(String path, char separator) {
		if (path == null || path.isBlank())
			throw new InvalidParameterException("path is required");
		int i = path.lastIndexOf(separator);
		return byName(path.substring(i + 1));
	}

	/** Cherche le type MIME du fichier indiqué, par exemple <code>MimeTypes.byName("file.html")</code> */
	public static final String byName(String name) {
		if (name == null || name.isBlank())
			throw new InvalidParameterException("name is required");
		int i = name.lastIndexOf('.');
		return byExtension(name.substring(i + 1));
	}

	/** Cherche le type MIME de l'extension indiquée, par exemple <code>MimeTypes.byExtension("html")</code> */
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

	/** Cherche le type MIME d'une valeur de Content-Type, par exemple <code>MimeTypes.byContentType("text/html; charset=utf-8")</code> */
	public static final String byContentType(String type) {
		if (type == null || type.isBlank())
			throw new InvalidParameterException("type is required");
		int i = type.indexOf(';');
		return (i == -1 ? type : type.substring(0, i)).trim().toLowerCase();
	}

}
