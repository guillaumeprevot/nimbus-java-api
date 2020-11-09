package fr.techgp.nimbus.formula.operators;

import java.util.List;

import fr.techgp.nimbus.formula.FormulaMember;
import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.ValueMember;

public class DivisionOperator extends AbstractNumericOperator {

	@Override
	public FormulaMember getReducedMember(List<FormulaMember> members) {
		// On utilise le principe suivant : a / b / c = a / (b * c)
		FormulaMember first = members.get(0);
		if (!(first instanceof ValueMember))
			return null;

		Number firstValue = (Number) first.getValue(null);
		if (firstValue == null)
			return new ValueMember(FormulaType.Double, null);
		if (firstValue.doubleValue() == 0.0)
			return new ValueMember(FormulaType.Double, 0);

		FormulaMember next = new MultiplicationOperator().getReducedMember(members.subList(1, members.size()));
		if (!(next instanceof ValueMember))
			return null;

		Number nextValue = (Number) next.getValue(null);
		if ((nextValue == null) || (nextValue.doubleValue() == 0.0))
			return new ValueMember(FormulaType.Double, Double.NaN);

		return new ValueMember(FormulaType.Double, firstValue.doubleValue() / nextValue.doubleValue());
	}

	@Override
	public FormulaType getType(List<FormulaMember> members) {
		return FormulaType.Double;
	}

	@Override
	public Object getValue(List<FormulaMember> members, Object context) {
		Number memberValue = (Number) members.get(0).getValue(context);
		if (memberValue == null)
			return null;
		if (memberValue.doubleValue() == 0.0)
			return 0.0;
		boolean isValid = true;
		Double result = memberValue.doubleValue();
		for (int i = 1; i < members.size(); i++) {
			memberValue = (Number) members.get(i).getValue(context);
			if ((memberValue == null) || (memberValue.doubleValue() == 0.0)) {
				isValid = false;
				break;
			}
			result /= memberValue.doubleValue();
		}
		if (!isValid)
			return Double.NaN;
		return result;
	}

}
