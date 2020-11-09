package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.ValueMember;
import fr.techgp.nimbus.formula.OperatorMember.Operator;

public class OrOperator implements Operator {

	@Override
	public boolean isUnary() {
		return false;
	}

	@Override
	public boolean isUnarySuffix() {
		return false;
	}

	@Override
	public boolean check(List<FormulaMember> members) {
		if (members.size() < 2)
			return false;
		for (FormulaMember member : members) {
			if (!FormulaType.Boolean.equals(member.getType()))
				return false;
		}
		return true;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Boolean;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		boolean result = true;
		for (FormulaMember member : members) {
			if (! (member instanceof ValueMember))
				return null;
			boolean value = Boolean.TRUE.equals(member.getValue(null));
			result |= value;
		}
		return new ValueMember(FormulaType.Boolean, result);
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		boolean result = true;
		for (FormulaMember member : members) {
			boolean value = Boolean.TRUE.equals(member.getValue(context));
			result |= value;
		}
		return result;
	}

}
