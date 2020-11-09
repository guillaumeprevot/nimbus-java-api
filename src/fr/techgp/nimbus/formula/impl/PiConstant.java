package fr.techgp.nimbus.formula.impl;

import fr.techgp.nimbus.formula.ConstantMember;
import fr.techgp.nimbus.formula.FormulaType;

public class PiConstant implements ConstantMember.Constant {

	private static final Double SIMPLIFIED_VALUE = Double.valueOf(3.1415927);
	@Override
	public FormulaType getType() {
		return FormulaType.Double;
	}

	@Override
	public Object getValue() {
		return SIMPLIFIED_VALUE;
	}

}
