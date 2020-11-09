package fr.techgp.nimbus.formula;

import java.util.ArrayList;
import java.util.List;

public class OperatorMember implements FormulaMember {

	private Operator operator;
	protected List<FormulaMember> members;

	public OperatorMember(Operator operator) {
		super();
		this.operator = operator;
		this.members = new ArrayList<>();
	}

	public Operator getOperator() {
		return this.operator;
	}

	public List<FormulaMember> getMembers() {
		return this.members;
	}

	@Override
	public void accept(FormulaVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isConstant() {
		for (FormulaMember member : this.members) {
			if (!member.isConstant())
				return false;
		}
		return true;
	}

	@Override
	public FormulaType getType() {
		return this.operator.getType(this.members);
	}

	@Override
	public Object getValue(Object context) {
		return this.operator.getValue(this.members, context);
	}

	public static interface Operator {

		public boolean isUnary();

		public boolean isUnarySuffix();

		public boolean check(List<FormulaMember> members);

		public FormulaType getType(List<FormulaMember> members);

		public FormulaMember getReducedMember(List<FormulaMember> members);

		public Object getValue(List<FormulaMember> members, Object context);

	}

}
