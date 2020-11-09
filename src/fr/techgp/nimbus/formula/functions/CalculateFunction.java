package fr.techgp.nimbus.formula.functions;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember.Function;

public class CalculateFunction implements Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		return !members.isEmpty();
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Boolean;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		return null;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		try {
			for (FormulaMember member : members) {
				member.getValue(context);
			}
			return Boolean.TRUE;
		} catch (Exception ex) {
			return Boolean.FALSE;
		}
	}

}
