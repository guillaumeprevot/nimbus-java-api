package fr.techgp.nimbus.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class Utils {

	private Utils() {
		//
	}

	/**
	 * An utility function that writes an {@link InputStream} to an {@link OutputStream} using a 1MB buffer.
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		int n;
		byte[] buffer = new byte[1024 * 1024];
		while ((n = is.read(buffer)) != -1) {
			os.write(buffer, 0, n);
		}
	}

	/** Get HTTP method for this request (and checks potential proxy headers) */
	public static final String extractMethodWithProxy(Request request) {
		String r = request.header("X-HTTP-Method");
		if (r != null)
			return r;
		r = request.header("X-HTTP-Method-Override");
		if (r != null)
			return r;
		r = request.header("X-METHOD-OVERRIDE");
		if (r != null)
			return r;
		return request.method();
	}

	/** Get the client address for this request (and checks potential proxy headers) */
	public static final String extractIPWithProxy(Request request) {
		String r = request.header("X-Real-IP");
		if (r != null)
			return r;
		r = request.header("X-Forwarded-For");
		if (r != null)
			return r.split(",")[0].trim();
		return request.ip();
	}

	/** Get the host name used by client for this request (and checks potential proxy headers) */
	public static final String extractHostWithProxy(Request request) {
		String r = request.header("X-Forwarded-Host");
		if (r != null)
			return r.split(",")[0].trim();
		return request.header("Host");
	}

}
