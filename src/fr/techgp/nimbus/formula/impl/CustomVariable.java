package fr.techgp.nimbus.formula.impl;

import fr.techgp.nimbus.formula.FormulaType;
import fr.techgp.nimbus.formula.VariableMember.Variable;

public class CustomVariable implements Variable {

	private FormulaType type;
	private Object value;

	public CustomVariable(FormulaType type) {
		super();
		this.type = type;
	}

	@Override
	public FormulaType getType() {
		return this.type;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public Object getValue(Object context) {
		return this.value;
	}

}
