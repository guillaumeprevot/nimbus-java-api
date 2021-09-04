package fr.techgp.nimbus.utils.json;

public interface JSONVisitor {

	public void visit(JSONNull element);

	public void visit(JSONBoolean element);

	public void visit(JSONString element);

	public void visit(JSONNumber element);

	public void visit(JSONObject element);

	public void visit(JSONArray element);

}
