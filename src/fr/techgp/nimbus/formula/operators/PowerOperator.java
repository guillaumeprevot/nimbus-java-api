package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.OperatorMember;
import fr.techgp.nimbus.formula.ValueMember;

public class PowerOperator implements OperatorMember.Operator {

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
		FormulaType type = getType(m1, m2);
		return new ValueMember(type, getValue(m1, m2, null));
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return getType(members.get(0), members.get(1));
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return getValue(members.get(0), members.get(1), context);
	}

	public static final Object getValue(FormulaMember m1, FormulaMember m2, Object context) {
		Double value1 = getValue(m1, context);
		if (null == value1)
			return null;
		Double value2 = getValue(m2, context);
		if (null == value2)
			return null;
		FormulaType type = getType(m1, m2);
		double result = Math.pow(value1, value2);
		if (FormulaType.Long.equals(type))
			return Double.valueOf(result).longValue();
		return Math.pow(value1, value2);
	}

	private static final Double getValue(FormulaMember member, Object context) {
		Object value = member.getValue(context);
		if (null == value)
			return null;
		FormulaType type = member.getType();
		if (type != FormulaType.Double)
			return (Double) FormulaType.Double.convertFrom(value, type);
		return (Double) value;
	}

	private static final FormulaType getType(FormulaMember m1, FormulaMember m2) {
		FormulaType t1 = m1.getType();
		FormulaType t2 = m2.getType();
		if (FormulaType.Long.equals(t1) && FormulaType.Long.equals(t2))
			return FormulaType.Long;
		return FormulaType.Double;
	}
}
