package fr.techgp.nimbus.utils.json;

import java.util.Objects;
import java.util.Stack;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JSONStream {

	public static class JSONStreamContext {

		protected final JSONStreamRenderer renderer;

		public JSONStreamContext(JSONStreamRenderer renderer) {
			super();
			this.renderer = renderer;
		}

	}

	public static class JSONStreamStartContext extends JSONStreamContext {

		public JSONStreamStartContext(JSONStreamRenderer renderer) {
			super(renderer);
		}

		public JSONStreamEndContext value(Boolean value) {
			this.renderer.value(value);
			return new JSONStreamEndContext(this.renderer);
		}

		public JSONStreamEndContext value(boolean value) {
			this.renderer.value(value);
			return new JSONStreamEndContext(this.renderer);
		}

		public JSONStreamEndContext value(String value) {
			this.renderer.value(value);
			return new JSONStreamEndContext(this.renderer);
		}

		public JSONStreamEndContext value(Number value) {
			this.renderer.value(value);
			return new JSONStreamEndContext(this.renderer);
		}

		public JSONStreamEndContext value(long value) {
			this.renderer.value(value);
			return new JSONStreamEndContext(this.renderer);
		}

		public JSONStreamEndContext value(double value) {
			this.renderer.value(value);
			return new JSONStreamEndContext(this.renderer);
		}

		public JSONStreamEndContext nullValue() {
			this.renderer.nullValue();
			return new JSONStreamEndContext(this.renderer);
		}

		public JSONStreamArrayContext<JSONStreamEndContext> arrayValue() {
			return new JSONStreamArrayContext<>(this.renderer, new JSONStreamEndContext(this.renderer));
		}

		public JSONStreamObjectContext<JSONStreamEndContext> objectValue() {
			return new JSONStreamObjectContext<>(this.renderer, new JSONStreamEndContext(this.renderer));
		}

	}

	public static class JSONStreamEndContext extends JSONStreamContext {

		public JSONStreamEndContext(JSONStreamRenderer renderer) {
			super(renderer);
		}

	}

	public static class JSONStreamObjectContext<C extends JSONStreamContext> extends JSONStreamContext {

		private final C parent;
		private boolean empty = true;

		public JSONStreamObjectContext(JSONStreamRenderer renderer, C parent) {
			super(renderer);
			this.parent = parent;
			this.renderer.beginObject();
		}

		public JSONStreamPropertyContext<C> name(String name) {
			if (!this.empty)
				this.renderer.separator();
			this.empty = false;
			this.renderer.name(name);
			return new JSONStreamPropertyContext<>(this.renderer, this);
		}

		public C end() {
			this.renderer.endObject();
			return this.parent;
		}

	}

	public static class JSONStreamPropertyContext<C extends JSONStreamContext> extends JSONStreamContext {

		private final JSONStreamObjectContext<C> parent;

		public JSONStreamPropertyContext(JSONStreamRenderer renderer, JSONStreamObjectContext<C> parent) {
			super(renderer);
			this.parent = parent;
		}

		public JSONStreamObjectContext<C> value(Boolean value) {
			this.renderer.value(value);
			return this.parent;
		}

		public JSONStreamObjectContext<C> value(boolean value) {
			this.renderer.value(value);
			return this.parent;
		}

		public JSONStreamObjectContext<C> value(String value) {
			this.renderer.value(value);
			return this.parent;
		}

		public JSONStreamObjectContext<C> value(Number value) {
			this.renderer.value(value);
			return this.parent;
		}

		public JSONStreamObjectContext<C> value(long value) {
			this.renderer.value(value);
			return this.parent;
		}

		public JSONStreamObjectContext<C> set(double value) {
			this.renderer.value(value);
			return this.parent;
		}

		public JSONStreamObjectContext<C> nullValue() {
			this.renderer.nullValue();
			return this.parent;
		}

		public JSONStreamArrayContext<JSONStreamObjectContext<C>> arrayValue() {
			return new JSONStreamArrayContext<>(this.renderer, this.parent);
		}

		public JSONStreamObjectContext<JSONStreamObjectContext<C>> objectValue() {
			return new JSONStreamObjectContext<>(this.renderer, this.parent);
		}

	}

	public static class JSONStreamArrayContext<C extends JSONStreamContext> extends JSONStreamContext {

		private final C parent;
		private boolean empty = true;

		public JSONStreamArrayContext(JSONStreamRenderer renderer, C parent) {
			super(renderer);
			this.parent = parent;
			this.renderer.beginArray();
		}

		private void beforeValue() {
			if (!this.empty)
				this.renderer.separator();
			this.empty = false;
		}

		public JSONStreamArrayContext<C> add(Boolean value) {
			this.beforeValue();
			this.renderer.value(value);
			return this;
		}

		public JSONStreamArrayContext<C> add(boolean value) {
			this.beforeValue();
			this.renderer.value(value);
			return this;
		}

		public JSONStreamArrayContext<C> add(String value) {
			this.beforeValue();
			this.renderer.value(value);
			return this;
		}

		public JSONStreamArrayContext<C> add(Number value) {
			this.beforeValue();
			this.renderer.value(value);
			return this;
		}

		public JSONStreamArrayContext<C> add(long value) {
			this.beforeValue();
			this.renderer.value(value);
			return this;
		}

		public JSONStreamArrayContext<C> add(double value) {
			this.beforeValue();
			this.renderer.value(value);
			return this;
		}

		public JSONStreamArrayContext<C> addNull() {
			this.beforeValue();
			this.renderer.nullValue();
			return this;
		}

		public JSONStreamArrayContext<JSONStreamArrayContext<C>> addArray() {
			this.beforeValue();
			return new JSONStreamArrayContext<>(this.renderer, this);
		}

		public JSONStreamObjectContext<JSONStreamArrayContext<C>> addObject() {
			this.beforeValue();
			return new JSONStreamObjectContext<>(this.renderer, this);
		}

		public C end() {
			this.renderer.endArray();
			return this.parent;
		}

	}

	public interface JSONStreamRenderer {

		public void beginObject();
		public void endObject();
		public void beginArray();
		public void endArray();
		public void separator();
		public void name(String name);
		public void value(Boolean value);
		public void value(boolean value);
		public void value(String value);
		public void value(Number value);
		public void value(long value);
		public void value(double value);
		public void nullValue();

	}

	public static class JSONStreamStringRenderer implements JSONStreamRenderer {

		private final Consumer<String> consumer;

		public JSONStreamStringRenderer(Consumer<String> consumer) {
			super();
			this.consumer = consumer;
		}

		@Override public void beginObject()        { this.consumer.accept("{"); }
		@Override public void endObject()          { this.consumer.accept("}"); }
		@Override public void beginArray()         { this.consumer.accept("["); }
		@Override public void endArray()           { this.consumer.accept("]"); }
		@Override public void separator()          { this.consumer.accept(","); }
		@Override public void name(String name)    { this.consumer.accept("\"" + JSON.escape(name) + "\":"); }
		@Override public void value(Boolean value) { this.consumer.accept(value == null ? "null" : value.toString()); }
		@Override public void value(boolean value) { this.consumer.accept(Boolean.toString(value)); }
		@Override public void value(String value)  { this.consumer.accept(value == null ? "null" : ("\"" + JSON.escape(value) + "\"")); }
		@Override public void value(Number value)  { this.consumer.accept(value == null ? "null" : value.toString()); }
		@Override public void value(long value)    { this.consumer.accept(Long.toString(value)); }
		@Override public void value(double value)  { this.consumer.accept(Double.toString(value)); }
		@Override public void nullValue()          { this.consumer.accept("null"); }

	}

	public static class JSONStreamElementRenderer implements JSONStreamRenderer {

		private Stack<JSONElement> parents = new Stack<>();
		private JSONElement result = null;
		private String propertyName = null;

		public JSONStreamElementRenderer() {
			super();
		}

		public JSONElement getResult() {
			return this.result;
		}

		private void digest(JSONElement value) {
			// La première valeur est aussi le résultat
			if (this.result == null)
				this.result = value;
			// Une valeur de propriété ou un élément de tableau est ajouté au parent en haut de la pile
			if (!this.parents.isEmpty()) {
				JSONElement parent = this.parents.peek();
				if (parent.isArray())
					parent.asArray().add(value);
				else if (parent.isObject())
					parent.asObject().set(Objects.requireNonNull(this.propertyName), value);
				else
					throw new IllegalStateException("Parent is " + parent.getClass());
			}
			// Si la valeur est un tableau ou un objet, on l'empile comme parent des futures valeurs
			if (value.isArray() || value.isObject())
				this.parents.push(value);
		}

		@Override public void beginObject()        { this.digest(new JSONObject()); }
		@Override public void endObject()          { this.parents.pop(); }
		@Override public void beginArray()         { this.digest(new JSONArray()); }
		@Override public void endArray()           { this.parents.pop(); }
		@Override public void separator()          { /**/ }
		@Override public void name(String name)    { this.propertyName = name; }
		@Override public void value(Boolean value) { this.digest(JSON.of(value)); }
		@Override public void value(boolean value) { this.digest(JSON.of(value)); }
		@Override public void value(String value)  { this.digest(JSON.of(value)); }
		@Override public void value(Number value)  { this.digest(JSON.of(value)); }
		@Override public void value(long value)    { this.digest(JSON.of(value)); }
		@Override public void value(double value)  { this.digest(JSON.of(value)); }
		@Override public void nullValue()          { this.digest(JSON.ofNull()); }

	}

	public static class JSONStreamGSONRenderer implements JSONStreamRenderer {

		private Stack<JsonElement> parents = new Stack<>();
		private JsonElement result = null;
		private String propertyName = null;

		public JSONStreamGSONRenderer() {
			super();
		}

		public JsonElement getResult() {
			return this.result;
		}

		private void digest(JsonElement value) {
			// La première valeur est aussi le résultat
			if (this.result == null)
				this.result = value;
			// Une valeur de propriété ou un élément de tableau est ajouté au parent en haut de la pile
			if (!this.parents.isEmpty()) {
				JsonElement parent = this.parents.peek();
				if (parent.isJsonArray())
					parent.getAsJsonArray().add(value);
				else if (parent.isJsonObject())
					parent.getAsJsonObject().add(Objects.requireNonNull(this.propertyName), value);
				else
					throw new IllegalStateException("Parent is " + parent.getClass());
			}
			// Si la valeur est un tableau ou un objet, on l'empile comme parent des futures valeurs
			if (value.isJsonArray() || value.isJsonObject())
				this.parents.push(value);
		}

		@Override public void beginObject()        { this.digest(new JsonObject()); }
		@Override public void endObject()          { this.parents.pop(); }
		@Override public void beginArray()         { this.digest(new JsonArray()); }
		@Override public void endArray()           { this.parents.pop(); }
		@Override public void separator()          { /**/ }
		@Override public void name(String name)    { this.propertyName = name; }
		@Override public void value(Boolean value) { this.digest(new JsonPrimitive(value)); }
		@Override public void value(boolean value) { this.digest(new JsonPrimitive(value)); }
		@Override public void value(String value)  { this.digest(new JsonPrimitive(value)); }
		@Override public void value(Number value)  { this.digest(new JsonPrimitive(value)); }
		@Override public void value(long value)    { this.digest(new JsonPrimitive(value)); }
		@Override public void value(double value)  { this.digest(new JsonPrimitive(value)); }
		@Override public void nullValue()          { this.digest(JsonNull.INSTANCE); }

	}

	public static class JSONStreamIndentedRenderer implements JSONStreamRenderer {

		private int depth = 0;
		private boolean indentValue = false;
		private boolean emptyParent = true;
		private final Consumer<String> consumer;
		private final String indent;
		private final Boolean spaceAfterColumn;

		public JSONStreamIndentedRenderer(Consumer<String> consumer, String indent, boolean spaceAfterColumn) {
			super();
			this.consumer = consumer;
			this.indent = indent;
			this.spaceAfterColumn = spaceAfterColumn;
		}

		private void newLine() {
			this.consumer.accept("\n");
		}

		private void indent() {
			for (int i = 0; i < this.depth; i++) {
				this.consumer.accept(this.indent);
			}
		}

		private void open(String value) {
			if (this.indentValue) {
				this.newLine();
				this.indent();
			}
			this.consumer.accept(value);
			this.depth++;
			this.emptyParent = true;
			this.indentValue = true;
		}

		private void close(String value) {
			if (!this.emptyParent)
				this.newLine();
			this.depth--;
			if (!this.emptyParent)
				this.indent();
			this.consumer.accept(value);
			this.emptyParent = false;
			this.indentValue = false;
		}

		private void primitive(String value) {
			if (this.indentValue) {
				this.newLine();
				this.indent();
			}
			this.consumer.accept(value);
			this.emptyParent = false;
			this.indentValue = false;
		}

		private void consume(String value, boolean newLine, boolean dec, boolean indent, boolean inc, boolean newEmptyParent, boolean newIndentValue) {
			if (newLine)
				this.newLine();
			if (dec)
				this.depth--;
			if (indent)
				this.indent();
			this.consumer.accept(value);
			if (inc)
				this.depth++;
			this.emptyParent = newEmptyParent;
			this.indentValue = newIndentValue;
		}

		@Override public void beginObject()        { this.open("{"); }
		@Override public void endObject()          { this.close("}"); }
		@Override public void beginArray()         { this.open("["); }
		@Override public void endArray()           { this.close("]"); }
		@Override public void separator()          { this.consume(",", false, false, false, false, false, true); }
		@Override public void name(String name)    { this.consume("\"" + JSON.escape(name) + "\":" + (this.spaceAfterColumn ? " " : ""), true, false, true, false, false, false); }

		@Override public void value(Boolean value) { this.primitive(value == null ? "null" : value.toString()); }
		@Override public void value(boolean value) { this.primitive(Boolean.toString(value)); }
		@Override public void value(String value)  { this.primitive(value == null ? "null" : ("\"" + JSON.escape(value) + "\"")); }
		@Override public void value(Number value)  { this.primitive(value == null ? "null" : value.toString()); }
		@Override public void value(long value)    { this.primitive(Long.toString(value)); }
		@Override public void value(double value)  { this.primitive(Double.toString(value)); }
		@Override public void nullValue()          { this.primitive("null"); }

	}

}
