package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.OperatorMember.Operator;

public abstract class AbstractNumericOperator implements Operator {

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
			if (!FormulaType.compatible(member.getType(), FormulaType.Double))
				return false;
		}
		return true;
	}

	@Override
	public abstract FormulaMember getReducedMember(List<FormulaMember> members);

	@Override
	public abstract FormulaType getType(List<FormulaMember> members);

	@Override
	public abstract Object getValue(List<FormulaMember> members, Object context);

}
