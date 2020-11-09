package fr.techgp.nimbus.formula;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;

import fr.techgp.nimbus.utils.StringUtils;

public class FormulaSyntax {

	private char blockDelimiterStart = '(';
	private char blockDelimiterEnd = ')';
	private char stringDelimiterStart = '"';
	private char stringDelimiterEnd = '"';
	private char functionParameterStart = '(';
	private char functionParameterSeparator = ',';
	private char functionParameterEnd = ')';

	private int radix = 10;
	private String nullValue = null;
	private String trueValue = Boolean.TRUE.toString();
	private String falseValue = Boolean.FALSE.toString();
	private DateFormat dateFormat = DateFormat.getDateInstance();
	private DateFormat timeFormat = DateFormat.getTimeInstance();
	private DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();

	public char getBlockDelimiterStart() {
		return this.blockDelimiterStart;
	}

	public void setBlockDelimiterStart(char blockDelimiterStart) {
		this.blockDelimiterStart = blockDelimiterStart;
	}

	public char getBlockDelimiterEnd() {
		return this.blockDelimiterEnd;
	}

	public void setBlockDelimiterEnd(char blockDelimiterEnd) {
		this.blockDelimiterEnd = blockDelimiterEnd;
	}

	public char getStringDelimiterStart() {
		return this.stringDelimiterStart;
	}

	public void setStringDelimiterStart(char stringDelimiterStart) {
		this.stringDelimiterStart = stringDelimiterStart;
	}

	public char getStringDelimiterEnd() {
		return this.stringDelimiterEnd;
	}

	public void setStringDelimiterEnd(char stringDelimiterEnd) {
		this.stringDelimiterEnd = stringDelimiterEnd;
	}

	public char getFunctionParameterStart() {
		return this.functionParameterStart;
	}

	public void setFunctionParameterStart(char functionParameterStart) {
		this.functionParameterStart = functionParameterStart;
	}

	public char getFunctionParameterSeparator() {
		return this.functionParameterSeparator;
	}

	public void setFunctionParameterSeparator(char functionParameterSeparator) {
		this.functionParameterSeparator = functionParameterSeparator;
	}

	public char getFunctionParameterEnd() {
		return this.functionParameterEnd;
	}

	public void setFunctionParameterEnd(char functionParameterEnd) {
		this.functionParameterEnd = functionParameterEnd;
	}

	public int getRadix() {
		return this.radix;
	}

	public void setRadix(int radix) {
		this.radix = radix;
	}

	public String getNullValue() {
		return this.nullValue;
	}

	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

	public String getTrueValue() {
		return this.trueValue;
	}

	public void setTrueValue(String trueValue) {
		this.trueValue = trueValue;
	}

	public String getFalseValue() {
		return this.falseValue;
	}

	public void setFalseValue(String falseValue) {
		this.falseValue = falseValue;
	}

	public DateFormat getDateFormat() {
		return this.dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public DateFormat getTimeFormat() {
		return this.timeFormat;
	}

	public void setTimeFormat(DateFormat timeFormat) {
		this.timeFormat = timeFormat;
	}

	public DateFormat getDateTimeFormat() {
		return this.dateTimeFormat;
	}

	public void setDateTimeFormat(DateFormat dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	public ValueMember parse(String source) {
		if (source.equalsIgnoreCase(this.nullValue))
			return new ValueMember(FormulaType.Null, null);
		if (source.equalsIgnoreCase(this.trueValue))
			return new ValueMember(FormulaType.Boolean, Boolean.TRUE);
		if (source.equalsIgnoreCase(this.falseValue))
			return new ValueMember(FormulaType.Boolean, Boolean.FALSE);

		ValueMember member;
		member = parse(source, this.dateTimeFormat, FormulaType.DateTime);
		if (member != null)
			return member;
		member = parse(source, this.dateFormat, FormulaType.Date);
		if (member != null)
			return member;
		member = parse(source, this.timeFormat, FormulaType.Time);
		if (member != null)
			return member;

		try {
			return new ValueMember(FormulaType.Long, Long.valueOf(source, this.radix));
		} catch (NumberFormatException ex) {
			//
		}
		try {
			return new ValueMember(FormulaType.Double, Double.valueOf(source));
		} catch (NumberFormatException ex) {
			//
		}

		if (StringUtils.checkDelimitedBy(source, this.stringDelimiterStart, this.stringDelimiterEnd))
			return new ValueMember(FormulaType.String, source.substring(1, source.length() - 1));

		return null;
	}

	private static ValueMember parse(String source, DateFormat format, FormulaType type) {
		ParsePosition pos = new ParsePosition(0);
		Date value = format.parse(source, pos);
		if ((pos.getIndex() == source.length()) && (pos.getErrorIndex() == -1))
			return new ValueMember(type, value);
		return null;
	}

	public String format(ValueMember member) {
		return format(member.getValue(), member.getType());
	}

	public String format(Object value, FormulaType type) {
		if (value == null)
			return this.nullValue;
		switch (type) {
			case Null:
				return this.nullValue;
			case Boolean:
				return ((Boolean) value).booleanValue() ? this.trueValue : this.falseValue;
			case Long:
				return Long.toString(((Long) value).longValue(), this.radix).toUpperCase();
			case Double:
				return ((Double) value).toString();
			case String:
				return this.stringDelimiterStart + (String) value + this.stringDelimiterEnd;
			case Date:
				return this.dateFormat.format((Date) value);
			case Time:
				return this.timeFormat.format((Date) value);
			case DateTime:
				return this.dateTimeFormat.format((Date) value);
			default:
				throw new InvalidParameterException(
						type + " is not a valid " + FormulaType.class.getSimpleName());
		}
	}

}
