package fr.techgp.nimbus.formula.operators;

public class DifferentOperator extends AbstractCompareOperator {

	@Override
	protected boolean getValue(double compareResult) {
		return compareResult != 0;
	}

}
