package fr.techgp.nimbus.utils.json;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSONDecoder {

	private static final char[] SEPARATORS = "{}[]:, ".toCharArray();
	private String expression;
	private int index;

	public JSONDecoder() {
		super();
	}

	public JSONElement decode(String json) throws ParseException {
		this.expression = json.trim();
		this.index = 0;
		return this.eParser();
	}

	/** stops the parsing process and reports an error. */
	private void error(String message, Object... messageParams) throws ParseException {
		throw new ParseException(String.format(message, messageParams), this.index);
	}

	/** returns the next token of input or an empty string when parsing is over. "next" does not alter the input stream. */
	private String next() throws ParseException {
		// Parsing after string end throws an error
		if (this.index >= this.expression.length())
			this.error("Unexpected end of stream");
		// Check if a string starts at current position
		if (this.expression.charAt(this.index) == '"') {
			// In that case, find the next closing quote
			char previous = ' ';
			char escape = '\\';
			int i = this.index + 1;
			while (i < this.expression.length() && (this.expression.charAt(i) != '"' || escape == previous)) { // skip despecialized quotes
				if (escape == previous && escape == this.expression.charAt(i)) // \" won't end string but \\" will
					previous = ' '; // so each couple \\ is ignored
				else
					previous = this.expression.charAt(i);
				i++;
			}
			if (i == this.expression.length())
				this.error("Un-terminated string started at position %d", this.index);
			// Found a string
			return this.expression.substring(this.index, i + 1);
		}
		// Search the next occurence of each separators
		int index = Integer.MAX_VALUE;
		for (char c : JSONDecoder.SEPARATORS) {
			int i = this.expression.indexOf(c, this.index);
			if (i >= 0 && i < index)
				index = i;
		}
		// found a token ending stream
		if (index == Integer.MAX_VALUE)
			return this.expression.substring(this.index);
		// found a separator at current position
		if (index == this.index)
			return this.expression.substring(this.index, this.index + 1);
		// found a token from this current position to the next separator
		return this.expression.substring(this.index, index).trim();
	}

	/** reads one token. When "next=end", consume is still allowed, but has no effect. */
	private void consume(String token) throws ParseException {
		// Get next token to consume, or use text if provided as optimisation
		// In fact, this.next() is never called because each call to 'consume' already knowns what is the next token (= "text" argument)
		String s = token == null ? this.next() : token;
		// Move forward
		this.index += s.length();
		// And skip following spaces
		while (this.index < this.expression.length() && Character.isWhitespace(this.expression.charAt(this.index)))
			this.index++;
	}

	/** if next = text then consume else error */
	private void expect(String token) throws ParseException {
		// Get next token
		String s = this.next();
		// Check if this token matches expected text
		if (s.equals(token))
			// OK, consume token
			this.consume(s);
		else
			// Error, the next token is unexpected
			this.error("Found \"%s\" but expecting \"%s\" at position %d", s, token, this.index);
	}

	private JSONElement eParser() throws ParseException {
		// Try to get the Abstract Syntax Tree (AST) for the expression starting at position 0
		JSONElement e = this.parseElement();
		// Ensure that the end of the formula is reached, like expected
		if (this.index != this.expression.length())
			this.error("Found \"%s\" but expecting end of stream at position %d", this.next(), this.index);
		// Return the AST
		return e;
	}

	private JSONElement parseElement() throws ParseException {
		String s = this.next();
		if ("[".equals(s))
			return parseArray();
		if ("{".equals(s))
			return parseObject();
		return parseValue(s);
	}

	private JSONArray parseArray() throws ParseException {
		this.consume("[");
		JSONArray result = new JSONArray();
		String s = this.next();
		while (!"]".equals(s)) {
			result.add(parseElement());
			s = this.next();
			if (!"]".equals(s)) {
				this.expect(",");
				s = this.next();
			}
		}
		this.expect("]");
		return result;
	}

	private JSONObject parseObject() throws ParseException {
		this.consume("{");
		JSONObject result = new JSONObject();
		String s = this.next();
		while (!"}".equals(s)) {
			if (s.length() < 3 || s.charAt(0) != '\"' || s.charAt(s.length() - 1) != '\"')
				this.error("%s is not a valid object property name", s);
			String propertyName = s.substring(1, s.length() - 1);
			this.consume(s);
			this.expect(":");
			JSONElement propertyValue = this.parseElement();
			result.set(unescape(propertyName), propertyValue);
			s = this.next();
			if (!"}".equals(s)) {
				this.expect(",");
				s = this.next();
			}
		}
		this.expect("}");
		return result;
	}

	private JSONElement parseValue(String token) throws ParseException {
		JSONElement result = null;
		if ("null".equals(token))
			result = JSONNull.INSTANCE;
		else if ("true".equals(token))
			result = JSONBoolean.TRUE_INSTANCE;
		else if ("false".equals(token))
			result = JSONBoolean.FALSE_INSTANCE;
		else if (token.charAt(0) == '"' && token.charAt(token.length() - 1) == '"')
			result = new JSONString(unescape(token.substring(1, token.length() - 1)));
		else {
			try {
				result = new JSONNumber(Long.parseLong(token));
			} catch (NumberFormatException ex) {
				try {
					result = new JSONNumber(Double.parseDouble(token));
				} catch (NumberFormatException ex2) {
					this.error("%s is not a valid value", token);
				}
			}
		}
		this.consume(token);
		return result;
	}

	private static final List<String> UNESCAPE_STRINGS = new ArrayList<>();
	private static final List<Character> UNESCAPE_CHARS = new ArrayList<>();
	static {
		UNESCAPE_STRINGS.addAll(Arrays.asList("\\\"", "\\\\", "\\r", "\\n", "\\t", "\\b", "\\f", "\\u2028", "\\u2029"));
		UNESCAPE_CHARS.addAll(Arrays.asList('"', '\\', '\r', '\n', '\t', '\b', '\f', '\u2028', '\u2029'));
		for (int i = 0; i <= 0x1f; i++) {
			UNESCAPE_STRINGS.add(String.format("\\u%04x", i));
			UNESCAPE_CHARS.add((char) i);
		}
	}

	private static final String unescape(String s) {
		String val = s;
		int position = 0;
		int index = val.indexOf('\\', position);
		while (index >= 0) {
			for (int i = 0; i < UNESCAPE_STRINGS.size(); i++) {
				if (val.startsWith(UNESCAPE_STRINGS.get(i), index)) {
					val = val.substring(0, index) + UNESCAPE_CHARS.get(i) + val.substring(index + UNESCAPE_STRINGS.get(i).length());
				}
			}
			position = index + 1;
			index = val.indexOf('\\', position);
		}
		return val;
	}

}
