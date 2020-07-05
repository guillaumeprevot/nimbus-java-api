package fr.techgp.nimbus.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Random;

public final class Utils {

	private Utils() {
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

	/** generates a random ASCII string using specified characters */
	public static final String randomAscii(Random random, int count, boolean lowercase, boolean uppercase, boolean digits, char[] others) {
		if (count < 0)
			throw new InvalidParameterException("String length " + count + " is invalid.");
		if (count == 0)
			return "";
		// Prepare pool using specified characters
		StringBuilder builder = new StringBuilder();
		if (lowercase)
			for (char c = 'a'; c <= 'z'; c++) { builder.append(c); }
		if (uppercase)
			for (char c = 'A'; c <= 'Z'; c++) { builder.append(c); }
		if (digits)
			for (char c = '0'; c <= '9'; c++) { builder.append(c); }
		if (others != null)
			builder.append(others);
		char[] pool = builder.toString().toCharArray();
		// Prepare random string of specified length
		builder.setLength(count);
		for (int i = 0; i < count; i++) {
			int offset = random.nextInt(pool.length);
			builder.setCharAt(i, pool[offset]);
		}
		return builder.toString();
	}

	/** converts an hexadecimal String to an array of byte */
	public static final byte[] hex2bytes(final String hex) {
		final int len = hex.length();
		if ((len & 0x01) != 0)
			throw new InvalidParameterException("length should be a multiple of 2");
		final byte[] result = new byte[len >> 1];
		for (int i = 0; i < len; i += 2) {
			result[i >> 1] = (byte) (Integer.parseInt(hex.substring(i, i + 2), 16) & 0xFF);
		}
		return result;
	}

	/** converts an array of byte to an hexadecimal String */
	public static final String bytes2hex(final byte[] bytes) {
		final int len = bytes.length;
		final char[] chars = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			chars[j++] = Character.forDigit((0xF0 & bytes[i]) >>> 4, 16);
			chars[j++] = Character.forDigit(0x0F & bytes[i], 16);
		}
		return new String(chars);
	}

	/** converts a long value to an array of 8 bytes */
	public static final byte[] long2bytes(long value) {
		byte[] result = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			result[Long.BYTES - 1 - i] = (byte) (value >> (8 * i));
		}
		return result;
	}

	/** converts an array of 8 bytes to a long value */
	public static final long bytes2long(byte[] value) {
		long result = 0;
		for (int i = 0; i < Long.BYTES; i++) {
			result <<= 8;
			result |= (value[i] & 0xFF);
		}
		return result;
	}

	/** gets the HTTP method for this {@link Request}, checking for potential proxy headers */
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

	/** gets the client address for this {@link Request}, checking for potential proxy headers */
	public static final String extractIPWithProxy(Request request) {
		String r = request.header("X-Real-IP");
		if (r != null)
			return r;
		r = request.header("X-Forwarded-For");
		if (r != null)
			return r.split(",")[0].trim();
		return request.ip();
	}

	/** gets the host name used by client for this {@link Request}, checking for potential proxy headers */
	public static final String extractHostWithProxy(Request request) {
		String r = request.header("X-Forwarded-Host");
		if (r != null)
			return r.split(",")[0].trim();
		return request.header("Host");
	}

}
