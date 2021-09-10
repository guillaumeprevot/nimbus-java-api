package fr.techgp.nimbus.utils.json;

import java.text.ParseException;

import fr.techgp.nimbus.utils.json.JSONStream.JSONStreamStartContext;
import fr.techgp.nimbus.utils.json.JSONStream.JSONStreamRenderer;

public final class JSON {

	private JSON() {
		//
	}

	public static final JSONElement decode(String json) throws ParseException {
		return new JSONDecoder().decode(json);
	}

	public static final JSONStreamStartContext stream(JSONStreamRenderer renderer) {
		return new JSONStreamStartContext(renderer);
	}

	public static final String encode(JSONElement element) {
		return new JSONEncoder().encode(element);
	}

	public static final String format(JSONElement element) {
		return new JSONEncoder().beautify().encode(element);
	}

	public static final String format(JSONElement element, String indentOrNull, boolean spaceAfterColumn, boolean encodeNullProperties) {
		return new JSONEncoder().beautify(indentOrNull, spaceAfterColumn, encodeNullProperties).encode(element);
	}

	public static final JSONArray array() {
		return new JSONArray();
	}

	public static final JSONObject object() {
		return new JSONObject();
	}

	public static final JSONElement ofNull() {
		return JSONNull.INSTANCE;
	}

	public static final JSONElement checked(JSONElement element) {
		return element == null ? JSONNull.INSTANCE : element;
	}

	public static final JSONElement of(Boolean value) {
		return value == null ? JSONNull.INSTANCE : value.booleanValue() ? JSONBoolean.TRUE_INSTANCE : JSONBoolean.FALSE_INSTANCE;
	}

	public static final JSONElement of(boolean value) {
		return value ? JSONBoolean.TRUE_INSTANCE : JSONBoolean.FALSE_INSTANCE;
	}

	public static final JSONElement of(String value) {
		return value == null ? JSONNull.INSTANCE : new JSONString(value);
	}

	public static final JSONElement of(Number value) {
		return value == null ? JSONNull.INSTANCE : new JSONNumber(value);
	}

	public static final JSONElement of(long value) {
		return new JSONNumber(value);
	}

	public static final JSONElement of(double value) {
		return new JSONNumber(value);
	}

	public static final String escape(String input) {
		StringBuilder sb = new StringBuilder();
		escapeTo(input, sb);
		return sb.toString();
	}

	public static final void escapeTo(String input, StringBuilder sb) {
		int length = input.length();
		for (int i = 0; i < length; i++) {
			char c = input.charAt(i);
			String replacement = null;
			if (c < 128) {
				replacement = CHAR_REPLACEMENTS[c];
			} else if (c == '\u2028') {
				replacement = "\\u2028";
			} else if (c == '\u2029') {
				replacement = "\\u2029";
			}
			if (replacement == null)
				sb.append(c);
			else
				sb.append(replacement);
		}
	}

	/** @see https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/stream/JsonWriter.java */
	private static final String[] CHAR_REPLACEMENTS = new String[128];
	static {
		for (int i = 0; i <= 0x1f; i++) {
			CHAR_REPLACEMENTS[i] = String.format("\\u%04x", i);
		}
		CHAR_REPLACEMENTS['"'] = "\\\"";
		CHAR_REPLACEMENTS['\\'] = "\\\\";
		CHAR_REPLACEMENTS['\r'] = "\\r";
		CHAR_REPLACEMENTS['\n'] = "\\n";
		CHAR_REPLACEMENTS['\t'] = "\\t";
		CHAR_REPLACEMENTS['\b'] = "\\b";
		CHAR_REPLACEMENTS['\f'] = "\\f";
	}
}
