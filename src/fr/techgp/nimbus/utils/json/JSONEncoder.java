package fr.techgp.nimbus.utils.json;

import java.util.Map;

public class JSONEncoder implements JSONVisitor {

	private StringBuilder sb = new StringBuilder();
	private String indent = null;
	private boolean spaceAfterColumn = false;
	private boolean encodeNullProperties = true;
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
		JSON.checked(element).accept(this);
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
		JSON.escapeTo(value, this.sb);
		this.sb.append("\"");
	}
}
