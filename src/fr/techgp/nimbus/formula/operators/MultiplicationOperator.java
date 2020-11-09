package fr.techgp.nimbus.formula.operators;

import java.util.ArrayList;
import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.OperatorMember;
import fr.techgp.nimbus.formula.ValueMember;

public class MultiplicationOperator extends AbstractNumericOperator {

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaType type = FormulaType.Long;
		List<FormulaMember> newMembers = null;
		Double constValue = 1.0;

		for (FormulaMember member : members) {

			if (!(member instanceof ValueMember)) {
				if (newMembers == null)
					newMembers = new ArrayList<>();
				newMembers.add(member);
			} else if (constValue.doubleValue() != 0.0) {
				if (member.getType() == FormulaType.Double)
					type = FormulaType.Double;
				if (member.getValue(null) == null)
					constValue = 0.0;
				else
					constValue *= ((Number) member.getValue(null)).doubleValue();
			}
		}

		if (constValue.doubleValue() == 0.0)
			return new ValueMember(FormulaType.Long, 0);
		if (newMembers != null) {
			OperatorMember result = new OperatorMember(this);
			result.getMembers().addAll(newMembers);
			if (constValue.doubleValue() != 1.0) {
				if (type == FormulaType.Double)
					result.getMembers().add(new ValueMember(type, constValue));
				else
					result.getMembers().add(new ValueMember(type, constValue.longValue()));
			}
			return result;
		} else if (type == FormulaType.Double)
			return new ValueMember(type, constValue);
		else
			return new ValueMember(type, constValue.longValue());
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
		boolean isLong = true;
		Double result = 1.0;
		for (FormulaMember member : members) {
			Number memberValue = (Number) member.getValue(context);
			isLong = isLong && (member.getType() == FormulaType.Long);
			result *= memberValue == null ? 0.0 : memberValue.doubleValue();
		}
		if (isLong)
			return result.longValue();
		return result;
	}

}
