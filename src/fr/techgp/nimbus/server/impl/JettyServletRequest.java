package fr.techgp.nimbus.server.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.eclipse.jetty.http.MultiPartFormInputStream;

/** This class extends {@link ServletRequest} to optimize uploads by overriding {@link ServletRequest#loadUploads} */
public class JettyServletRequest extends ServletRequest {

	private final MultipartConfigElement multipart;

	public JettyServletRequest(HttpServletRequest request, SessionConfig session, MultipartConfigElement multipart) {
		super(request, session);
		this.multipart = multipart;
	}

	@Override
	protected List<ServletUpload> loadUploads() {
		try {
			this.attribute("org.eclipse.jetty.multipartConfig", this.multipart);
			Collection<Part> parts = this.raw().getParts();
			List<ServletUpload> uploads = new ArrayList<>();
			for (Part part : parts) {
				ServletUpload upload = new ServletUpload(part);
				configureMultiPartWithJettyInternal(upload, part);
				uploads.add(upload);
			}
			return uploads;
		} catch (IOException | ServletException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** This method exposes internal file or bytes from Jetty's {@link MultiPartFormInputStream.MultiPart} implementation to optimize uploads. */
	protected static final boolean configureMultiPartWithJettyInternal(ServletUpload upload, Part part) {
		if (part instanceof MultiPartFormInputStream.MultiPart) {
			MultiPartFormInputStream.MultiPart mpart = (MultiPartFormInputStream.MultiPart) part;
			upload.setFile(mpart.getFile());
			upload.setBytes(mpart.getBytes());
			return true;
		}
		return false;
	}

	/** This method exposes internal file or bytes from Jetty's deprecated MultiPart implementation to optimize uploads. */
	@SuppressWarnings("deprecation")
	protected static final boolean configureMultiPartWithJettyDeprecated(ServletUpload upload, Part part) {
		if (part instanceof org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart) {
			org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart mpart = (org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart) part;
			upload.setFile(mpart.getFile());
			upload.setBytes(mpart.getBytes());
			return true;
		}
		return false;
	}

}
