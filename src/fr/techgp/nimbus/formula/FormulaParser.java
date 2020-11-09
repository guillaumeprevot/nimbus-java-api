package fr.techgp.nimbus.formula;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.techgp.nimbus.formula.ConstantMember.Constant;
import fr.techgp.nimbus.formula.FunctionMember.Function;
import fr.techgp.nimbus.formula.OperatorMember.Operator;
import fr.techgp.nimbus.formula.VariableMember.Variable;
import fr.techgp.nimbus.utils.StringUtils;

public final class FormulaParser {

	private FormulaSyntax syntax = new FormulaSyntax();
	private Map<String, Constant> constants = new HashMap<>();
	private Map<String, Function> functions = new HashMap<>();
	private Map<String, Variable> variables = new HashMap<>();
	private List<Pair<String, Operator>> operators = new ArrayList<>();

	public FormulaParser() {
		super();
	}

	public FormulaSyntax getSyntax() {
		return this.syntax;
	}

	public void setSyntax(FormulaSyntax syntax) {
		this.syntax = syntax;
	}

	public Map<String, Constant> getConstants() {
		return this.constants;
	}

	public void setConstants(Map<String, Constant> constants) {
		this.constants = constants;
	}

	public Map<String, Function> getFunctions() {
		return this.functions;
	}

	public void setFunctions(Map<String, Function> functions) {
		this.functions = functions;
	}

	public Map<String, Variable> getVariables() {
		return this.variables;
	}

	public void setVariables(Map<String, Variable> variables) {
		this.variables = variables;
	}

	public List<Pair<String, Operator>> getOperators() {
		return this.operators;
	}

	public void setOperators(List<Pair<String, Operator>> operators) {
		this.operators = operators;
	}

	public FormulaMember parse(String formula) throws ParseException {
		String parsedFormula = formula.trim();
		String lowerFormula = parsedFormula.toLowerCase();

		if (StringUtils.checkDelimitedBy(parsedFormula,
				this.syntax.getBlockDelimiterStart(), this.syntax.getBlockDelimiterEnd())) {
			BlockMember member = new BlockMember();
			member.setMember(parse(parsedFormula.substring(1, parsedFormula.length() - 1)));
			return member;
		}

		Constant constant = this.constants.get(lowerFormula);
		if (constant != null)
			return new ConstantMember(constant);

		Variable variable = this.variables.get(lowerFormula);
		if (variable != null)
			return new VariableMember(variable);

		List<FormulaMember> members = new ArrayList<>();

		for (Pair<String, Operator> operator : this.operators) {
			if (!operator.getValue2().isUnary()) {
				// Opérateur non unaire comme 1+2+3
				parse(parsedFormula, operator.getValue1(), members);
			} else if (operator.getValue2().isUnarySuffix()) {
				// Opérateur unaire suffixe comme x²
				if (parsedFormula.endsWith(operator.getValue1())) {
					String s = parsedFormula.substring(0, parsedFormula.length() - operator.getValue1().length());
					members.add(parse(s));
				}
			} else {
				// Opérateur unaire préfixe comme -2
				if (parsedFormula.startsWith(operator.getValue1())) {
					String s = parsedFormula.substring(operator.getValue1().length());
					members.add(parse(s));
				}
			}
			if (!members.isEmpty() && operator.getValue2().check(members)) {
				OperatorMember member = new OperatorMember(operator.getValue2());
				member.members.addAll(members);
				return member;
			}
			members.clear();
		}

		int indexOfParenthesis = parsedFormula.indexOf(this.syntax.getFunctionParameterStart());
		Function function = indexOfParenthesis == -1 ? null : this.functions.get(lowerFormula.substring(0,
				indexOfParenthesis).trim());
		if ((function != null)
				&& (parsedFormula.charAt(parsedFormula.length() - 1) == this.syntax.getFunctionParameterEnd())) {
			String parameters = parsedFormula.substring(indexOfParenthesis + 1, parsedFormula.length() - 1).trim();
			if (parameters.length() != 0) {
				parse(parameters, String.valueOf(this.syntax.getFunctionParameterSeparator()), members);
				if (members.isEmpty())
					members.add(parse(parameters));
			}
			if (function.check(members)) {
				FunctionMember member = new FunctionMember(function);
				member.getMembers().addAll(members);
				return member;
			}
			members.clear();
		}

		ValueMember member = this.syntax.parse(parsedFormula);
		if (member != null)
			return member;

		throw new ParseException(parsedFormula, 0);
	}

	public void parse(String formula, String separator, List<FormulaMember> members) throws ParseException {
		int curCharIndex = 0;
		char curChar;
		boolean inExpression = false;
		int curDepth = 0;
		int curMemberStart = 0;

		while (curCharIndex < formula.length()) {
			curChar = formula.charAt(curCharIndex);

			if (curChar == this.syntax.getStringDelimiterStart() && !inExpression) {
				inExpression = true;
				curCharIndex++;
			} else if (curChar == this.syntax.getStringDelimiterEnd() && inExpression) {
				inExpression = false;
				curCharIndex++;
			} else if (curChar == this.syntax.getBlockDelimiterStart()
					|| curChar == this.syntax.getFunctionParameterStart()) {
				if (!inExpression)
					curDepth++;
				curCharIndex++;
			} else if (curChar == this.syntax.getBlockDelimiterEnd()
					|| curChar == this.syntax.getFunctionParameterEnd()) {
				if (!inExpression)
					curDepth--;
				curCharIndex++;
			} else if (inExpression) {
				curCharIndex++;
			} else if (curDepth != 0) {
				curCharIndex++;
			} else if (formula.substring(curCharIndex).startsWith(separator)) {
				members.add(parse(formula.substring(curMemberStart, curCharIndex)));
				curCharIndex = curCharIndex + separator.length();
				curMemberStart = curCharIndex;
			} else {
				curCharIndex++;
			}

		}

		if (members.size() > 0)
			members.add(parse(formula.substring(curMemberStart, curCharIndex)));
	}

	public String format(FormulaMember member) {
		StringBuilder sb = new StringBuilder();
		member.accept(new FormatVisitor(sb));
		return sb.toString();
	}

	public String format(List<FormulaMember> members, String separator) {
		StringBuilder sb = new StringBuilder();
		FormatVisitor visitor = new FormatVisitor(sb);
		boolean first = true;
		for (FormulaMember member : members) {
			if (first)
				first = false;
			else
				sb.append(separator);
			member.accept(visitor);
		}
		return sb.toString();
	}

	public FormulaMember reduce(FormulaMember member) {
		ReduceVisitor visitor = new ReduceVisitor();
		member.accept(visitor);
		return visitor.getMember();
	}

	private class FormatVisitor implements FormulaVisitor {

		private StringBuilder sb;

		public FormatVisitor(StringBuilder sb) {
			super();
			this.sb = sb;
		}

		@Override
		public void visit(BlockMember member) {
			this.sb.append(getSyntax().getBlockDelimiterStart());
			member.getMember().accept(this);
			this.sb.append(getSyntax().getBlockDelimiterEnd());
		}

		@Override
		public void visit(ConstantMember member) {
			for (Map.Entry<String, Constant> entry : getConstants().entrySet()) {
				if (entry.getValue().equals(member.getConstant())) {
					this.sb.append(entry.getKey());
					break;
				}
			}
		}

		@Override
		public void visit(FunctionMember member) {
			for (Map.Entry<String, Function> entry : getFunctions().entrySet()) {
				if (entry.getValue().equals(member.getFunction())) {
					this.sb.append(entry.getKey());
					this.sb.append(getSyntax().getFunctionParameterStart());
					boolean first = true;
					for (FormulaMember parameter : member.getMembers()) {
						if (first)
							first = false;
						else {
							this.sb.append(getSyntax().getFunctionParameterSeparator());
							this.sb.append(' ');
						}
						parameter.accept(this);
					}
					this.sb.append(getSyntax().getFunctionParameterEnd());
					break;
				}
			}
		}

		@Override
		public void visit(OperatorMember member) {
			Operator o = member.getOperator();
			for (Pair<String, Operator> pair : getOperators()) {
				if (pair.getValue2().equals(o)) {
					boolean first = true;
					// Opérateur unaire comme "-1"
					if (o.isUnary() && !o.isUnarySuffix())
						this.sb.append(pair.getValue1());
					// Liste séparée par l'opérateur
					for (FormulaMember parameter : member.getMembers()) {
						if (first)
							first = false;
						else {
							this.sb.append(' ');
							this.sb.append(pair.getValue1());
							this.sb.append(' ');
						}
						parameter.accept(this);
					}
					// Opérateur unaire comme "2²"
					if (o.isUnary() && o.isUnarySuffix())
						this.sb.append(pair.getValue1());
					break;
				}
			}
		}

		@Override
		public void visit(ValueMember member) {
			this.sb.append(getSyntax().format(member));
		}

		@Override
		public void visit(VariableMember member) {
			for (Map.Entry<String, Variable> entry : getVariables().entrySet()) {
				if (entry.getValue().equals(member.getVariable())) {
					this.sb.append(entry.getKey());
					break;
				}
			}
		}
	}

	private class ReduceVisitor implements FormulaVisitor {

		private FormulaMember member;

		public ReduceVisitor() {
			super();
		}

		public FormulaMember getMember() {
			return this.member;
		}

		@Override
		public void visit(BlockMember visited) {
			visited.getMember().accept(this);
		}

		@Override
		public void visit(ConstantMember visited) {
			Constant c = visited.getConstant();
			this.member = new ValueMember(c.getType(), c.getValue());
		}

		@Override
		public void visit(FunctionMember visited) {
			ReduceMemberList list = new ReduceMemberList(visited.getMembers());
			this.member = visited.getFunction().getReducedMember(list);
			if (this.member == null) {
				FunctionMember result = new FunctionMember(visited.getFunction());
				result.getMembers().addAll(list);
				this.member = result;
			}
		}

		@Override
		public void visit(OperatorMember visited) {
			ReduceMemberList list = new ReduceMemberList(visited.getMembers());
			this.member = visited.getOperator().getReducedMember(list);
			if (this.member == null) {
				OperatorMember result = new OperatorMember(visited.getOperator());
				result.getMembers().addAll(list);
				this.member = result;
			}

			if (this.member instanceof OperatorMember) {
				OperatorMember parent = (OperatorMember) this.member;
				int parentPriority = getPriority(parent.getOperator());
				for (int i = 0; i < parent.getMembers().size(); i++) {
					if (parent.getMembers().get(i) instanceof OperatorMember) {
						OperatorMember child = (OperatorMember) parent.getMembers().get(i);
						int childPriority = getPriority(child.getOperator());
						if (childPriority < parentPriority)
							parent.getMembers().set(i, new BlockMember(child));
					}
				}
			}
		}

		@Override
		public void visit(ValueMember visited) {
			this.member = visited;
		}

		@Override
		public void visit(VariableMember visited) {
			this.member = visited;
		}

		private int getPriority(Operator operator) {
			for (int i = 0; i < getOperators().size(); i++) {
				Pair<String, Operator> pair = getOperators().get(i);
				if (pair.getValue2().equals(operator))
					return i;
			}
			throw new InvalidParameterException();
		}
	}

	private class ReduceMemberList extends AbstractList<FormulaMember> {

		private ReduceVisitor visitor;
		private List<FormulaMember> internalList;
		private FormulaMember[] members;

		public ReduceMemberList(List<FormulaMember> internalList) {
			super();
			this.visitor = new ReduceVisitor();
			this.internalList = internalList;
			this.members = new FormulaMember[internalList.size()];
		}

		@Override
		public FormulaMember get(int index) {
			if (this.members[index] == null) {
				this.internalList.get(index).accept(this.visitor);
				this.members[index] = this.visitor.getMember();
			}
			return this.members[index];
		}

		@Override
		public int size() {
			return this.members.length;
		}

	}

	public final static class Pair<T, U> {

		private final T value1;
		private final U value2;

		public Pair(T value1, U value2) {
			super();
			this.value1 = value1;
			this.value2 = value2;
		}

		public static final <T, U> Pair<T, U> of(T value1, U value2) {
			return new Pair<>(value1, value2);
		}

		public T getValue1() {
			return this.value1;
		}

		public U getValue2() {
			return this.value2;
		}

	}

}
