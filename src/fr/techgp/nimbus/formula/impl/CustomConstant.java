package fr.techgp.nimbus.formula.impl;

import fr.techgp.nimbus.formula.ConstantMember;
import fr.techgp.nimbus.formula.FormulaType;

public class CustomConstant implements ConstantMember.Constant {

	private FormulaType type;
	private Object value;

	public CustomConstant(FormulaType type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	@Override
	public FormulaType getType() {
		return this.type;
	}

	@Override
	public Object getValue() {
		return this.value;
	}
}
