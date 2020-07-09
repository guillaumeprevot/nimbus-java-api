package fr.techgp.nimbus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtils {

	private IOUtils() {
		//
	}

	/** writes an {@link InputStream} to an {@link OutputStream} using a 1MB buffer */
	public static final void copy(InputStream is, OutputStream os) throws IOException {
		int n;
		byte[] buffer = new byte[1024 * 1024];
		while ((n = is.read(buffer)) != -1) {
			os.write(buffer, 0, n);
		}
	}

}
