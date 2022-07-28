package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Consumer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;

import fr.techgp.nimbus.server.WebSocket;

/** This class implements the router WebSocket session using a Jetty's WebSocket {@link Session} */
public class JettyWebSocketSession implements WebSocket.Session {

	private final Session session;

	public JettyWebSocketSession(Session session) {
		super();
		this.session = session;
	}

	@Override
	public Duration idleTimeout() {
		return this.session.getIdleTimeout();
	}

	@Override
	public void idleTimeout(Duration timeout) {
		this.session.setIdleTimeout(timeout);
	}

	@Override
	public boolean opened() {
		return this.session.isOpen();
	}

	@Override
	public void close(int statusCode, String reason) {
		this.session.close(statusCode, reason);
	}

	@Override
	public void sendText(String text) throws IOException {
		this.session.getRemote().sendString(text);
	}

	@Override
	public void sendTextAsync(String text, Runnable success, Consumer<Throwable> error) {
		this.session.getRemote().sendString(text, new WriteCallback() {

			@Override
			public void writeSuccess() {
				success.run();
			}

			@Override
			public void writeFailed(Throwable t) {
				error.accept(t);
			}
		});
	}

	@Override
	public void sendBinary(ByteBuffer binary) throws IOException {
		this.session.getRemote().sendBytes(binary);
	}

	@Override
	public void sendBinaryAsync(ByteBuffer binary, Runnable success, Consumer<Throwable> error) {
		this.session.getRemote().sendBytes(binary, new WriteCallback() {

			@Override
			public void writeSuccess() {
				success.run();
			}

			@Override
			public void writeFailed(Throwable t) {
				error.accept(t);
			}
		});
	}

}
