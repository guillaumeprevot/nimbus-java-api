package fr.techgp.nimbus.utils.json;

import java.util.Objects;

public class JSONString implements JSONElement {

	private final String value;

	public JSONString(String value) {
		this.value = Objects.requireNonNull(value);
	}

	public String getValue() {
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
	public boolean isString() {
		return true;
	}

	@Override
	public JSONString asString() {
		return this;
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj) || (obj instanceof JSONString && this.value.equals(((JSONString) obj).value));
	}

}
