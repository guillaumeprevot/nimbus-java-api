package fr.techgp.nimbus.formula;

import java.util.ArrayList;
import java.util.List;

public class FunctionMember implements FormulaMember {

	private Function function;
	protected List<FormulaMember> members;

	public FunctionMember(Function function) {
		super();
		this.function = function;
		this.members = new ArrayList<>();
	}

	public Function getFunction() {
		return this.function;
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
		return this.function.getType(this.members);
	}

	@Override
	public Object getValue(Object context) {
		return this.function.getValue(this.members, context);
	}

	public static interface Function {

		public boolean check(List<FormulaMember> members);

		public FormulaType getType(List<FormulaMember> members);

		public FormulaMember getReducedMember(List<FormulaMember> members);

		public Object getValue(List<FormulaMember> members, Object context);

	}
}
