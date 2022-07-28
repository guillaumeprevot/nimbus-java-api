package fr.techgp.nimbus.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import fr.techgp.nimbus.utils.StringUtils;

/**
 * This class implements the WebSocket behaviour using composition.
 * <br />The expected usage is :
 * <ul>
 * <li>to use either {@link OnText}, {@link OnBinary} or both to implement communication
 * <li>to use {@link OnConnect} and {@link OnClose} when session tracking is needed
 * <li>to use {@link OnError} for error handling
 * </ul>
 * All theses interfaces are {@link FunctionalInterface} to allow fluent code pattern :
 * <pre>
 * router.websocket("/ws/example", new WebSocket()
 * 	.onConnect((session) -> System.out.println("WebSocket connected " + session))
 * 	.onText((session, message) -> session.sendText("ping".equals(message) ? "pong" : message))
 * 	.onError((session, throwable) -> throwable.printStackTrace())
 * 	.onClose((session, statusCode, reason) -> System.out.println("WebSocket closed " + session)));
 * </pre>
 * A few helper methods are available for very simple WebSocket :
 * <pre>
 * router.websocket("/ws/echo", WebSocket.text((session, message) -> "ping".equals(message) ? "pong" : message));
 * </pre>
 */
public class WebSocket {

	private OnText text;
	private OnBinary binary;
	private OnConnect connect;
	private OnError error;
	private OnClose close;

	public WebSocket() {
		super();
	}

	public OnText onText() {
		return this.text;
	}

	public WebSocket onText(OnText text) {
		this.text = text;
		return this;
	}

	public OnBinary onBinary() {
		return this.binary;
	}

	public WebSocket onBinary(OnBinary binary) {
		this.binary = binary;
		return this;
	}

	public OnConnect onConnect() {
		return this.connect;
	}

	public WebSocket onConnect(OnConnect connect) {
		this.connect = connect;
		return this;
	}

	public OnError onError() {
		return this.error;
	}

	public WebSocket onError(OnError error) {
		this.error = error;
		return this;
	}

	public OnClose onClose() {
		return this.close;
	}

	public WebSocket onClose(OnClose close) {
		this.close = close;
		return this;
	}

	/** This {@link FunctionalInterface} implementation is required to deal with text message */
	@FunctionalInterface
	public static interface OnText {
		public void text(Session session, String message) throws IOException;
	}

	/** This {@link FunctionalInterface} implementation is required to deal with binary message */
	@FunctionalInterface
	public static interface OnBinary {
		public void binary(Session session, ByteBuffer message) throws IOException;
	}

	/** This {@link FunctionalInterface} is called when the WebSocket is connected */
	@FunctionalInterface
	public static interface OnConnect {
		public void connect(Session session) throws IOException;
	}

	/** This {@link FunctionalInterface} is called when an error occurred with the WebSocket */
	@FunctionalInterface
	public static interface OnError {
		public void error(Session session, Throwable throwable);
	}

	/** This {@link FunctionalInterface} is called when the WebSocket is closed */
	@FunctionalInterface
	public static interface OnClose {
		public void close(Session session, int statusCode, String reason) throws IOException;
	}

	/** This method creates a WebSocket whose main purpose is to exchange text message */
	public static WebSocket text(BiFunction<Session, String, String> handler) {
		WebSocket ws = new WebSocket();
		ws.onText((session, message) -> {
			String answer = handler.apply(session, message);
			if (answer != null)
				session.sendText(answer);
		});
		return ws;
	}

	/** This method creates a WebSocket whose main purpose is to exchange JSON message */
	public static WebSocket json(BiFunction<Session, JsonElement, JsonElement> handler) {
		WebSocket ws = new WebSocket();
		ws.onText((session, message) -> {
			JsonElement input = StringUtils.isBlank(message) ? null : JsonParser.parseString(message);
			JsonElement output = handler.apply(session, input);
			if (output != null)
				session.sendText(output.toString());
		});
		return ws;
	}

	/** This method creates a WebSocket whose main purpose is to exchange binary message */
	public static WebSocket binary(BiFunction<Session, ByteBuffer, ByteBuffer> handler) {
		WebSocket ws = new WebSocket();
		ws.onBinary((session, message) -> {
			ByteBuffer answer = handler.apply(session, message);
			if (answer != null)
				session.sendBinary(answer);
		});
		return ws;
	}

	/** This interface represents a WebSocket session */
	public static interface Session {

		public Duration idleTimeout();
		public void idleTimeout(Duration timeout);

		public boolean opened();
		public void close(int statusCode, String reason);

		public void sendText(String text) throws IOException;
		public void sendTextAsync(String text, Runnable success, Consumer<Throwable> error);

		public void sendBinary(ByteBuffer binary) throws IOException;
		public void sendBinaryAsync(ByteBuffer binary, Runnable success, Consumer<Throwable> error);

	}

}
