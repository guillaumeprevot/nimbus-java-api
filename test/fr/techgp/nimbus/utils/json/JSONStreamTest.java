package fr.techgp.nimbus.utils.json;

import com.google.gson.JsonElement;

import fr.techgp.nimbus.utils.json.JSONStream.JSONStreamElementRenderer;
import fr.techgp.nimbus.utils.json.JSONStream.JSONStreamGSONRenderer;
import fr.techgp.nimbus.utils.json.JSONStream.JSONStreamIndentedRenderer;
import fr.techgp.nimbus.utils.json.JSONStream.JSONStreamRenderer;
import fr.techgp.nimbus.utils.json.JSONStream.JSONStreamStringRenderer;

public class JSONStreamTest {

	public static void execute() {
		String indented = "[\n true,\n 0.5,\n 1024,\n \"Hello\\tWorld!\\\\°/\",\n null,\n {\n  \"p1\": null,\n  \"p2\": true,\n  \"p3\": []\n },\n [\n  null,\n  true,\n  {}\n ]\n]";
		String compressed = indented.replace(" ", "").replace("\n", "");

		StringBuilder sb = new StringBuilder();
		JSONStreamStringRenderer renderer1 = new JSONStreamStringRenderer(sb::append);
		test(renderer1);
		System.out.println(compressed.equals(sb.toString()) + " for " + JSONStreamStringRenderer.class.getName());

		JSONStreamGSONRenderer renderer2 = new JSONStreamGSONRenderer();
		test(renderer2);
		JsonElement gson = renderer2.getResult();
		System.out.println(compressed.equals(gson.toString()) + " for " + JSONStreamGSONRenderer.class.getName());

		JSONStreamElementRenderer renderer3 = new JSONStreamElementRenderer();
		test(renderer3);
		JSONElement json = renderer3.getResult();
		System.out.println(compressed.equals(json.toJSON()) + " for " + JSONStreamElementRenderer.class.getName());

		sb.setLength(0);
		JSONStreamIndentedRenderer renderer4 = new JSONStreamIndentedRenderer(sb::append, " ", true);
		test(renderer4);
		System.out.println(indented.equals(sb.toString()) + " for " + JSONStreamIndentedRenderer.class.getName());
		JSONStreamRenderer renderer = new JSONStreamStringRenderer(System.out::print);
		JSON.stream(renderer).objectValue()
			.name("text").value("value")
			.name("number").value(2.5)
			.name("boolean").value(true)
			.name("nullProperty").nullValue()
			.name("array").arrayValue()
				.add(1)
				.add(2)
				.end()
			.end();
	}

	private static final void test(JSONStreamRenderer renderer) {
		JSON.stream(renderer).arrayValue()
			.add(true)
			.add(0.5)
			.add(1024)
			.add("Hello\tWorld!\\°/") // test JSON escaping
			.addNull()
			.addObject()
				.name("p1").nullValue()
				.name("p2").value(true)
				.name("p3").arrayValue().end() // empty array
				.end()
			.addArray()
				.addNull()
				.add(true)
				.addObject().end() // empty object
				.end()
			.end();
	}

}
