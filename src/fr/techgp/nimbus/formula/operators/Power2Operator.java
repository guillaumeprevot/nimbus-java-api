package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.ValueMember;
import fr.techgp.nimbus.formula.OperatorMember.Operator;

public class Power2Operator implements Operator {

	@Override
	public boolean isUnary() {
		return true;
	}

	@Override
	public boolean isUnarySuffix() {
		return true;
	}

	@Override
	public boolean check(List<FormulaMember> members) {
		return (members.size() == 1) && FormulaType.Double.convertableFrom(members.get(0).getType());
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Double;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember member = members.get(0);
		if (!(member instanceof ValueMember))
			return null;
		return new ValueMember(FormulaType.Double, getValue(member, null));
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return getValue(members.get(0), context);
	}

	private static Double getValue(FormulaMember member, Object context) {
		Object value = member.getValue(context);
		if (null == value)
			return null;
		FormulaType type = member.getType();
		Double dvalue;
		if (FormulaType.Double.equals(type))
			dvalue = (Double) value;
		else
			dvalue = (Double) FormulaType.Double.convertFrom(value, type);
		return dvalue * dvalue;
	}

}
