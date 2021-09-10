package fr.techgp.nimbus.utils.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONObject implements JSONElement {

	private final Map<String, JSONElement> children = new LinkedHashMap<>(3);

	public JSONObject() {
		super();
	}

	public JSONObject set(String property, JSONElement element) {
		return this.setChecked(property, JSON.checked(element));
	}

	public JSONObject set(String property, Boolean value) {
		return this.setChecked(property, JSON.of(value));
	}

	public JSONObject set(String property, boolean value) {
		return this.setChecked(property, JSON.of(value));
	}

	public JSONObject set(String property, String value) {
		return this.setChecked(property, JSON.of(value));
	}

	public JSONObject set(String property, Number value) {
		return this.setChecked(property, JSON.of(value));
	}

	public JSONObject set(String property, long value) {
		return this.setChecked(property, JSON.of(value));
	}

	public JSONObject set(String property, double value) {
		return this.setChecked(property, JSON.of(value));
	}

	public JSONObject setNull(String property) {
		return this.setChecked(property, JSON.ofNull());
	}

	private JSONObject setChecked(String property, JSONElement element) {
		this.children.put(property, element);
		return this;
	}

	public int size() {
		return this.children.size();
	}

	public boolean isEmpty() {
		return this.children.isEmpty();
	}

	public boolean has(String property) {
		return this.children.containsKey(property);
	}

	public JSONElement get(String property) {
		return this.children.get(property);
	}

	public JSONElement remove(String property) {
		return this.children.remove(property);
	}

	public JSONObject clear() {
		this.children.clear();
		return this;
	}

	public Iterable<Map.Entry<String, JSONElement>> iterate() {
		return this.children.entrySet();
	}

	@Override
	public JSONElement deepCopy() {
		JSONObject copy = new JSONObject();
		for (Map.Entry<String, JSONElement> entry : this.children.entrySet()) {
			copy.children.put(entry.getKey(), entry.getValue().deepCopy());
		}
		return copy;
	}

	@Override
	public void accept(JSONVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public JSONObject asObject() {
		return this;
	}

	@Override
	public int hashCode() {
		return this.children.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj) || (obj instanceof JSONObject && this.children.equals(((JSONObject) obj).children));
	}

}
