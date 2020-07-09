package fr.techgp.nimbus.utils;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.Random;

public final class RandomUtils {

	private RandomUtils() {
		//
	}

	/** generates a new array of random bytes using the specified {@link Random} or {@link SecureRandom} */
	public static final byte[] randomBytes(Random random, int length) {
		byte[] result = new byte[length];
		random.nextBytes(result);
		return result;
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
			for (char c : others) { if (StringUtils.isOtherAsciiPrintableCharacter(c)) builder.append(c); }
		char[] pool = builder.toString().toCharArray();
		// Prepare random string of specified length
		builder.setLength(count);
		for (int i = 0; i < count; i++) {
			int offset = random.nextInt(pool.length);
			builder.setCharAt(i, pool[offset]);
		}
		return builder.toString();
	}

}
