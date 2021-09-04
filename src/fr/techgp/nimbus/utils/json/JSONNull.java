package fr.techgp.nimbus.utils.json;

public class JSONNull implements JSONElement {

	public static final JSONNull INSTANCE = new JSONNull();

	private JSONNull() {
		super();
	}

	@Override
	public JSONElement deepCopy() {
		return this; // singleton
	}

	@Override
	public void accept(JSONVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public JSONNull asNull() {
		return this;
	}

	@Override
	public int hashCode() {
		// return System.identityHashCode(this);
		return 31; // singleton
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj; // singleton
	}

}
