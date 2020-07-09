package fr.techgp.nimbus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtils {

	private IOUtils() {
		//
	}

	/**
	 * Cette méthode copie un {@link InputStream} dans un {@link OutputStream} en utilisant un buffer de 1Mo.
	 *
	 * @param is le flux contenant les données à copier
	 * @param os le flux vers lequel copier les données
	 */
	public static final void copy(final InputStream is, final OutputStream os) throws IOException {
		int n;
		byte[] buffer = new byte[1024 * 1024];
		while ((n = is.read(buffer)) != -1) {
			os.write(buffer, 0, n);
		}
	}

}
