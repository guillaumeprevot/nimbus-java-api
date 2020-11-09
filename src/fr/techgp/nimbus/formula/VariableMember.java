package fr.techgp.nimbus.formula;

public class VariableMember implements FormulaMember {

	private Variable variable;

	public VariableMember(Variable variable) {
		super();
		this.variable = variable;
	}

	public Variable getVariable() {
		return this.variable;
	}

	@Override
	public void accept(FormulaVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public FormulaType getType() {
		return this.variable.getType();
	}

	@Override
	public Object getValue(Object context) {
		return this.variable.getValue(context);
	}

	public static interface Variable {

		public FormulaType getType();

		public Object getValue(Object context);

	}
}
