package fr.techgp.nimbus.server.test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;

import fr.techgp.nimbus.server.Cookie;
import fr.techgp.nimbus.server.MimeTypes;
import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Request;
import fr.techgp.nimbus.server.Response;
import fr.techgp.nimbus.server.Router;
import fr.techgp.nimbus.server.Session;
import fr.techgp.nimbus.server.Session.ClientSession;
import fr.techgp.nimbus.server.Session.ServerSession;
import fr.techgp.nimbus.server.Upload;
import fr.techgp.nimbus.server.impl.JettyServer;
import fr.techgp.nimbus.server.impl.MethodRoute;
import fr.techgp.nimbus.utils.FunctionalUtils.ConsumerWithException;
import fr.techgp.nimbus.utils.IOUtils;
import fr.techgp.nimbus.utils.WebUtils.MultiPartAdapter;

public class Test {

	private static final int PORT = 8080;
	private static String cookieLine = null;

	private final String request;
	private String method = "GET";
	private ConsumerWithException<HttpURLConnection, Exception> customizer;
	private int status = 200;
	private String mimetype = "text/html;charset=utf-8";
	private int length = -1;
	private String body = null;
	private boolean before1 = true;
	private boolean before2 = false;
	private boolean after1 = true;
	private Map<String, String> headers = new HashMap<>();
	private boolean sendCookie = false;
	private boolean saveCookie = false;

	public Test(String request) {
		this.request = request;
	}

	public Test method(String method) { this.method = method; return this; }
	public Test customize(ConsumerWithException<HttpURLConnection, Exception> customizer) { this.customizer = customizer; return this; }
	public Test status(int status) { this.status = status; return this; }
	public Test mimetype(String mimetype) { this.mimetype = mimetype; return this; }
	public Test length(int length) { this.length = length; return this; }
	public Test body(String body) { this.body = body; return this; }
	public Test filters(boolean before1, boolean before2, boolean after1) { this.before1 = before1; this.before2 = before2; this.after1 = after1; return this; }
	public Test header(String header, String value) { this.headers.put(header, value); return this; }
	public Test cookie(boolean send, boolean save) { this.sendCookie = send; this.saveCookie = save; return this; }

	public void run() throws Exception {
		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection connection = (HttpURLConnection) new URL("http", "localhost", PORT, this.request).openConnection();
		if (this.sendCookie)
			connection.setRequestProperty("Cookie", Test.cookieLine);
		connection.setRequestMethod(this.method);
		if (this.customizer != null)
			this.customizer.accept(connection);
		connection.connect();
		if (this.status != -1 && this.status != connection.getResponseCode())
			throw new Exception("Mauvais statut " + connection.getResponseCode());
		if (this.mimetype != null && !this.mimetype.equals(connection.getContentType()))
			throw new Exception("Mauvais type " + connection.getContentType());
		if (this.length != -1 && this.length != connection.getContentLength())
			throw new Exception("Mauvais longueur " + connection.getContentLength());
		if (this.status < 400 && this.body != null) {
			try (InputStream is = connection.getInputStream()) {
				if (!this.body.equals(IOUtils.toUTF8String(is)))
					throw new Exception("Mauvais contenu " + connection.getContent());
			}
		}
		if (this.before1 && !"Before1".equals(connection.getHeaderField("Before1")))
			throw new Exception("Mauvais Before1 " + connection.getHeaderField("Before1"));
		if (this.before2 && !"Before2".equals(connection.getHeaderField("Before2")))
			throw new Exception("Mauvais Before2 " + connection.getHeaderField("Before2"));
		if (!this.before2 && connection.getHeaderField("Before2") != null)
			throw new Exception("Mauvais Before2 car présent");
		if (this.after1 && !"After1".equals(connection.getHeaderField("After1")))
			throw new Exception("Mauvais After1 " + connection.getHeaderField("After1"));
		if (!this.headers.isEmpty()) {
			for (Map.Entry<String, String> header : this.headers.entrySet()) {
				if (!header.getValue().equals(connection.getHeaderField(header.getKey())))
					throw new Exception("Mauvais header " + connection.getHeaderField(header.getKey()));
			}
		}
		if (this.saveCookie) {
			Test.cookieLine = connection.getHeaderField("Set-Cookie");
			if (Test.cookieLine == null)
				throw new Exception("Cookie manquant");
		}
	}

	public static Test get(String request) { return new Test(request).method("GET"); }
	public static Test post(String request) { return new Test(request).method("POST"); }
	public static void assertThat(boolean test) { if (!test) throw new AssertionError(); }

	public static Render reflect(Request request, Response response, Upload upload, Upload[] uploads,
			Cookie cookie, Cookie[] cookies,
			ServerSession session, Optional<ServerSession> optionalSession,
			ClientSession clientSession, Optional<ClientSession> optionalClientSession,
			String stringValue, Integer integerValue, int intValue, Optional<Integer> optionalInteger,
			Integer[] integerValues, int[] intValues, List<Integer> collection, EnumTest enumValue) {
		assertThat(request != null);
		assertThat(response != null);
		assertThat(upload == null);
		assertThat(uploads.length == 0);
		assertThat(cookie == null);
		assertThat(cookies.length == 0);
		assertThat(session != null);
		assertThat(optionalSession != null && optionalSession.isPresent());
		assertThat(clientSession != null);
		assertThat(optionalClientSession != null && optionalClientSession.isPresent());
		assertThat("abc".equals(stringValue));
		assertThat(null == integerValue);
		assertThat(42 == intValue);
		assertThat(optionalInteger != null && optionalInteger.isEmpty());
		assertThat(integerValues != null && integerValues.length == 3 && integerValues[0] == 1 && integerValues[1] == null && integerValues[2] == 2);
		assertThat(intValues != null && intValues.length == 2 && intValues[0] == 1 && intValues[1] == 2);
		assertThat(collection != null && collection.size() == 3 && collection.contains(1) && collection.contains(null) && collection.contains(2));
		assertThat(EnumTest.Something.equals(enumValue));
		return Render.string("OK");
	}

	public static enum EnumTest {
		Something,
		Anything,
		Nothing
	}

	public static class ReflectTest {
		public Render run(String stringValue) {
			return Render.string(stringValue);
		}
	}

	public static void main(String[] args) {
		try {
			Router r = new Router();

			// r.before("/*", (req, res) -> { System.out.println(((ServletRequest) req).raw().getParameterMap()); return null; });
			r.before("/*", (req, res) -> { res.header("Before1", "Before1"); return null; });
			r.before("/hello", (req, res) -> { res.header("Before2", "Before2"); return null; });

			r.get("/error", (req, res) -> { throw new RuntimeException("error"); });
			r.get("/empty", (req, res) -> Render.EMPTY);
			r.get("/hello", (req, res) -> Render.string("world"));
			r.get("/bytes", (req, res) -> Render.bytes("bytes".getBytes(StandardCharsets.UTF_8), "application/octet-stream", "data.bin", false));
			r.route("/anymethod", (req, res) -> Render.string("OK"));
			r.post("/json", (req, res) -> {
				JsonObject o = new JsonObject();
				o.addProperty("name", req.queryParameter("name"));
				o.addProperty("id", req.queryParameterInteger("id", null));
				return Render.json(o);
			});
			r.get("/redirect", (req, res) -> Render.redirect("/hello"));
			r.redirect("/redirect2", "/redirect");
			r.get("/samepage", (req, res) -> Render.samePage());
			r.post("/upload", (req, res) -> {
				String name = req.upload("name").asString();
				String content = req.upload("file").asString();
				return Render.string(name + "/" + req.uploads("file").get(0).fileName() + "/" + content);
			});
			r.get("/reflect", MethodRoute.to(Test.class, "reflect"));
			r.get("/reflect2", MethodRoute.to("fr.techgp.nimbus.server.test.Test.reflect"));
			r.get("/reflect3", MethodRoute.to(new ReflectTest(), "run"));
			r.get("/session", (req, res) -> {
				try {
					boolean client = req.queryParameterBoolean("client", true);
					Session currentSession = client ? req.clientSession(false) : req.session(false);
					String currentValue = currentSession == null ? null : currentSession.attribute("value");
					Session updatedSession = client ? req.clientSession() : req.session();
					updatedSession.attribute("value", req.queryParameter("value"));
					return Render.string(currentValue == null ? "" : currentValue);
				} catch (RuntimeException ex) {
					ex.printStackTrace();
					throw ex;
				}
			});

			r.after("/*", (req, res) -> { res.header("After1", "After1"); return null; });

			JettyServer s = new JettyServer(PORT);
			s.multipart(null/* or System.getProperty("java.io.tmpdir")*/, Integer.MAX_VALUE, Long.MAX_VALUE, 10);
			s.session(2, null, null, "ce26b4bb1dc61766fbe866eb5550ab81cc8f48e81dd9a73b98cacb2c66c3e3c0");
			s.start(r);

			try {
				runAllTests();
				System.out.println("OK");
			} finally {
				s.stop();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static final void runAllTests() throws Exception {
		// No matching routes should return 404 Not Found
		get("/notfound").status(404).run();
		// Server side error should return 500 Internal Server Error with stack trace as text/plain and skipping "after" filters
		get("/error").status(500).mimetype(MimeTypes.TEXT).filters(true, false, false).run();
		// Matching empty route return an empty body
		get("/empty").body("").run();
		// Calling "hello" should return the 5-bytes "world" text and should match all three filters
		get("/hello").length(5).body("world").filters(true, true, true).run();
		// Calling valid path "/bytes" but with wrong method should return 404 Not Found
		post("/bytes").status(404).length("Not Found".length()).run();
		// Calling valid path "/bytes" with valid method should return "bytes" as application/octet-stream and inline file attachment
		get("/bytes").body("bytes").mimetype(MimeTypes.BINARY).header("Content-Disposition", "inline; filename=\"data.bin\"").run();
		// Calling a route without method restriction should be "OK" with "PUT" method
		new Test("/anymethod").method("PUT").body("OK").run();
		// Checking JSON response
		post("/json?name=aaa&id=12").mimetype(MimeTypes.JSON).body("{\"name\":\"aaa\",\"id\":12}").run();
		// Checking redirection from "/redirect" to "/world"
		get("/redirect").length(5).body("world").filters(true, true, true).run();
		// Checking helper method Router.redirect
		get("/redirect2").length(5).body("world").filters(true, true, true).run();
		// Checking SamePage, using a Referer simulating a current "/hello" page
		get("/samepage").customize(c -> c.addRequestProperty("Referer", "/hello")).length(5).body("world").filters(true, true, true).run();
		// Checking SamePage, using a Referer simulating a current "/bytes" page
		get("/samepage").customize(c -> c.addRequestProperty("Referer", "/bytes")).length(5).body("bytes").mimetype(MimeTypes.BINARY).filters(true, false, true).run();

		// Cheking uploads
		post("/upload").customize(c -> {
			try (MultiPartAdapter adapter = new MultiPartAdapter(c, "******")) {
				adapter.addFormField("name", "toto");
				adapter.addFileUpload("file", "tata.txt", "tutu".getBytes(StandardCharsets.UTF_8));
			}
		}).length(18).body("toto/tata.txt/tutu").run();

		// Checking custom route using Java reflection to inject parameters
		String p = "intValue=42&stringValue=abc&integerValues=1&integerValues=&integerValues=2&intValues=1&intValues=2"
				+ "&collection=1&collection=&collection=2&enumValue=Something";
		get("/reflect?" + p).length(2).body("OK").run();
		get("/reflect2?" + p).length(2).body("OK").run();
		get("/reflect3?" + p).length(3).body("abc").run();

		// Check client session
		get("/session?value=toto").cookie(false, true).length(0).run(); // new cookie, nothing in session, store toto
		get("/session?value=titi").cookie(true, true).length(4).body("toto").run(); // send cookie, get toto, store titi
		get("/session?value=tutu").cookie(true, true).length(4).body("titi").run(); // send cookie, get titi, store tutu
		get("/session?value=tata").cookie(false, true).length(0).run(); // don't send cookie, nothing in session, store tata
		get("/session?value=tata").cookie(true, true).length(4).body("tata").run(); // send cookie, get tata, store tata
		Thread.sleep(3000); // wait for session timeout
		get("/session?value=tata").cookie(true, true).length(0).run(); // send cookie, expired session, no result

		// Check server session
		get("/session?client=false&value=toto").cookie(false, true).length(0).run(); // new cookie, nothing in session, store toto
		get("/session?client=false&value=titi").cookie(true, false).length(4).body("toto").run(); // send cookie, get toto, store titi
		get("/session?client=false&value=tutu").cookie(true, false).length(4).body("titi").run(); // send cookie, get titi, store tutu
		get("/session?client=false&value=tata").cookie(false, true).length(0).run(); // don't send cookie, nothing in session, store tata
		get("/session?client=false&value=tata").cookie(true, false).length(4).body("tata").run(); // send cookie, get tata, store tata
		Thread.sleep(3000); // wait for session timeout
		get("/session?client=false&value=tata").cookie(true, false).length(0).run(); // send cookie, expired session, no result

		// to continue...
	}
}