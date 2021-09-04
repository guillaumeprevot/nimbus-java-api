package fr.techgp.nimbus.utils.json;

import java.util.Objects;

public class JSONNumber implements JSONElement {

	private final Number value;

	public JSONNumber(Number value) {
		Objects.requireNonNull(value);
		if (value instanceof Double && ((Double) value).isNaN())
			throw new IllegalArgumentException("NaN is not supported for JSON");
		if (value instanceof Float && ((Float) value).isNaN())
			throw new IllegalArgumentException("NaN is not supported for JSON");
		if (value instanceof Double && ((Double) value).isInfinite())
			throw new IllegalArgumentException("Infinity is not supported for JSON");
		if (value instanceof Float && ((Float) value).isInfinite())
			throw new IllegalArgumentException("Infinity is not supported for JSON");
		this.value = value;
	}

	public Number getValue() {
		return this.value;
	}

	@Override
	public JSONElement deepCopy() {
		return this; // object immuable donc on peut retourner l'objet lui-même
	}

	@Override
	public void accept(JSONVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public JSONNumber asNumber() {
		return this;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(this.value.doubleValue());
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj) || (obj instanceof JSONNumber && this.value.equals(((JSONNumber) obj).value));
	}

}
