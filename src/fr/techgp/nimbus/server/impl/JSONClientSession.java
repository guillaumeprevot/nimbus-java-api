package fr.techgp.nimbus.server.impl;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import fr.techgp.nimbus.server.Session.ClientSession;
import fr.techgp.nimbus.utils.ConversionUtils;
import fr.techgp.nimbus.utils.RandomUtils;

/**
 * Implementation of a session securely stored in a cookie on the client side.
 *
 * The idea is based on WebMotion "ClientSession" I used to play with.
 *
 * The implementation follows the best practice as explained here (notably page 15) :
 *   https://crypto.stanford.edu/cs142/papers/web-session-management.pdf
 * This document is saved in the "doc" folder.
 *
 * @deprecated use {@link JWTClientSession} instead
 */
@Deprecated()
public class JSONClientSession implements ClientSession {

	/** The name of the cookie storing session on the client-side */
	private static final String CLIENT_SESSION_COOKIE_NAME = "nimbus-client-session";
	/** The algorithm used to generate the server key for client session signature */
	private static final String CLIENT_SESSION_KEY_ALGORITHM = "HmacSHA256";
	/** The source of randomness for session id and encryption */
	private static final SecureRandom RANDOM = new SecureRandom();

	private final ServletRequest request;
	private String id;
	private long creationTime;
	private long lastAccessedTime;
	private boolean isNew;
	private int maxInactiveInterval;
	private JsonObject attributes;

	public JSONClientSession(ServletRequest request) {
		this.request = request;
		this.initDefaults();
	}

	public JSONClientSession(ServletRequest request, String id, long creationTime, long lastAccessedTime, int maxInactiveInterval, JsonObject attributes) {
		this.request = request;
		this.id = id;
		this.creationTime = creationTime;
		this.lastAccessedTime = lastAccessedTime;
		this.isNew = false;
		this.maxInactiveInterval = maxInactiveInterval;
		this.attributes = attributes;
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public long creationTime() {
		return this.creationTime;
	}

	@Override
	public long lastAccessedTime() {
		return this.lastAccessedTime;
	}

	@Override
	public boolean isNew() {
		return this.isNew;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T attribute(String name) {
		JsonElement e = this.attributes.get(name);
		if (e == null || e.isJsonNull())
			return null;
		JsonPrimitive p = e.getAsJsonPrimitive();
		if (p.isBoolean())
			return (T) Boolean.valueOf(p.getAsBoolean());
		if (p.isString())
			return (T) p.getAsString();
		return (T) p.getAsNumber();
	}

	@Override
	public JSONClientSession attribute(String name, Object value) {
		if (value == null)
			this.attributes.remove(name);
		else if (value instanceof Boolean)
			this.attributes.addProperty(name, (Boolean) value);
		else if (value instanceof String)
			this.attributes.addProperty(name, (String) value);
		else if (value instanceof Number)
			this.attributes.addProperty(name, (Number) value);
		else
			throw new UnsupportedOperationException("Unsupported attribute type " + value.getClass().getName() + " (only Boolean, String, Number are supported)");
		return this;
	}

	@Override
	public JSONClientSession removeAttribute(String name) {
		this.attributes.remove(name);
		return this;
	}

	@Override
	public int maxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	@Override
	public JSONClientSession maxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
		return this;
	}

	@Override
	public void invalidate() {
		this.initDefaults();
	}

	protected void initDefaults() {
		this.id = RandomUtils.randomAscii(RANDOM, 32, true, true, true, null);
		this.creationTime = System.currentTimeMillis();
		this.lastAccessedTime = this.creationTime;
		this.isNew = true;
		this.maxInactiveInterval = this.request.getSessionConfig().getTimeout();
		this.attributes = new JsonObject();
	}

	protected static JSONClientSession load(ServletRequest request, boolean create) {
		// Get the client-session cookie (no cookie => no session yet)
		ServletCookie cookie = request.cookie(CLIENT_SESSION_COOKIE_NAME);
		if (cookie == null)
			return create ? new JSONClientSession(request) : null;

		// Load secret key (no secret key => invalidates any previous session)
		byte[] secretKey = request.getSessionConfig().getSecretKey();
		if (secretKey == null)
			return create ? new JSONClientSession(request) : null;

		// Decode the cookie value while checking for any alteration
		byte[] bytes;
		try {
			bytes = decrypt(secretKey, cookie.value());
		} catch (InvalidParameterException ex) {
			ex.printStackTrace();
			return create ? new JSONClientSession(request) : null;
		}
		String json = new String(bytes, StandardCharsets.UTF_8);

		// Parse cookie as JSON Object { id: String, creationTime: long, lastAccessedTime: long, maxInactiveInterval: int, attributes: map }
		JsonElement e = JsonParser.parseString(json);
		if (!e.isJsonObject())
			throw new IllegalStateException("client-session should contain a JSON object");
		JsonObject o = e.getAsJsonObject();

		// Check if session has expired
		long lastAccessedTime = o.get("lastAccessedTime").getAsLong();
		int maxInactiveInterval = o.get("maxInactiveInterval").getAsInt();
		long now = System.currentTimeMillis();
		boolean expired = maxInactiveInterval > 0 && (now - lastAccessedTime) > maxInactiveInterval * 1000;
		if (expired)
			return create ? new JSONClientSession(request) : null;

		// Restore session, update "lastAccessedTime" and keep current "maxInactiveInterval"
		String id = o.get("id").getAsString();
		long creationTime = o.get("creationTime").getAsLong();
		JsonObject attributes = o.getAsJsonObject("attributes").getAsJsonObject();
		JSONClientSession session = new JSONClientSession(request, id, creationTime, now, maxInactiveInterval, attributes);
		return session;
	}

	protected static void save(JSONClientSession session, ServletResponse response) {
		// Skip saving if no client-session is used
		if (session == null)
			return;

		// Format client-session as JSON
		JsonObject o = new JsonObject();
		o.addProperty("id", session.id());
		o.addProperty("creationTime", session.creationTime());
		o.addProperty("lastAccessedTime", session.lastAccessedTime());
		o.addProperty("maxInactiveInterval", session.maxInactiveInterval());
		o.add("attributes", session.attributes);
		String json = o.toString();

		// Get (or create) secret key for client session encryption
		SessionConfig config = session.request.getSessionConfig();
		byte[] secretKey = config.getSecretKey();
		if (secretKey == null) {
			synchronized (config) {
				if (config.getSecretKey() == null) {
					secretKey = generateAES256SecretKey();
					config.setSecretKey(secretKey);
					System.out.println("Generated new secret key " + ConversionUtils.bytes2hex(secretKey));
				}
			}
		}

		// Encode cookie
		String value = encrypt(secretKey, json.getBytes(StandardCharsets.UTF_8));

		// Add cookie to response
		response.cookie(
				CLIENT_SESSION_COOKIE_NAME,
				config.getCookiePath(),
				value,
				config.getCookieDomain(),
				session.maxInactiveInterval(),
				true,
				true);
	}

	public static final String encrypt(byte[] key, byte[] data) {
		try {
			// Generate random IV as byte array
			byte[] iv = RandomUtils.randomBytes(RANDOM, 16);

			// Generate timestamp as byte array
			long timestamp = System.currentTimeMillis();
			byte[] timestampBytes = ConversionUtils.long2bytes(timestamp);

			// Encrypt data as AES/CBC(secretKey, random IV, compress(data))
			SecretKeySpec aesKeySpec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec);
			byte[] encryptedData = cipher.doFinal(data);

			// Calculate HMAC signature : HMAC(secretKey, iv | timestamp | encryptedData)
			SecretKeySpec hmacKeySpec = new SecretKeySpec(key, CLIENT_SESSION_KEY_ALGORITHM);
			Mac mac = Mac.getInstance(CLIENT_SESSION_KEY_ALGORITHM);
			mac.init(hmacKeySpec);
			mac.update(iv);
			mac.update(timestampBytes);
			mac.update(encryptedData);
			byte[] signature = mac.doFinal();

			// Renvoyer la valeur du cookie
			return ConversionUtils.bytes2hex(signature)
					+ "|" + ConversionUtils.bytes2hex(iv)
					+ "|" + ConversionUtils.bytes2hex(timestampBytes)
					+ "|" + ConversionUtils.bytes2hex(encryptedData);
		} catch (Exception ex) {
			throw new RuntimeException("Expected algorithm is not supported", ex);
		}
	}

	public static final byte[] decrypt(byte[] key, String value) {
		try {
			// Split value
			String[] parts = value.split("\\|");
			if (parts.length != 4)
				throw new InvalidParameterException("Value is expected to be signature|iv|timestamp|data");
			byte[] signature = ConversionUtils.hex2bytes(parts[0]);
			byte[] iv = ConversionUtils.hex2bytes(parts[1]);
			byte[] timestampBytes = ConversionUtils.hex2bytes(parts[2]);
			byte[] encryptedData = ConversionUtils.hex2bytes(parts[3]);

			// Verify HMAC signature : HMAC(secretKey, iv | timestamp | encryptedData)
			SecretKeySpec hmacKeySpec = new SecretKeySpec(key, CLIENT_SESSION_KEY_ALGORITHM);
			Mac mac = Mac.getInstance(CLIENT_SESSION_KEY_ALGORITHM);
			mac.init(hmacKeySpec);
			mac.update(iv);
			mac.update(timestampBytes);
			mac.update(encryptedData);
			byte[] messageSignature = mac.doFinal();
			if (!Arrays.equals(signature, messageSignature))
				throw new InvalidParameterException("Signature validation has failed");

			// Decrypt data as AES/CBC(secretKey, random IV, data)
			SecretKeySpec aesKeySpec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, ivSpec);
			return cipher.doFinal(encryptedData);

		} catch (NoSuchAlgorithmException | InvalidKeyException
				| NoSuchPaddingException | InvalidAlgorithmParameterException
				| BadPaddingException | IllegalBlockSizeException ex) {
			throw new RuntimeException("Expected algorithm is not supported", ex);
		}
	}

	public static final byte[] generateAES256SecretKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(256);
			SecretKey k = keyGenerator.generateKey();
			return k.getEncoded();
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException("Expected algorithm is non supported", ex);
		}
	}

}
