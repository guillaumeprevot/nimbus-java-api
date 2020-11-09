package fr.techgp.nimbus.formula;

public interface FormulaVisitor {

	public void visit(BlockMember member);

	public void visit(ConstantMember member);

	public void visit(FunctionMember member);

	public void visit(OperatorMember member);

	public void visit(ValueMember member);

	public void visit(VariableMember member);

}
