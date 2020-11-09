package fr.techgp.nimbus.formula.functions;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember;
import fr.techgp.nimbus.formula.ValueMember;

public class NoNullFunction implements FunctionMember.Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		if (members.size() == 1)
			return false;
		FormulaMember m = members.get(members.size() - 1);
		return (m.isConstant()) && (m.getValue(null) != null);
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		int i = 0;
		while (i < members.size()) {
			FormulaMember m = members.get(i);
			if (!(m instanceof ValueMember))
				return null;
			if (m.getValue(null) != null)
				return m;
			i++;
		}
		return null;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return members.get(members.size() - 1).getType();
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		FormulaType resultType = members.get(members.size() - 1).getType();
		FormulaType valueType = FormulaType.Null;
		Object value = null;
		for (FormulaMember member : members) {
			value = member.getValue(context);
			if (value != null) {
				valueType = member.getType();
				break;
			}
		}
		if (valueType != resultType)
			value = resultType.convertFrom(value, valueType);
		return value;
	}

}
