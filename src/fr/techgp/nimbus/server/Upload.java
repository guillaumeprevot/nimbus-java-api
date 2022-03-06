package fr.techgp.nimbus.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.techgp.nimbus.utils.IOUtils;

public interface Upload {

	/** returns the input's name in the HTML form */
	public String name();

	/** return the file's name, as it was on the client side */
	public String fileName();

	/** returns the content type of this part */
	public String contentType();

	/** returns the content size of this part, in bytes */
	public long contentLength();

	/** gets the content of this part as an InputStream */
	public InputStream getInputStream() throws IOException;

	/** deletes the underlying storage for a file item, including deleting any associated temporary disk file */
	public void delete() throws IOException;

	default String asString() throws IOException {
		try (InputStream is = this.getInputStream()) {
			return IOUtils.toUTF8String(is);
		}
	}

	default void saveTo(File storedFile) throws IOException {
		// La méthode par défaut est d'ouvrir le flux pour le copier.
		// L'implémentation finale, par exemple avec Jetty, peut surcharger pour optimiser
		try (OutputStream os = new FileOutputStream(storedFile)) {
			saveTo(os);
		}
	}

	default void saveTo(OutputStream storedStream) throws IOException {
		// La méthode par défaut est d'ouvrir le flux pour le copier.
		// L'implémentation finale, par exemple avec Jetty, peut surcharger pour optimiser
		try (InputStream is = this.getInputStream()) {
			IOUtils.copy(is, storedStream, new byte[1024 * 1024 * 10]);
		}
	}

}
