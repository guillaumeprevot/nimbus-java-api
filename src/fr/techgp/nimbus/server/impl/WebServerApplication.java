package fr.techgp.nimbus.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.JsonObject;

import fr.techgp.nimbus.server.Render;
import fr.techgp.nimbus.server.Request;
import fr.techgp.nimbus.server.Response;
import fr.techgp.nimbus.server.Route;
import fr.techgp.nimbus.server.Router;
import fr.techgp.nimbus.server.Utils;

public class WebServerApplication {

	private static final String pidPath = System.getProperty("webserver.pid", "webserver.pid");
	private static final String logPath = System.getProperty("webserver.log", "webserver.log");
	private static final String confPath = System.getProperty("webserver.conf", "webserver.conf");
	private static final Logger logger = prepareLogger();

	private static final Logger prepareLogger() {
		if (!"none".equals(logPath))
			System.setProperty("org.slf4j.simpleLogger.logFile", logPath);
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
		System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "dd/MM/yyyy HH:mm:ss");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
		System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
		System.setProperty("org.slf4j.simpleLogger.log.org.eclipse.jetty", "warn");
		return LoggerFactory.getLogger(WebServerApplication.class);
	}

	public static final void main(String[] args) {
		try {
			// Log start
			if (logger.isInfoEnabled())
				logger.info("Starting application...");

			// Load configuration files(s)
			Properties properties = new Properties();
			String[] pathParts = confPath.split(File.pathSeparator);
			for (String pathPart : pathParts) {
				File configFile = new File(pathPart);
				if (configFile.exists()) {
					try (FileInputStream fis = new FileInputStream(configFile)) {
						properties.load(fis);
					}
				}
			}

			// Prepare function to access configuration
			BiFunction<String, String, String> settings = (name, defaultValue) -> {
				String s = System.getProperty(name);
				return (s != null) ? s : properties.getProperty(name, defaultValue);
			};

			// Port
			int port = Integer.parseInt(settings.apply("server.port", "10001"));
			JettyServer server = new JettyServer(port);

			// Optional HTTPS
			String keystore = settings.apply("server.keystore", null);
			if (keystore != null)
				server.https(keystore, settings.apply("server.keystore.password", null));

			// Routes
			Router router = new Router();
			int i = 0;
			String folder = settings.apply("static." + i + ".folder", null);
			String prefix = settings.apply("static." + i + ".prefix", "");
			while (folder != null) {
				router.get(prefix + "/*", new StaticRessourceWithCache(prefix, folder));
				i++;
				folder = settings.apply("static." + i + ".folder", null);
				prefix = settings.apply("static." + i + ".prefix", "");
			}

			// Some predefined features
			if ("true".equals(settings.apply("utils.ping.enabled", null)))
				router.get("/utils/ping", (req, resp) -> Render.string("pong"));
			if ("true".equals(settings.apply("utils.ip.enabled", null)))
				router.get("/utils/ip", (req, resp) -> Render.string(Utils.extractIPWithProxy(req)));
			if ("true".equals(settings.apply("utils.mimetype.enabled", null)))
				router.get("/utils/mimetype/:extension", new MimeType());
			if ("true".equals(settings.apply("utils.moneyrates.enabled", null)))
				router.get("/utils/moneyrates", new MoneyRates(settings));
			if ("true".equals(settings.apply("utils.iblocklist.enabled", null)))
				router.get("/utils/iblocklist", new IBlockList(settings));

			// Check that requested path are not insecure
			router.before("/*", (req, res) -> {
				if (req.path().contains("..")) {
					if (logger.isWarnEnabled())
						logger.warn("[" + req.ip() + "] " + HttpServletResponse.SC_FORBIDDEN + " : " + req.path());
					return Render.forbidden();
				}
				return null;
			});

			// Trace for requested URLs that do not exist in shared folders
			router.after("/*", (req, res) -> {
				if (res.body() == null && logger.isWarnEnabled())
					logger.warn("[" + req.ip() + "] " + HttpServletResponse.SC_NOT_FOUND + " : " + req.path());
				else if (logger.isTraceEnabled())
					logger.trace("[" + req.ip() + "] " + (res.body() == null ? HttpServletResponse.SC_NOT_FOUND : res.status()) + " : " + req.path());
				return null;
			});

			// Prepare "pid" file
			// String pid = new File("/proc/self").getCanonicalFile().getName(); // (linux only, all jvm)
			String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			try (FileOutputStream os = new FileOutputStream(new File(pidPath))) {
				os.write(pid.getBytes());
			}

			// Log started
			server.start(router);
			if (logger.isInfoEnabled())
				logger.info("Application started on " + (keystore != null ? "HTTPS" : "HTTP") + " port " + port + " with PID " + pid);
		} catch (Exception ex) {
			// Log fatal error
			if (logger.isErrorEnabled())
				logger.error("Application stopped because of unexpected error.", ex);
		}
	}

	private static final class MimeType implements Route {

		@Override
		public Render handle(Request request, Response response) {
			// File extension
			String extension = request.pathParameter(":extension");
			// Associated MIME type
			String mimetype = MimeTypes.getDefaultMimeByExtension("file." + extension);
			if (mimetype == null)
				return Render.notFound();
			return Render.string(mimetype);
		}

	}

	private static final class MoneyRates implements Route {

		private final String url;
		private final long refreshInterval;
		private long refreshTime = 0;
		private JsonObject result = null;

		public MoneyRates(BiFunction<String, String, String> settings) {
			super();
			this.url = settings.apply("utils.moneyrates.url", "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
			this.refreshInterval = Integer.parseInt(settings.apply("utils.moneyrates.interval", "1")) * 24 * 60 * 60 * 1000;
		}

		@Override
		public Render handle(Request request, Response response) throws Exception {
			try {
				// Attendre une journée entre chaque raffraichissement
				if (this.result == null || (System.currentTimeMillis() - this.refreshTime) > this.refreshInterval) {
					if (logger.isInfoEnabled())
						logger.info("[moneyrates] Refreshing...");
					// http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
					// http://www.ecb.europa.eu/stats/exchange/eurofxref/html/index.en.html
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(this.url);
					NodeList nodes = doc.getElementsByTagName("Cube");
					this.result = new JsonObject();
					this.refreshTime = System.currentTimeMillis();
					for (int i = 0; i < nodes.getLength(); i++) {
						Node node = nodes.item(i);
						Node currency = node.getAttributes().getNamedItem("currency");
						Node rate = node.getAttributes().getNamedItem("rate");
						if (currency != null)
							this.result.addProperty(currency.getNodeValue(), Double.valueOf(rate.getNodeValue()));
					}
					if (logger.isInfoEnabled())
						logger.info("[moneyrates] Refresh completed.");
				}

				// Renvoyer le résultat au format JSON
				response.header("X-DISCLAIMER", "Test API. Use at your own risk");
				response.header("Access-Control-Allow-Origin", "*");
				response.header("Content-Disposition", "inline; filename=\"moneyrates.json\"");
				return Render.json(this.result);

			} catch (Exception ex) {
				// Annuler
				this.result = null;
				this.refreshTime = 0;
				// Tracer
				if (logger.isErrorEnabled())
					logger.error("[moneyrates] Unexpected error while refreshing.", ex);
				// Retour
				return Render.internalServerError();
			}
		}
	}

	private static final class IBlockList implements Route {

		private final List<String> urls;
		private final File file;
		private final long refreshInterval;
		private long refreshTime = 0;

		public IBlockList(BiFunction<String, String, String> settings) {
			super();
			this.urls = new ArrayList<>();
			this.file = new File(settings.apply("utils.iblocklist.file", "iblocklist.txt.gz"));
			this.refreshInterval = Integer.parseInt(settings.apply("utils.iblocklist.interval", "1")) * 24 * 60 * 60 * 1000;
			int index = 0;
			while (settings.apply("utils.iblocklist." + index, null) != null) {
				this.urls.add(settings.apply("utils.iblocklist." + index, null));
				index++;
			}
		}

		@Override
		public Render handle(Request request, Response response) throws Exception {
			try {
				// Attendre une journée entre chaque raffraichissement
				if ((System.currentTimeMillis() - this.refreshTime) > this.refreshInterval) {
					if (logger.isInfoEnabled())
						logger.info("[iblocklist] Refreshing " + this.file.getAbsolutePath() + "...");
					this.file.delete();
					try (OutputStream os = new GZIPOutputStream(new FileOutputStream(this.file, false))) {
						for (String url : this.urls) {
							if (logger.isInfoEnabled())
								logger.info("[iblocklist] Adding " + url + "...");
							try (InputStream is = new GZIPInputStream(new URL(url).openStream())) {
								Utils.copy(is, os);
							}
						}
						this.refreshTime = System.currentTimeMillis();
					}
					if (logger.isInfoEnabled())
						logger.info("[iblocklist] Refresh completed.");
				}

				// Renvoyer le résultat du fichier concaténé
				return Render.file(this.file, "application/x-gzip", this.file.getName(), false, false);

			} catch (IOException ex) {
				// Annuler
				this.file.delete();
				this.refreshTime = 0;
				// Tracer
				if (logger.isErrorEnabled())
					logger.error("[iblocklist] Unexpected error while refreshing.", ex);
				// Retour
				return Render.internalServerError();
			}
		}
	}

	private static final class StaticRessourceWithCache implements Route {

		private final String prefix;
		private final File folder;

		public StaticRessourceWithCache(String prefix, String folder) {
			this.prefix = prefix;
			this.folder = new File(folder);
		}

		@Override
		public Render handle(Request request, Response response) throws Exception {
			// Récupérer le chemin demandé
			String path = request.path();
			try {
				// Récupérer le fichier associé
				File file = new File(this.folder, path.substring(this.prefix.length()));

				// Vérifier que le fichier existe et passer à la Route suivante sinon
				if (!file.exists())
					return null;

				// Renvoyer le fichier avec le bon type MIME et en fonction du cache
				String mimetype = MimeTypes.getDefaultMimeByExtension(path);
				return Render.staticFile(file, mimetype);

			} catch (Exception ex) {
				if (logger.isErrorEnabled())
					logger.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR + " : " + path, ex);
				return Render.internalServerError();
			}
		}
	}

}
