package fr.techgp.nimbus.formula.operators;

public class MoreOrEqualOperator extends AbstractCompareOperator {

	@Override
	protected boolean getValue(double compareResult) {
		return compareResult >= 0;
	}

}
