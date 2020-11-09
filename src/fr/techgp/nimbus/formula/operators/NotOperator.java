package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.ValueMember;
import fr.techgp.nimbus.formula.OperatorMember.Operator;

public class NotOperator implements Operator {

	@Override
	public boolean isUnary() {
		return true;
	}

	@Override
	public boolean isUnarySuffix() {
		return false;
	}

	@Override
	public boolean check(List<FormulaMember> members) {
		if (members.size() != 1)
			return false;
		return FormulaType.Boolean.equals(members.get(0).getType());
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Boolean;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember member = members.get(0);
		if (! (member instanceof ValueMember))
			return null;
		boolean value = Boolean.TRUE.equals(member.getValue(null));
		return new ValueMember(FormulaType.Boolean, !value);
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		FormulaMember member = members.get(0);
		boolean value = Boolean.TRUE.equals(member.getValue(null));
		return !value;
	}

}
