package fr.techgp.nimbus.utils.json;

public class JSONBoolean implements JSONElement {

	public static final JSONBoolean TRUE_INSTANCE = new JSONBoolean(true);
	public static final JSONBoolean FALSE_INSTANCE = new JSONBoolean(false);
	private final boolean value;

	private JSONBoolean(boolean value) {
		super();
		this.value = value;
	}

	public boolean getValue() {
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
	public boolean isBoolean() {
		return true;
	}

	@Override
	public JSONBoolean asBoolean() {
		return this;
	}

	@Override
	public int hashCode() {
		return Boolean.hashCode(this.value);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj; // soit l'instance true, soit l'instance false
	}

}
