package fr.techgp.nimbus.utils.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

public class JSONTest {

	public static void main(String[] args) {
		try {
			JSONTest t = new JSONTest();
			t.testEncoder();
			t.testDecoder();
			t.testMoreComplexContent();
			t.testPerformance();
			t.testStream();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void testEncoder() {
		JSONEncoder encoder = new JSONEncoder();
		// Values
		encodeEquals(encoder, JSON.ofNull(), "null");
		encodeEquals(encoder, JSON.of(true), "true");
		encodeEquals(encoder, JSON.of(false), "false");
		encodeEquals(encoder, JSON.of(0), "0");
		encodeEquals(encoder, JSON.of(10), "10");
		encodeEquals(encoder, JSON.of(12.3), "12.3");
		encodeEquals(encoder, JSON.of("toto titi"), "\"toto titi\"");
		encodeEquals(encoder, JSON.of("Toto\t\"Test\""), "\"Toto\\t\\\"Test\\\"\"");
		// Arrays
		encodeEquals(encoder, JSON.array(), "[]");
		encodeEquals(encoder, JSON.array().add(12), "[12]");
		encodeEquals(encoder, JSON.array().add(12).add("Coucou"), "[12,\"Coucou\"]");
		encodeEquals(encoder, JSON.array().add(12).add("Coucou").addNull(), "[12,\"Coucou\",null]");
		// Objects
		encodeEquals(encoder, JSON.object(), "{}");
		encodeEquals(encoder, JSON.object().set("a", 12), "{\"a\":12}");
		encodeEquals(encoder, JSON.object().set("a", 12).set("Autre chose", "Coucou"), "{\"a\":12,\"Autre chose\":\"Coucou\"}");
		encodeEquals(encoder, JSON.object().set("a", 12).set("Autre chose", "Coucou").setNull("nothing"), "{\"a\":12,\"Autre chose\":\"Coucou\",\"nothing\":null}");
		encodeEquals(encoder, JSON.object().set("a", 12).set("b", true).setNull("c").set("d", "e"), "{\"a\":12,\"b\":true,\"c\":null,\"d\":\"e\"}");
		// Formating
		encodeEquals(encoder, JSON.object(), "{}");
		encodeEquals(encoder.beautify(), JSON.object(), "{\n}");
		encodeEquals(encoder.beautify(), JSON.object().set("a", 12).setNull("b"), "{\n\t\"a\": 12,\n\t\"b\": null\n}");
		encodeEquals(encoder.beautify("  ", false, false), JSON.object().set("a", 12).setNull("b"), "{\n  \"a\":12\n}");
	}

	public void testDecoder() {
		// Values
		decodeSuccess("null", JSON.ofNull());
		decodeSuccess("true", JSON.of(true));
		decodeSuccess("false", JSON.of(false));
		decodeSuccess("\"toto titi\"", JSON.of("toto titi"));
		decodeSuccess("12345678.9", JSON.of(12345678.9));
		decodeSuccess("123456789", JSON.of(123456789L));
		// Arrays
		decodeSuccess("[]", JSON.array());
		decodeSuccess("[ 12 ]", JSON.array().add(12L));
		decodeSuccess("[ 12, true ]", JSON.array().add(12L).add(true));
		// Objects
		decodeSuccess("{}", JSON.object());
		decodeSuccess("{ \"toto\": 12}", JSON.object().set("toto", 12L));
		decodeSuccess("{ \"toto\": 12,\t \" un nom de propriété bizarre\": \n[]}", JSON.object().set("toto", 12L).set(" un nom de propriété bizarre", JSON.array()));
		// Expected errors
		decodeFailure("",  "Unexpected end of stream");
		decodeFailure("[ \"toto ]",  "Un-terminated string started at position 2");
		decodeFailure("{ \"toto\" , }",  "Found \",\" but expecting \":\" at position 9");
		decodeFailure("12 true", "Found \"true\" but expecting end of stream at position 3");
		decodeFailure(" { un nom de propriété invalid: 12}",  "un is not a valid object property name");
		decodeFailure(" { \"toto\": abcd}",  "abcd is not a valid value");
	}

	public void testMoreComplexContent() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(JSONTest.class.getResourceAsStream("test.json"), "UTF-8"))) {
			List<String> strings = reader.lines().collect(Collectors.toList());
			String jsonIn = strings.stream().reduce((content, line) -> content + '\n' + line).orElse("null");
			JSONElement element = JSON.decode(jsonIn);
			String jsonOut = JSON.format(element, "  ", true, false);
			if (!jsonOut.equals(jsonIn))
				throw new RuntimeException("La lecture du fichier a échoué (" + jsonOut + " <> " + jsonIn + ")");
		} catch (Exception ex) {
			throw new RuntimeException("La lecture du fichier a échoué", ex);
		}
	}

	public void testPerformance() {
		JSONPerf.execute();
	}

	public void testStream() {
		JSONStreamTest.execute();
	}

	private static final void encodeEquals(JSONEncoder encoder, JSONElement element, String json) {
		try {
			String result = encoder.encode(element);
			if (!result.equals(json))
				throw new RuntimeException("Encodage JSON échoué (" + result + " <> " + json + ")");
		} catch (RuntimeException ex) {
			throw new RuntimeException(json + " a lancé une erreur inattendue", ex);
		}
	}

	private static final void decodeSuccess(String expression, JSONElement element) {
		try {
			JSONElement e = new JSONDecoder().decode(" " + expression + " ");
			if (!element.getClass().equals(e.getClass()))
				throw new RuntimeException("Décodage JSON échoué (" + e.getClass().getSimpleName() + " <> " + element.getClass().getSimpleName() + ")");
			if (!element.equals(e))
				throw new RuntimeException("Décodage JSON échoué (" + element.toJSON() + " <> " + e.toJSON() + ")");
		} catch (ParseException ex) {
			throw new RuntimeException(expression + " n'a pas pû être parsée", ex);
		}
	}

	private static final void decodeFailure(String expression, String error) {
		try {
			new JSONDecoder().decode(expression);
		} catch (Exception ex) {
			if (!(ex instanceof ParseException))
				throw new RuntimeException("Erreur inattendue (" + ex.getClass().getSimpleName() + " <> " + ParseException.class.getSimpleName() + ")");
			if (!error.equals(ex.getMessage()))
				throw new RuntimeException("Erreur inattendue (" + ex.getMessage() + " <> " + error + ")");
			return;
		}
		throw new RuntimeException(expression + " aurait dû planter");
	}

}
