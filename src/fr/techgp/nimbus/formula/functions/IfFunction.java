package fr.techgp.nimbus.formula.functions;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember;
import fr.techgp.nimbus.formula.ValueMember;

public class IfFunction implements FunctionMember.Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		return (members.size() == 3) && (members.get(0).getType() == FormulaType.Boolean)
				&& FormulaType.compatible(members.get(1), members.get(2));
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember member = members.get(0);
		if (!(member instanceof ValueMember))
			return null;

		if (Boolean.TRUE.equals(member.getValue(null)))
			return members.get(1);
		return members.get(2);
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.compatibleType(members.get(1), members.get(2));
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		FormulaType compatibleType = getType(members);
		FormulaType type;
		Object value;
		if (Boolean.TRUE.equals(members.get(0).getValue(context))) {
			value = members.get(1).getValue(context);
			type = members.get(1).getType();
		} else {
			value = members.get(2).getValue(context);
			type = members.get(2).getType();
		}
		if (type != compatibleType)
			value = compatibleType.convertFrom(value, type);
		return value;
	}

}
