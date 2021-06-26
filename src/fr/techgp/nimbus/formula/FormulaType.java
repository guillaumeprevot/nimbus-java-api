package fr.techgp.nimbus.formula;

import java.util.Date;
import java.util.List;

public enum FormulaType {

	Boolean {

		@Override
		public Class<?> getValueClass() {
			return Boolean.class;
		}

		@Override
		public int compare(Object value1, Object value2) {
			return ((Boolean) value1).compareTo((Boolean) value2);
		}

	},
	Long {

		@Override
		public Class<?> getValueClass() {
			return Long.class;
		}

		@Override
		public int compare(Object value1, Object value2) {
			Number n1 = (Number) value1;
			Number n2 = (Number) value2;
			return java.lang.Long.compare(n1.longValue(), n2.longValue());
		}

	},
	Double {

		@Override
		public Class<?> getValueClass() {
			return Double.class;
		}

		@Override
		public int compare(Object value1, Object value2) {
			Number n1 = (Number) value1;
			Number n2 = (Number) value2;
			return java.lang.Double.compare(n1.doubleValue(), n2.doubleValue());
		}

		@Override
		public boolean convertableFrom(FormulaType inputType) {
			return super.convertableFrom(inputType)
					|| Number.class.isAssignableFrom(inputType.getValueClass());
		}

		@Override
		public Object convertFrom(Object value, FormulaType inputType) {
			if (value instanceof Number)
				return ((Number) value).doubleValue();
			return super.convertFrom(value, inputType);
		}

	},
	Date {

		@Override
		public Class<?> getValueClass() {
			return Date.class;
		}

		@Override
		public int compare(Object value1, Object value2) {
			return ((Date) value1).compareTo((Date) value2);
		}

	},
	Time {

		@Override
		public Class<?> getValueClass() {
			return Date.class;
		}

		@Override
		public int compare(Object value1, Object value2) {
			return ((Date) value1).compareTo((Date) value2);
		}

	},
	DateTime {

		@Override
		public Class<?> getValueClass() {
			return Date.class;
		}

		@Override
		public int compare(Object value1, Object value2) {
			return ((Date) value1).compareTo((Date) value2);
		}

		@Override
		public boolean convertableFrom(FormulaType inputType) {
			return super.convertableFrom(inputType)
					|| Date.class.isAssignableFrom(inputType.getValueClass());
		}

		@Override
		public Object convertFrom(Object value, FormulaType inputType) {
			if (value instanceof Date)
				return ((Date) value).clone();
			return super.convertFrom(value, inputType);
		}

	},
	String {

		@Override
		public Class<?> getValueClass() {
			return String.class;
		}

		@Override
		public int compare(Object value1, Object value2) {
			return ((String) value1).compareTo((String) value2);
		}

	},
	Null {

		@Override
		public Class<?> getValueClass() {
			return null;
		}

		@Override
		public int compare(Object value1, Object value2) {
			return 0;
		}

	};

	public abstract Class<?> getValueClass();

	public abstract int compare(Object value1, Object value2);

	public boolean convertableTo(FormulaType outputType) {
		return outputType.convertableFrom(this);
	}

	public Object convertTo(Object value, FormulaType outputType) {
		return outputType.convertFrom(value, this);
	}

	public boolean convertableFrom(FormulaType inputType) {
		return inputType == Null;
	}

	public Object convertFrom(Object value, FormulaType inputType) {
		if (value == null || inputType == Null)
			return null;
		throw new UnsupportedOperationException();
	}

	public static boolean compatible(FormulaType t1, FormulaType t2) {
		return (t1 == t2)
				|| t1.convertableFrom(t2) || t1.convertableTo(t2)
				|| t2.convertableFrom(t1) || t2.convertableTo(t1);
	}

	public static boolean compatible(FormulaMember m1, FormulaMember m2) {
		return compatible(m1.getType(), m2.getType());
	}

	public static boolean compatible(List<FormulaMember> members) {
		FormulaType type = members.get(0).getType();
		for (int i = 1; i < members.size(); i++) {
			if (!compatible(type, members.get(i).getType()))
				return false;
		}
		return true;
	}

	public static FormulaType compatibleType(FormulaType t1, FormulaType t2) {
		if (t1 == t2)
			return t1;
		if (t1.convertableFrom(t2) || t2.convertableTo(t1))
			return t1;
		if (t2.convertableFrom(t1) || t1.convertableTo(t2))
			return t2;
		return null;
	}

	public static FormulaType compatibleType(FormulaMember m1, FormulaMember m2) {
		return compatibleType(m1.getType(), m2.getType());
	}

	public static FormulaType compatibleType(List<FormulaMember> members) {
		FormulaType type = members.get(0).getType();
		for (int i = 1; i < members.size(); i++) {
			type = compatibleType(type, members.get(i).getType());
			if (type == null)
				break;
		}
		return type;
	}

}
