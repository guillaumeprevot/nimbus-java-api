package fr.techgp.nimbus.utils;

import java.security.InvalidParameterException;

public final class ConversionUtils {

	private ConversionUtils() {
		//
	}

	/**
	 * Cette méthode convertit une chaine de caractères hexadécimaux en un tableau d'octets.
	 *
	 * @param hex une chaine de caractères hexadécimaux
	 * @return l'équivalent sous la forme d'un tableau d'octets
	 */
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

	/**
	 * Cette méthode convertit un tableau d'octets en une chaine de caractères hexadécimaux.
	 *
	 * @param bytes un tableau d'octets
	 * @return l'équivalent sous la forme d'une chaine de caractères hexadécimaux
	 */
	public static final String bytes2hex(final byte[] bytes) {
		final int len = bytes.length;
		final char[] chars = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			chars[j++] = Character.forDigit((0xF0 & bytes[i]) >>> 4, 16);
			chars[j++] = Character.forDigit(0x0F & bytes[i], 16);
		}
		return new String(chars);
	}

	/**
	 * Cette méthode convertit un entier long en un tableau de 8 octets.
	 *
	 * @param value un entier long
	 * @return l'équivalent sous la forme d'un tableau de 8 octets
	 */
	public static final byte[] long2bytes(long value) {
		byte[] result = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			result[Long.BYTES - 1 - i] = (byte) (value >> (8 * i));
		}
		return result;
	}

	/**
	 * Cette méthode convertit un tableau de 8 octets en un entier long.
	 *
	 * @param value un tableau de 8 octets
	 * @return l'entier long équivalent
	 */
	public static final long bytes2long(byte[] value) {
		long result = 0;
		for (int i = 0; i < Long.BYTES; i++) {
			result <<= 8;
			result |= (value[i] & 0xFF);
		}
		return result;
	}

}
