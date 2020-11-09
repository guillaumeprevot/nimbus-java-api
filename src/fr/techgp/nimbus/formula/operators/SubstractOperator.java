package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.ValueMember;

public class SubstractOperator extends AbstractNumericOperator {

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		// On utilise le principe suivant : a - b - c = a - (b + c)
		FormulaMember first = members.get(0);
		FormulaMember next = new AdditionOperator().getReducedMember(members.subList(1, members.size()));
		if ((first instanceof ValueMember) && (next instanceof ValueMember)) {
			FormulaType type = FormulaType.compatibleType(first, next);
			Double value = ((Number) first.getValue(null)).doubleValue() - ((Number) next.getValue(null)).doubleValue();
			if (type == FormulaType.Double)
				return new ValueMember(type, value);
			return new ValueMember(type, value.longValue());
		}
		return null;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		for (FormulaMember member : members) {
			if (member.getType() == FormulaType.Double)
				return FormulaType.Double;
		}
		return FormulaType.Long;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		Number memberValue = (Number) members.get(0).getValue(context);
		boolean isLong = members.get(0).getType() == FormulaType.Long;
		Double result = memberValue == null ? 0.0 : memberValue.doubleValue();
		for (int i = 1; i < members.size(); i++) {
			isLong = isLong && (members.get(i).getType() == FormulaType.Long);
			memberValue = (Number) members.get(i).getValue(context);
			result -= memberValue == null ? 0.0 : memberValue.doubleValue();
		}
		if (isLong)
			return result.longValue();
		return result;
	}

}
