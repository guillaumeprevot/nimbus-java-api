package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.OperatorMember;
import fr.techgp.nimbus.formula.ValueMember;

public abstract class AbstractCompareOperator implements OperatorMember.Operator {

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
		return (members.size() == 2) && FormulaType.compatible(members.get(0), members.get(1));
	}

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		FormulaMember m1 = members.get(0);
		if (!(m1 instanceof ValueMember))
			return null;
		FormulaMember m2 = members.get(1);
		if (!(m2 instanceof ValueMember))
			return null;
		return new ValueMember(FormulaType.Boolean, getValue(m1, m2, null));
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Boolean;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		return getValue(members.get(0), members.get(1), context);
	}

	protected abstract boolean getValue(double compareResult);

	private Object getValue(FormulaMember m1, FormulaMember m2, Object context) {
		Object value1 = m1.getValue(context);
		Object value2 = m2.getValue(context);
		if ((null == value1) && (null == value2))
			return getValue(0);
		else if (null == value1)
			return getValue(-1);
		else if (null == value2)
			return getValue(1);
		else {
			FormulaType type = FormulaType.compatibleType(m1, m2);
			if (m1.getType() != type)
				value1 = type.convertFrom(value1, m1.getType());
			if (m2.getType() != type)
				value2 = type.convertFrom(value2, m2.getType());
			return getValue(type.compare(value1, value2));
		}
	}

}
