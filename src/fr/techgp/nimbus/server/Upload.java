package fr.techgp.nimbus.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

}
