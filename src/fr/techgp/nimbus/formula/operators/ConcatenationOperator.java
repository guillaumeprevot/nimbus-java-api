package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.OperatorMember;
import fr.techgp.nimbus.formula.ValueMember;

public class ConcatenationOperator implements OperatorMember.Operator {

	@Override
	public boolean isUnary() {
		return false;
	}

	@Override
	public boolean isUnarySuffix() {
		return false;
	}

	@Override
	public boolean check(List<FormulaMember> members) {
		if (members.size() < 2)
			return false;
		for (FormulaMember member : members) {
			if (member.getType() != FormulaType.String)
				return false;
		}
		return true;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		OperatorMember result = new OperatorMember(this);

		StringBuilder sb = null;
		for (FormulaMember member : members) {
			if (member instanceof ValueMember) {
				if (sb == null)
					sb = new StringBuilder();
				sb.append((String) member.getValue(null));
			} else {
				if (sb != null) {
					result.getMembers().add(new ValueMember(FormulaType.String, sb.toString()));
					sb = null;
				}
				result.getMembers().add(member);
			}
		}
		if (sb != null) {
			result.getMembers().add(new ValueMember(FormulaType.String, sb.toString()));
			sb = null;
		}
		return result;
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.String;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		StringBuilder sb = new StringBuilder();
		String memberValue;
		for (FormulaMember member : members) {
			memberValue = (String) member.getValue(context);
			if (null != memberValue)
				sb.append(memberValue);
		}
		return sb.toString();
	}

}
