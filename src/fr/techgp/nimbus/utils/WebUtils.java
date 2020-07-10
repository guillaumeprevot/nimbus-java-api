package fr.techgp.nimbus.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class WebUtils {

	private WebUtils() {
		//
	}

	/**
	 * Cette méthode ouvre l'URL donnée en GET, en autorisant les redirections et avec "Nimbus" comme "User-Agent"
	 *
	 * @param url l'URL à ouvrir
	 * @return la {@link HttpURLConnection} pointant sur l'URL demandée
	 * @throws IOException
	 */
	public static final HttpURLConnection openURL(final String url) throws IOException {
		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.addRequestProperty("User-Agent", "Nimbus");
		connection.setRequestMethod("GET");
		connection.connect();
		return connection;
	}

	/**
	 * Cette méthode renvoie la chaine de caractères UTF-8 contenue à l'URL donnée.
	 *
	 * @param url l'URL à charger en utilisant {@link WebUtils#openURL(String)}
	 * @return la chaine de caractères UTF-8 contenue à l'URL donnée
	 */
	public static final String downloadURL(final String url) {
		try {
			HttpURLConnection connection = openURL(url);
			try (InputStream stream = connection.getInputStream()) {
				return IOUtils.toStringUTF8(stream);
			}
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * Cette méthode renvoie le contenu de l'URL donnée au format "data:...", en utilisant le type retourné, ou "defaultMimetype" par défaut.
	 *
	 * @param url l'URL à charger en utilisant {@link WebUtils#openURL(String)}
	 * @param defaultMimetype le type MIME à utiliser par défaut si la réponse HTTP ne le précise pas
	 * @return le contenu de l'URL donnée au format "data:..."
	 */
	public static final String downloadURLAsDataUrl(final String url, final String defaultMimetype) {
		try {
			HttpURLConnection connection = openURL(url);
			try (InputStream stream = connection.getInputStream()) {
				byte[] bytes = IOUtils.toByteArray(stream);
				String mimetype = connection.getContentType();
				if (StringUtils.isBlank(mimetype))
					mimetype = defaultMimetype;
				return "data:" + mimetype + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
			}
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * Cette méthode n'est pas conseillée. Vous pouvez l'utiliser, A VOS RISQUES ET PERILS, pour ouvrir une connexion
	 * HTTPS sans vérification du certificat présenté. Ceci peut vous exposer à des attaques pour intercepter ou modifier le flux.
	 *
	 * @param connection la connexion HTTPS à ouvrir coûte que coûte (déconseillé !)
	 * @throws IOException
	 */
	/* Méthode risquée, uniquement faite pour des tests */
	public static final void unsecuredConnectionUseAtYourOwnRisk(final HttpsURLConnection connection) throws IOException {
		try {
			connection.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			SSLContext sc = SSLContext.getInstance("SSL");
			X509TrustManager manager = new X509TrustManager() {

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					//
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					//
				}
			};
			sc.init(null, new TrustManager[] { manager }, new java.security.SecureRandom());
			connection.setSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException | KeyManagementException ex) {
			throw new IOException(ex);
		}
	}

	/**
	 * Cette classe permet de générer une requête en "multipart/form-data" (par exemple pour l'upload d'un fichier) sans
	 * nécessiter de librairie externe, comme HttpClient.
	 *
	 * L'idée de départ vient d'ici : https://blog.morizyun.com/blog/android-httpurlconnection-post-multipart/index.html
	 */
	public static final class MultiPartAdapter implements AutoCloseable {

		private final HttpURLConnection connection;
		private final String boundary;
		private final OutputStream stream;

		public MultiPartAdapter(HttpURLConnection connection, String boundary) throws IOException {
			super();
			this.connection = connection;
			this.connection.setDoOutput(true);
			this.connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			this.boundary = boundary;
			this.stream = this.connection.getOutputStream();
		}

		public void addFormField(String name, String value) throws IOException {
			this.write("--" + this.boundary + "\r\n");
			this.write("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
			this.write("Content-Type: text/plain; charset=UTF-8\r\n");
			this.write("\r\n");
			this.write(value + "\r\n");
			this.stream.flush();
		}

		public void addFileUpload(String name, String fileName, byte[] content) throws IOException {
			this.write("--" + this.boundary + "\r\n");
			this.write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n");
			this.write("\r\n");
			this.stream.write(content);
			this.write("\r\n");
			this.stream.flush();
		}

		public void addFileUpload(String name, String fileName, File file) throws IOException {
			this.write("--" + this.boundary + "\r\n");
			this.write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n");
			this.write("\r\n");
			try (FileInputStream is = new FileInputStream(file)) {
				IOUtils.copy(is, this.stream);
			}
			this.write("\r\n");
			this.stream.flush();
		}

		@Override
		public void close() throws IOException {
			this.write("--" + this.boundary + "--\r\n");
			this.stream.flush();
			this.stream.close();
		}

		private void write(String s) throws IOException {
			this.stream.write(s.getBytes(StandardCharsets.UTF_8));
		}
	}
}
