package fr.techgp.nimbus.server;

/**
 * <p>The {@link Route} gets the {@link Request}, may alter the {@link Response} and should provide a
 * {@link Render}, if the request has been handled.</p>
 *
 * <p>If the {@link Route} decides to handle the request, it has to provide the {@link Render} that will be
 * called later when the response is actually rendered :</p>
 *
 * <ul>
 * <li>it can be specified by calling {@link Response#body(Render)} and returning null later</li>
 * <li>it can be specified by returning the {@link Render} (more convenient in most cases)</li>
 * <li>it can be specified by throwing a {@link Render.Exception}</li>
 * </ul>
 *
 * <p>If no {@link Render} is specified, the {@link Router} will look for another {@link Route} handler whose
 * {@link Matcher} accepts the {@link Request}.</p>
 *
 * @param request exposes all information from client's request (method, type, path, parameters, headers, ...)
 * @param response describes the future response (status, type, headers, cookies, body, ...)
 * @return the response's body, or null to drop the request or after a calle to {@link Response#body(Render)}
 * @throws Exception if something went wrong or to specify response's body using {@link Render.Exception}
 */
@FunctionalInterface
public interface Route {

	/** @see Route */
	public Render handle(Request request, Response response) throws Exception;

}
