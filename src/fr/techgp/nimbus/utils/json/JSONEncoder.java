package fr.techgp.nimbus.utils.json;

import java.util.Map;

public class JSONEncoder implements JSONVisitor {

	/** @see https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/stream/JsonWriter.java */
	private static final String[] CHAR_REPLACEMENTS = new String[128];
	static {
		for (int i = 0; i <= 0x1f; i++) {
			CHAR_REPLACEMENTS[i] = String.format("\\u%04x", i);
		}
		CHAR_REPLACEMENTS['"'] = "\\\"";
		CHAR_REPLACEMENTS['\\'] = "\\\\";
		CHAR_REPLACEMENTS['\r'] = "\\r";
		CHAR_REPLACEMENTS['\n'] = "\\n";
		CHAR_REPLACEMENTS['\t'] = "\\t";
		CHAR_REPLACEMENTS['\b'] = "\\b";
		CHAR_REPLACEMENTS['\f'] = "\\f";
	}

	private StringBuilder sb = new StringBuilder();
	private String indent = null;
	private boolean spaceAfterColumn = false;
	private boolean encodeNullProperties = false;
	private int depth;

	public JSONEncoder() {
		//
	}

	public JSONEncoder beautify() {
		return this.beautify("\t", true, true);
	}

	public JSONEncoder beautify(String indentOrNull, boolean spaceAfterColumn, boolean encodeNullProperties) {
		this.indent = indentOrNull;
		this.spaceAfterColumn = spaceAfterColumn;
		this.encodeNullProperties = encodeNullProperties;
		return this;
	}

	public String encode(JSONElement element) {
		this.sb.setLength(0);
		this.depth = 0;
		element.accept(this);
		return this.sb.toString();
	}

	@Override
	public void visit(JSONArray json) {
		this.open("[");
		boolean first = true;
		for (JSONElement e : json.iterate()) {
			if (first)
				first = false;
			else
				this.sb.append(",");
			this.indent();
			e.accept(this);
		}
		this.close("]");
	}

	@Override
	public void visit(JSONObject json) {
		this.open("{");
		boolean first = true;
		for (Map.Entry<String, JSONElement> property : json.iterate()) {
			JSONElement e = property.getValue();
			if (!this.encodeNullProperties && e.isNull())
				continue;
			if (first)
				first = false;
			else
				this.sb.append(",");
			this.indent();
			this.encode(property.getKey());
			this.sb.append(this.spaceAfterColumn ? ": " : ":");
			e.accept(this);
		}
		this.close("}");
	}

	@Override
	public void visit(JSONNull json) {
		this.sb.append("null");
	}

	@Override
	public void visit(JSONBoolean json) {
		this.sb.append(Boolean.toString(json.getValue()));
	}

	@Override
	public void visit(JSONString json) {
		this.encode(json.getValue());
	}

	@Override
	public void visit(JSONNumber json) {
		this.sb.append(json.getValue().toString());
	}

	private void open(String opening) {
		this.depth++;
		this.sb.append(opening);
	}

	private void close(String closing) {
		this.depth--;
		this.indent();
		this.sb.append(closing);
	}

	private void indent() {
		if (this.indent != null) {
			this.sb.append("\n");
			for (int i = 0; i < this.depth; i++) {
				this.sb.append(this.indent);
			}
		}
	}

	private void encode(String value) {
		this.sb.append("\"");
		// needs escaping : this.sb.append(value);
		int length = value.length();
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			String replacement = null;
			if (c < 128) {
				replacement = CHAR_REPLACEMENTS[c];
			} else if (c == '\u2028') {
				replacement = "\\u2028";
			} else if (c == '\u2029') {
				replacement = "\\u2029";
			}
			if (replacement == null)
				this.sb.append(c);
			else
				this.sb.append(replacement);
		}
		this.sb.append("\"");
	}
}
