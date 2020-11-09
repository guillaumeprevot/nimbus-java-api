package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.ValueMember;
import fr.techgp.nimbus.formula.OperatorMember.Operator;

public class NegativeOperator implements Operator {

	@Override
	public boolean isUnary() {
		return true;
	}

	@Override
	public boolean isUnarySuffix() {
		return false;
	}

	@Override
	public boolean check(List<FormulaMember> members) {
		return (members.size() == 1) && FormulaType.Double.convertableFrom(members.get(0).getType());
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Double;
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember member = members.get(0);
		if (!(member instanceof ValueMember))
			return null;
		if (member.getValue(null) == null)
			return new ValueMember(FormulaType.Double, null);
		return new ValueMember(FormulaType.Double, -(Double) member.getValue(null));
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		Object value = members.get(0).getValue(context);
		if (null == value)
			return null;
		FormulaType type = members.get(0).getType();
		if (type != FormulaType.Double)
			value = FormulaType.Double.convertFrom(value, type);
		return -(Double) value;
	}

}
