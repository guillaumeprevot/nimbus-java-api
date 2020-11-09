package fr.techgp.nimbus.formula;

public class ConstantMember implements FormulaMember {

	private Constant constant;

	public ConstantMember(Constant constant) {
		super();
		this.constant = constant;
	}

	public Constant getConstant() {
		return this.constant;
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
	public FormulaType getType() {
		return this.constant.getType();
	}

	@Override
	public Object getValue(Object context) {
		return this.constant.getValue();
	}

	public static interface Constant {

		public FormulaType getType();

		public Object getValue();

	}
}
