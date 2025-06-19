package fr.techgp.nimbus.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import fr.techgp.nimbus.server.render.RenderBytes;
import fr.techgp.nimbus.server.render.RenderFile;
import fr.techgp.nimbus.server.render.RenderJSON;
import fr.techgp.nimbus.server.render.RenderRedirect;
import fr.techgp.nimbus.server.render.RenderSamePage;
import fr.techgp.nimbus.server.render.RenderStatic;
import fr.techgp.nimbus.server.render.RenderStatus;
import fr.techgp.nimbus.server.render.RenderString;
import fr.techgp.nimbus.server.render.RenderThrowable;

/**
 * A {@link Render} is used to write the response when routing is over, including all "before" and "after" filters.
 */
@FunctionalInterface
public interface Render {

	/**
	 * renders this instance to the {@link Response} and it's {@link OutputStream}, using specified {@link Charset} for text-encoding.
	 *
	 * @param request the handled request
	 * @param response the response, in it's current state, altered during routing by matching rules
	 * @param charset the {@link Charset} chosen by context for text encoding, to avoid hard-coding this in every {@link Render} implementations
	 * @param stream a supplier for the response {@link OutputStream} to call when the {@link Render} is ready to write the body
	 * @throws IOException if something goes wrong while writing the response
	 */
	public void render(Request request, Response response, Charset charset, Supplier<OutputStream> stream) throws IOException;

	/**
	 * A singleton for an empty body {@link Render}
	 */
	public static Render EMPTY = (request, response, charset, stream) -> { /* */ };

	/**
	 * This class make it possible to throw an Exception anywhere in {@link Route#handle(Request, Response)}
	 * while still providing a proper {@link Render}.
	 *
	 * By default, it is catched in {@link Router#processList(Request, Response, List, boolean)} to set {@link Response#body(Render)}.
	 */
	public static class Exception extends RuntimeException {

		private static final long serialVersionUID = 1L;
		private final Render render;

		public Exception(Render render) {
			super();
			this.render = render;
		}

		public Render get() {
			return this.render;
		}
	}

	/** returns a new {@link Render} that will write this {@link String} */
	public static Render string(String value) {
		return new RenderString(value);
	}

	/** returns a new {@link Render} that will write this byte array */
	public static Render bytes(byte[] value) {
		return new RenderBytes(value);
	}

	/** returns a new {@link Render} that will write this byte array, with optional "Content-Type" and "Content-Disposition" headers */
	public static Render bytes(byte[] value, String mimeType, String fileName, boolean download) {
		return new RenderBytes(value, mimeType, fileName, download);
	}

	/** returns a new {@link Render} that will redirect the request to another URL */
	public static Render redirect(String url) {
		return new RenderRedirect(url);
	}

	/** returns a new {@link Render} that will use the "Referer" header to redraw the same page */
	public static Render samePage() {
		return new RenderSamePage();
	}

	/** returns a new {@link Render} that will write this file, with proper caching using the "Etag" and "Last-Modified" headers */
	public static Render staticFile(File file) {
		return new RenderStatic(file, MimeTypes.byName(file.getName()));
	}

	/** returns a new {@link Render} that will write this file, with proper caching using the "Etag" and "Last-Modified" headers */
	public static Render staticFile(File file, String mimeType) {
		return new RenderStatic(file, mimeType);
	}

	/** returns a new {@link Render} that will write this file */
	public static Render file(File file) {
		return new RenderFile(file);
	}

	/** returns a new {@link Render} that will write this file, with optional "Content-Type" and "Content-Disposition" headers and ability to delete temp file after */
	public static Render file(File file, String mimeType, String fileName, boolean download, boolean deleteAfter) {
		return new RenderFile(file, mimeType, fileName, download, deleteAfter);
	}

	/** returns a new {@link Render} that will send an "Internal Server Error" status code and the "throwable" stacktrace as response body */
	public static Render throwable(java.lang.Throwable throwable) {
		return new RenderThrowable(throwable);
	}

	/** returns a new {@link Render} that will send the specified status code, and optional "body" {@link String} */
	public static Render status(int code, String body) {
		return new RenderStatus(code, body);
	}

	/** wrapper for the 200 response with "OK" body */
	public static Render ok() {
		return new RenderStatus(HttpServletResponse.SC_OK, "OK"); // 200
	}

	/** wrapper for the 304 response with no body */
	public static Render notModified() {
		return new RenderStatus(HttpServletResponse.SC_NOT_MODIFIED, ""); // 304
	}

	/** wrapper for the 400 response with "Bad Request" body */
	public static Render badRequest() {
		return new RenderStatus(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"); // 400
	}

	/** wrapper for the 401 response with "Unauthorized" body */
	public static Render unauthorized() {
		return new RenderStatus(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"); // 401
	}

	/** wrapper for the 403 response with "Forbidden" body */
	public static Render forbidden() {
		return new RenderStatus(HttpServletResponse.SC_FORBIDDEN, "Forbidden"); // 403
	}

	/** wrapper for the 404 response with "Not Found" body */
	public static Render notFound() {
		return new RenderStatus(HttpServletResponse.SC_NOT_FOUND, "Not Found"); // 404
	}

	/** wrapper for the 409 response with "Conflict" body */
	public static Render conflict() {
		return new RenderStatus(HttpServletResponse.SC_CONFLICT, "Conflict"); // 409
	}

	/** wrapper for the 500 response with "Internal Server Error" body */
	public static Render internalServerError() {
		return new RenderStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"); // 500
	}

	/** wrapper for the 501 response with "Not Implemented" body */
	public static Render notImplemented() {
		return new RenderStatus(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not Implemented"); // 501
	}

	/** wrapper for the 507 response with "Insufficient Storage" body */
	public static Render insufficientStorage() {
		return new RenderStatus(507, "Insufficient Storage"); // Détourné de WEBDAV : https://tools.ietf.org/html/rfc4918#section-11.5
	}

	/** returns a new {@link Render} that will use the specified {@link JsonElement} as the JSON response body */
	public static Render json(JsonElement object) {
		return new RenderJSON(object);
	}

	/** returns a new {@link Render} that will transform the specified "objects" to a {@link JsonArray} as the JSON response body */
	public static <T> Render json(List<T> objects, Function<T, JsonElement> transformer) {
		return new RenderJSON(objects, transformer);
	}

}
