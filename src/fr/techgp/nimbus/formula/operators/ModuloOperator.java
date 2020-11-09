package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.OperatorMember;
import fr.techgp.nimbus.formula.ValueMember;

public class ModuloOperator implements OperatorMember.Operator {

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
		return (members.size() == 2) && FormulaType.compatible(FormulaType.Double, members.get(0).getType())
				&& FormulaType.compatible(FormulaType.Double, members.get(1).getType());
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember m1 = members.get(0);
		if (!(m1 instanceof ValueMember))
			return null;
		FormulaMember m2 = members.get(1);
		if (!(m2 instanceof ValueMember))
			return null;
		return new ValueMember(FormulaType.Double, getValue(m1, m2, null));
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Double;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return getValue(members.get(0), members.get(1), context);
	}

	public static final Double getValue(FormulaMember m1, FormulaMember m2, Object context) {
		Double value1 = getValue(m1, context);
		if (null == value1)
			return null;
		Double value2 = getValue(m2, context);
		if (null == value2)
			return null;
		return value1 % value2;
	}

	private static final Double getValue(FormulaMember member, Object context) {
		Object value = member.getValue(context);
		if (null == value)
			return null;
		if (member.getType() != FormulaType.Double)
			return (Double) FormulaType.Double.convertFrom(value, member.getType());
		return (Double) value;
	}
}
