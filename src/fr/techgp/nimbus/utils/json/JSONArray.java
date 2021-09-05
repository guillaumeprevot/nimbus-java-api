package fr.techgp.nimbus.utils.json;

import java.util.ArrayList;
import java.util.List;

public class JSONArray implements JSONElement {

	private final List<JSONElement> children = new ArrayList<>();

	public JSONArray() {
		//
	}

	public JSONArray add(JSONElement child) {
		return this.addChecked(JSON.checked(child));
	}

	public JSONArray addAll(JSONElement... children) {
		for (JSONElement child : children) {
			this.children.add(JSON.checked(child));
		}
		return this;
	}

	public JSONArray add(Boolean child) {
		return this.addChecked(JSON.of(child));
	}

	public JSONArray add(Number child) {
		return this.addChecked(JSON.of(child));
	}

	public JSONArray add(String child) {
		return this.addChecked(JSON.of(child));
	}

	public JSONArray addNull() {
		return this.addChecked(JSON.ofNull());
	}

	private JSONArray addChecked(JSONElement child) {
		this.children.add(child);
		return this;
	}

	public int size() {
		return this.children.size();
	}

	public boolean isEmpty() {
		return this.children.isEmpty();
	}

	public boolean has(JSONElement child) {
		return this.children.contains(JSON.checked(child));
	}

	public int indexOf(JSONElement child) {
		return this.children.indexOf(JSON.checked(child));
	}

	public JSONElement get(int index) {
		return this.children.get(index);
	}

	public JSONElement remove(int index) {
		return this.children.remove(index);
	}

	public boolean remove(JSONElement child) {
		return this.children.remove(JSON.checked(child));
	}

	public JSONArray clear() {
		this.children.clear();
		return this;
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
