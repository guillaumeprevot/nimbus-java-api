package fr.techgp.nimbus.formula.functions;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.ValueMember;
import fr.techgp.nimbus.formula.FunctionMember.Function;

public class TypeFunction implements Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		return members.size() == 1;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.String;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		return new ValueMember(FormulaType.String, getValue(members, null));
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return members.get(0).getType().name();
	}

}
