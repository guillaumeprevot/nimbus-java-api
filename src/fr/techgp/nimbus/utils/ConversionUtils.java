package fr.techgp.nimbus.utils;

import java.security.InvalidParameterException;

public final class ConversionUtils {

	private ConversionUtils() {
		//
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

}
