package fr.techgp.nimbus.formula.impl;

import fr.techgp.nimbus.formula.FormulaParser;
import fr.techgp.nimbus.formula.FormulaParser.Pair;
import fr.techgp.nimbus.formula.functions.CalculateFunction;
import fr.techgp.nimbus.formula.functions.DateFunction;
import fr.techgp.nimbus.formula.functions.HexaFunction;
import fr.techgp.nimbus.formula.functions.IfFunction;
import fr.techgp.nimbus.formula.functions.IsNullFunction;
import fr.techgp.nimbus.formula.functions.NoNullFunction;
import fr.techgp.nimbus.formula.functions.NoZeroFunction;
import fr.techgp.nimbus.formula.functions.SqrtFunction;
import fr.techgp.nimbus.formula.functions.TypeFunction;
import fr.techgp.nimbus.formula.functions.UUIDFunction;
import fr.techgp.nimbus.formula.operators.AdditionOperator;
import fr.techgp.nimbus.formula.operators.AndOperator;
import fr.techgp.nimbus.formula.operators.ConcatenationOperator;
import fr.techgp.nimbus.formula.operators.DifferentOperator;
import fr.techgp.nimbus.formula.operators.DivisionOperator;
import fr.techgp.nimbus.formula.operators.EqualOperator;
import fr.techgp.nimbus.formula.operators.LessOperator;
import fr.techgp.nimbus.formula.operators.LessOrEqualOperator;
import fr.techgp.nimbus.formula.operators.ModuloOperator;
import fr.techgp.nimbus.formula.operators.MoreOperator;
import fr.techgp.nimbus.formula.operators.MoreOrEqualOperator;
import fr.techgp.nimbus.formula.operators.MultiplicationOperator;
import fr.techgp.nimbus.formula.operators.NegativeOperator;
import fr.techgp.nimbus.formula.operators.NotOperator;
import fr.techgp.nimbus.formula.operators.OrOperator;
import fr.techgp.nimbus.formula.operators.Power2Operator;
import fr.techgp.nimbus.formula.operators.PowerOperator;
import fr.techgp.nimbus.formula.operators.SubstractOperator;

public final class DefaultParsers {

	private DefaultParsers() {
		super();
	}

	public static final FormulaParser createInternalParser() {
		FormulaParser parser = new FormulaParser();

		parser.getConstants().put("pi", new PiConstant());

		parser.getOperators().add(Pair.of("|", new OrOperator()));
		parser.getOperators().add(Pair.of("&", new AndOperator()));
		parser.getOperators().add(Pair.of(">=", new MoreOrEqualOperator()));
		parser.getOperators().add(Pair.of("<=", new LessOrEqualOperator()));
		parser.getOperators().add(Pair.of("<>", new DifferentOperator()));
		parser.getOperators().add(Pair.of("=", new EqualOperator()));
		parser.getOperators().add(Pair.of(">", new MoreOperator()));
		parser.getOperators().add(Pair.of("<", new LessOperator()));
		parser.getOperators().add(Pair.of("+", new AdditionOperator()));
		parser.getOperators().add(Pair.of("-", new SubstractOperator()));
		parser.getOperators().add(Pair.of("*", new MultiplicationOperator()));
		parser.getOperators().add(Pair.of("/", new DivisionOperator()));
		parser.getOperators().add(Pair.of("%", new ModuloOperator()));
		parser.getOperators().add(Pair.of("^", new PowerOperator()));
		parser.getOperators().add(Pair.of("+", new ConcatenationOperator()));
		parser.getOperators().add(Pair.of("-", new NegativeOperator()));
		parser.getOperators().add(Pair.of("²", new Power2Operator()));
		parser.getOperators().add(Pair.of("!", new NotOperator()));

		parser.getFunctions().put("Calculate", new CalculateFunction());
		parser.getFunctions().put("Date", new DateFunction());
		parser.getFunctions().put("Hexa", new HexaFunction());
		parser.getFunctions().put("If", new IfFunction());
		parser.getFunctions().put("IsNull", new IsNullFunction());
		parser.getFunctions().put("NoNull", new NoNullFunction());
		parser.getFunctions().put("NoZero", new NoZeroFunction());
		parser.getFunctions().put("Sqrt", new SqrtFunction());
		parser.getFunctions().put("Type", new TypeFunction());
		parser.getFunctions().put("UUID", new UUIDFunction());

		return parser;
	}

}
