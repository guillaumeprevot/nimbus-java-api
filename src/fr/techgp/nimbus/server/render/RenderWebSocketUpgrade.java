package fr.techgp.nimbus.server.render;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Request;
import fr.techgp.nimbus.server.Response;

public class RenderWebSocketUpgrade implements Render {

	@Override
	public void render(Request request, Response response, Charset charset, Supplier<OutputStream> stream)
			throws IOException {
		// this class is just a marker interface and the real response is set by JettyRouterHandler
	}

}
