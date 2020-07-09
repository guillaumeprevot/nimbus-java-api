package fr.techgp.nimbus.utils;

public final class StringUtils {

	private StringUtils() {
		//
	}

	public static final String OTHER_ASCII_PRINTABLE_CHARACTERS = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

	/** returns true if "c" is an ASCII printable character but neither a letter nor a digit */
	public static final boolean isOtherAsciiPrintableCharacter(char c) {
		return OTHER_ASCII_PRINTABLE_CHARACTERS.indexOf(c) >= 0;
	}

	/** returns true if "s" is null, is empty or contains only whitespace characters */
	public static final boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}

	/** returns true if "s" is not null and contains at least one non-whitespace character */
	public static final boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	/** returns the first String of "values" that matches {@link StringUtils#isNotBlank} or null if none matches */
	public static final String coalesce(String... values) {
		for (String value : values) {
			if (isNotBlank(value))
				return value;
		}
		return null;
	}

	/** returns "value" if it is not blank ({@link StringUtils#isNotBlank(String)}) or defaultValue otherwise */
	public static final String withDefault(String value, String defaultValue) {
		return isBlank(value) ? defaultValue : value;
	}

	/** returns a new string containing "count" times the "value" text */
	public static final String repeat(String value, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(value);
		}
		return sb.toString();
	}

}
