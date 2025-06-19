package fr.techgp.nimbus.utils.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * http://blog.takipi.com/the-ultimate-json-library-json-simple-vs-gson-vs-jackson-vs-json/
 */
public final class JSONPerf {

	public static final class JSONResult {

		public long time1;
		public long time2;
		public long time3;
		public boolean isArray;
		public boolean isObject;
		public boolean isValue;
		public String output;

	}

	public static class JSONApi<T> {

		private String name;
		private String url;
		private String lastTestedVersion;
		private Function<String, T> parser;
		private Function<T, String> formatter;
		private Predicate<T> isArray;
		private Predicate<T> isObject;
		private Predicate<T> isValue;

		public JSONApi(String name, String url, String lastTestedVersion,
				Function<String, T> parser, Function<T, String> formatter,
				Predicate<T> isArray, Predicate<T> isObject, Predicate<T> isValue) {
			super();
			this.name = name;
			this.url = url;
			this.lastTestedVersion = lastTestedVersion;
			this.parser = parser;
			this.formatter = formatter;
			this.isArray = isArray;
			this.isObject = isObject;
			this.isValue = isValue;
		}

		public String getName() {
			return this.name;
		}

		public String getUrl() {
			return this.url;
		}

		public String getLastTestedVersion() {
			return this.lastTestedVersion;
		}

		public JSONResult run(String json) {
			JSONResult r = new JSONResult();
			r.time1 = System.nanoTime();
			T value = this.parser.apply(json);
			r.time2 = System.nanoTime();
			r.output = this.formatter.apply(value);
			r.time3 = System.nanoTime();
			r.isArray = this.isArray.test(value);
			r.isObject = this.isObject.test(value);
			r.isValue = this.isValue.test(value);
			return r;
		}

	}

	public static final JSONApi<com.google.gson.JsonElement> GOOGLE_GSON = new JSONApi<>("Google GSON", "https://github.com/google/gson", "2.8.7", (s) -> {
			try {
				return com.google.gson.JsonParser.parseString(s);
			} catch (com.google.gson.JsonSyntaxException ex) {
				return null;
			}
		},
		com.google.gson.JsonElement::toString,
		com.google.gson.JsonElement::isJsonArray,
		com.google.gson.JsonElement::isJsonObject,
		com.google.gson.JsonElement::isJsonPrimitive);
/*
	public static final JSONApi<com.fasterxml.jackson.databind.JsonNode> JACKSON = new JSONApi<>("FasterXML Jackson", "https://github.com/FasterXML/jackson", "2.10.3", (s) -> {
			try {
				return new com.fasterxml.jackson.databind.ObjectMapper().readTree(s);
			} catch (Exception ex) {
				return null;
			}
		},
		com.fasterxml.jackson.databind.JsonNode::toString,
		com.fasterxml.jackson.databind.JsonNode::isArray,
		com.fasterxml.jackson.databind.JsonNode::isObject,
		com.fasterxml.jackson.databind.JsonNode::isValueNode);

	public static final JSONApi<org.json.simple.JSONAware> JSON_SIMPLE = new JSONApi<>("json-simple", "https://github.com/fangyidong/json-simple", "1.1.1", (s) -> {
			try {
				return (org.json.simple.JSONAware) new org.json.simple.parser.JSONParser().parse(s);
			} catch (org.json.simple.parser.ParseException ex) {
				return null;
			}
		},
		org.json.simple.JSONAware::toJSONString,
		(o) -> o instanceof org.json.simple.JSONArray,
		(o) -> o instanceof org.json.simple.JSONObject,
		(o) -> o instanceof org.json.simple.JSONValue);
*/
	public static final JSONApi<JSONElement> NIMBUS_API = new JSONApi<>("Nimbus", "https://github.com/guillaumeprevot/nimbus-java-api", "1.5", (s) -> {
			try {
				return JSON.decode(s);
			} catch (ParseException ex) {
				return null;
			}
		},
		JSONElement::toJSON,
		JSONElement::isArray,
		JSONElement::isObject,
		(o) -> o.isNull() || o.isBoolean() || o.isString() || o.isString());

	public static void execute() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(JSONPerf.class.getResourceAsStream("test.json"), StandardCharsets.UTF_8))) {
			List<String> strings = reader.lines().collect(Collectors.toList());
			String jsonIn = strings.stream().reduce((content, line) -> content + '\n' + line).orElse("null");
			JSONApi<?>[] apis = new JSONApi[] {
					GOOGLE_GSON,
					//JACKSON,
					//JSON_SIMPLE,
					NIMBUS_API
			};
			int loops = 2;
			while (loops > 0) {
				for (int i = 0; i < apis.length; i++) {
					JSONResult r = apis[i].run(jsonIn);
					System.out.println(String.format("Name=%11s, Array=%s, Object=%s, Value=%s, Parse=%9d, Format=%9d, Total=%10d, JSON hash=%s",
							apis[i].name, r.isArray, r.isObject, r.isValue, r.time2 - r.time1, r.time3 - r.time2, r.time3 - r.time1, r.output.hashCode()));
				}
				System.out.println();
				loops--;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
