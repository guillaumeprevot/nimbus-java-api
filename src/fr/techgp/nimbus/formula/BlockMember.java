package fr.techgp.nimbus.formula;

public class BlockMember implements FormulaMember {

	private FormulaMember member;

	public BlockMember() {
		super();
	}

	public BlockMember(FormulaMember member) {
		super();
		this.member = member;
	}

	public FormulaMember getMember() {
		return this.member;
	}

	public void setMember(FormulaMember member) {
		this.member = member;
	}

	@Override
	public void accept(FormulaVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isConstant() {
		return this.member.isConstant();
	}

	@Override
	public FormulaType getType() {
		return this.member.getType();
	}

	@Override
	public Object getValue(Object context) {
		return this.member.getValue(context);
	}

}
