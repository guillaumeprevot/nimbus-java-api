package fr.techgp.nimbus.formula.functions;

import java.util.Date;
import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.FunctionMember.Function;

public class DateFunction implements Function {

	private Date value = new Date();

	@Override
	public boolean check(List<FormulaMember> members) {
		return members.size() == 0;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Date;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		return null;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return this.value;
	}

}
