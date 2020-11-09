package fr.techgp.nimbus.formula.functions;

import java.util.List;
import java.util.UUID;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember.Function;

public class UUIDFunction implements Function {

	@Override
	public boolean check(List<FormulaMember> members) {
		return members.isEmpty();
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.String;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		return null;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		UUID id = UUID.randomUUID();
		return id.toString().toUpperCase();
	}

}
