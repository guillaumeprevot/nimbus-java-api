package fr.techgp.nimbus.utils.json;

import java.text.ParseException;

public final class JSON {

	private JSON() {
		//
	}

	public static final JSONElement decode(String json) throws ParseException {
		return new JSONDecoder().decode(json);
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

	public static final JSONElement of(Boolean value) {
		return value == null ? JSONNull.INSTANCE : value.booleanValue() ? JSONBoolean.TRUE_INSTANCE : JSONBoolean.FALSE_INSTANCE;
	}

	public static final JSONElement of(String value) {
		return value == null ? JSONNull.INSTANCE : new JSONString(value);
	}

	public static final JSONElement of(Number value) {
		return value == null ? JSONNull.INSTANCE : new JSONNumber(value);
	}

}
