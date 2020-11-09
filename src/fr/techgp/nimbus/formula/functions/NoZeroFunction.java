package fr.techgp.nimbus.formula.functions;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember;
import fr.techgp.nimbus.formula.ValueMember;

public class NoZeroFunction implements FunctionMember.Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		return (members.size() == 1) && FormulaType.Double.convertableFrom(members.get(0).getType());
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember member = members.get(0);
		if (!(member instanceof ValueMember))
			return null;
		return new ValueMember(member.getType(), getValue(members, null));
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return members.get(0).getType();
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		Object value = members.get(0).getValue(context);
		if (null == value)
			return null;
		FormulaType type = members.get(0).getType();
		Double dvalue;
		if (FormulaType.Double.equals(type))
			dvalue = (Double) value;
		else
			dvalue = (Double) FormulaType.Double.convertFrom(value, type);
		if (dvalue.equals(0.0))
			return null;
		return value;
	}

}
