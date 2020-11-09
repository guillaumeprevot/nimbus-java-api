package fr.techgp.nimbus.formula.functions;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember;
import fr.techgp.nimbus.formula.ValueMember;

public class IsNullFunction implements FunctionMember.Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		return members.size() == 1;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember member = members.get(0);
		if (member instanceof ValueMember)
			return new ValueMember(FormulaType.Boolean, ((ValueMember) member).getValue() == null);
		return null;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Boolean;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return (members.get(0).getValue(context) == null);
	}

}
