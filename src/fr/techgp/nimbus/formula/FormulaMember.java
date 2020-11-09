package fr.techgp.nimbus.formula;

public interface FormulaMember {

	public FormulaType getType();

	public boolean isConstant();

	public Object getValue(Object context);

	public void accept(FormulaVisitor visitor);

}
