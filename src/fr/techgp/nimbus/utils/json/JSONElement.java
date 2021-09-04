package fr.techgp.nimbus.utils.json;

public interface JSONElement {

	public JSONElement deepCopy();

	public void accept(JSONVisitor visitor);

	default String toJSON() {
		return new JSONEncoder().encode(this);
	}

	public default boolean isArray() {
		return false;
	}

	public default boolean isObject() {
		return false;
	}

	public default boolean isNull() {
		return false;
	}

	public default boolean isBoolean() {
		return false;
	}

	public default boolean isString() {
		return false;
	}

	public default boolean isNumber() {
		return false;
	}

	public default JSONArray asArray() {
		throw new IllegalStateException(this.getClass().getName() + " is not a JSON array");
	}

	public default JSONObject asObject() {
		throw new IllegalStateException(this.getClass().getName() + " is not a JSON object");
	}

	public default JSONNull asNull() {
		throw new IllegalStateException(this.getClass().getName() + " is not a JSON null");
	}

	public default JSONBoolean asBoolean() {
		throw new IllegalStateException(this.getClass().getName() + " is not a JSON boolean");
	}

	public default JSONString asString() {
		throw new IllegalStateException(this.getClass().getName() + " is not a JSON string");
	}

	public default JSONNumber asNumber() {
		throw new IllegalStateException(this.getClass().getName() + " is not a JSON number");
	}

}
