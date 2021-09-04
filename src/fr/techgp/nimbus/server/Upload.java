package fr.techgp.nimbus.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.StandardCopyOption;

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

	/** or gets the content as a file, if the content has been dumped to a file during request extraction */
	public File getFile();

	/** or gets the content as a byte array, if the content was small enough to be loaded in memory */
	public byte[] getBytes();

	/** deletes the underlying storage for a file item, including deleting any associated temporary disk file */
	public void delete() throws IOException;

	default String asString() throws IOException {
		try (InputStream is = this.getInputStream()) {
			return IOUtils.toStringUTF8(is);
		}
	}

	default void saveTo(File storedFile) throws IOException {
		if (this.getFile() != null) {
			// La limite a été dépassée et le fichier a donc été écrit sur disque.
			// => on déplace le fichier (= rapide puisque c'est le même volume)
			java.nio.file.Files.move(this.getFile().toPath(), storedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		} else if (this.getBytes() != null) {
			// La taille est en dessous de la limite et le contenu est donc en mémoire
			// => on écrit dans le fichier demandé
			try (OutputStream os = new FileOutputStream(storedFile)) {
				os.write(this.getBytes());
			}

		} else {
			// La méthode par défaut est d'ouvrir le flux pour le copier
			// => c'est juste une fallback si on n'a détecté ni fichier, ni byte[]
			try (InputStream is = this.getInputStream()) {
				//too slow : FileUtils.copyInputStreamToFile(is, storedFile);
				try (OutputStream os = new FileOutputStream(storedFile)) {
					IOUtils.copy(is, os, new byte[1024*1024*10]);
				}
			}
		}
	}

	default void saveTo(OutputStream storedStream) throws IOException {
		if (this.getBytes() != null) {
			// La taille est en dessous de la limite et le contenu est donc en mémoire
			// => on écrit dans le flux demandé
			storedStream.write(this.getBytes());

		} else {
			// Soit le fichier a été écrit sur disque, soit getBytes() n'était pas dispo
			// => on ouvre le flux uploadé pour le copier
			try (InputStream is = this.getInputStream()) {
				IOUtils.copy(is, storedStream, new byte[1024*1024*10]);
			}
		}
	}

}
