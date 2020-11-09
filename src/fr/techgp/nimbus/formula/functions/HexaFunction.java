package fr.techgp.nimbus.formula.functions;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember;
import fr.techgp.nimbus.formula.ValueMember;

public class HexaFunction implements FunctionMember.Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		return (members.size() == 1) && (members.get(0).getType() == FormulaType.Long);
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember m = members.get(0);
		if (m instanceof ValueMember)
			return new ValueMember(FormulaType.String, getValue(m, null));
		return null;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.String;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return getValue(members.get(0), context);
	}

	private static String getValue(FormulaMember member, Object context) {
		Object value = member.getValue(context);
		if (null == value)
			return null;
		return Long.toHexString((Long) value);
	}
}
