package fr.techgp.nimbus.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class IOUtils {

	private IOUtils() {
		//
	}

	/**
	 * Cette méthode copie un {@link InputStream} dans un {@link OutputStream} en utilisant le buffer donné.
	 *
	 * @param is le flux contenant les données à copier
	 * @param os le flux vers lequel copier les données
	 * @param buffer le buffer à utiliser pendant la copie
	 */
	public static final void copy(final InputStream is, final OutputStream os, final byte[] buffer) throws IOException {
		int n;
		while ((n = is.read(buffer)) != -1) {
			os.write(buffer, 0, n);
		}
	}

	/**
	 * Cette méthode copie un {@link InputStream} dans un {@link OutputStream} en utilisant un buffer de 1Mo.
	 *
	 * @param is le flux contenant les données à copier
	 * @param os le flux vers lequel copier les données
	 */
	public static final void copy(final InputStream is, final OutputStream os) throws IOException {
		copy(is, os, new byte[1024 * 1024]);
	}

	/**
	 * Cette méthode extrait le contenu d'un {@link InputStream} pour y lire un tableau d'octets.
	 *
	 * @param is le flux à charger
	 * @return le tableau des octets lus dans le flux
	 * @throws IOException
	 */
	public static final byte[] toByteArray(final InputStream is) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			copy(is, os, new byte[10 * 1024]);
			return os.toByteArray();
		}
	}

	/**
	 * Cette méthode extrait le contenu d'un {@link InputStream} pour y lire une chaine de caractères du {@link Charset} donné.
	 *
	 * @param is le flux contenant le texte à lire
	 * @param charset l'ensemble de caractère à utiliser pour convertire les octets en caractères
	 * @return la chaine de caractères lue dans le flux
	 * @throws IOException
	 */
	public static final String toString(final InputStream is, final Charset charset) throws IOException {
		return new String(toByteArray(is), charset);
	}

	/**
	 * Cette méthode extrait le contenu d'un {@link InputStream} pour y lire une chaine de caractères UTF-8.
	 *
	 * @param is le flux contenant le texte à lire
	 * @return la chaine de caractères UTF-8  lue dans le flux
	 * @throws IOException
	 */
	public static final String toStringUTF8(final InputStream is) throws IOException {
		return new String(toByteArray(is), StandardCharsets.UTF_8);
	}

}
