package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/** This class extends {@link ServletRequest} to optimize uploads by overriding {@link ServletRequest#loadUploads} */
public class JettyServletRequest extends ServletRequest {

	private final MultipartConfigElement multipartConfig;

	public JettyServletRequest(HttpServletRequest request, SessionConfig session, MultipartConfigElement multipartConfig) {
		super(request, session);
		this.multipartConfig = multipartConfig;
	}

	@Override
	protected List<ServletUpload> loadUploads() {
		try {
			this.attribute("org.eclipse.jetty.multipartConfig", this.multipartConfig);
			Collection<Part> parts = this.raw().getParts();
			List<ServletUpload> uploads = new ArrayList<>();
			for (Part part : parts) {
				uploads.add(new JettyOptimizedUpload(part));
			}
			return uploads;
		} catch (IOException | ServletException ex) {
			throw new RuntimeException(ex);
		}
	}

}
