package fr.techgp.nimbus.utils.json;

import java.util.ArrayList;
import java.util.List;

public class JSONArray implements JSONElement {

	private final List<JSONElement> children = new ArrayList<>();

	public JSONArray() {
		//
	}

	public JSONArray add(JSONElement child) {
		this.children.add(child == null ? JSON.ofNull() : child);
		return this;
	}

	public JSONArray addAll(JSONElement... children) {
		for (JSONElement child : children) {
			this.children.add(child == null ? JSON.ofNull() : child);
		}
		return this;
	}

	public JSONArray add(Boolean child) {
		return this.add(JSON.of(child));
	}

	public JSONArray add(Number child) {
		return this.add(JSON.of(child));
	}

	public JSONArray add(String child) {
		return this.add(JSON.of(child));
	}

	public JSONArray addNull() {
		return this.add((JSONElement) null);
	}

	public int size() {
		return this.children.size();
	}

	public Iterable<JSONElement> iterate() {
		return this.children;
	}

	@Override
	public JSONElement deepCopy() {
		JSONArray result = new JSONArray();
		this.children.forEach((c) -> result.add(c.deepCopy()));
		return result;
	}

	@Override
	public void accept(JSONVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public JSONArray asArray() {
		return this;
	}

	@Override
	public int hashCode() {
		return this.children.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj) || (obj instanceof JSONArray && this.children.equals(((JSONArray) obj).children));
	}

}
