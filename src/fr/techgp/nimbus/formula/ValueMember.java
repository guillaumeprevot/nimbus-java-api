package fr.techgp.nimbus.formula;

public class ValueMember implements FormulaMember {

	private FormulaType type;
	private Object value;

	public ValueMember() {
		super();
	}

	public ValueMember(FormulaType type) {
		super();
		this.type = type;
	}

	public ValueMember(FormulaType type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	@Override
	public FormulaType getType() {
		return this.type;
	}

	public void setType(FormulaType type) {
		this.type = type;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public void accept(FormulaVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public Object getValue(Object context) {
		return this.value;
	}

}
