package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import fr.techgp.nimbus.server.Router;
import fr.techgp.nimbus.server.WebSocket;

/** This class implements Jetty's {@link WebSocketListener} using one {@link WebSocket} used in {@link Router} */
public class JettyWebSocket implements WebSocketListener {

	private final WebSocket ws;
	private WebSocket.Session session = null;

	public JettyWebSocket(WebSocket ws) {
		super();
		this.ws = ws;
	}

	@Override
	public void onWebSocketConnect(Session session) {
		this.session = new JettyWebSocketSession(session);
		if (this.ws.onConnect() != null) {
			try {
				this.ws.onConnect().connect(this.session);
			} catch (IOException ex) {
				this.onWebSocketError(ex);
			}
		}
	}

	@Override
	public void onWebSocketText(String message) {
		if (this.ws.onText() != null) {
			try {
				this.ws.onText().text(this.session, message);
			} catch (IOException ex) {
				this.onWebSocketError(ex);
			}
		}
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int length) {
		if (this.ws.onBinary() != null) {
			try {
				this.ws.onBinary().binary(this.session, ByteBuffer.wrap(payload, offset, length));
			} catch (IOException ex) {
				this.onWebSocketError(ex);
			}
		}
	}

	@Override
	public void onWebSocketError(Throwable throwable) {
		if (this.ws.onError() != null)
			this.ws.onError().error(this.session, throwable);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		if (this.ws.onClose() != null) {
			try {
				this.ws.onClose().close(this.session, statusCode, reason);
			} catch (IOException ex) {
				this.onWebSocketError(ex);
			}
		}
	}

}
