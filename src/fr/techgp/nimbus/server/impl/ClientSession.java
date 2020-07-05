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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.techgp.nimbus.server.Session;
import fr.techgp.nimbus.server.Utils;

/**
 * Implementation of a session securely stored in a cookie on the client side.
 *
 * The idea is based on WebMotion "ClientSession" I used to play with.
 *
 * The implementation follows the best practice as explained here (notably page 15) :
 *   https://crypto.stanford.edu/cs142/papers/web-session-management.pdf
 * This document is saved in the "doc" folder.
 */
public class ClientSession implements Session {

	/** The name of the cookie storing session on the client-side */
	public static final String CLIENT_SESSION_COOKIE_NAME = "nimbus-client-session";
	/** The default timeout for the client-side session, in minutes (see maxInactiveInterval) */
	private static final int CLIENT_SESSION_DEFAULT_MAX_ACTIVE_INTERVAL = 60 * 60;
	/** The source of randomness for session id and encryption */
	private static final SecureRandom RANDOM = new SecureRandom();
	/** The secret key to used for client session encryption */
	public static byte[] CLIENT_SESSION_SECRET_KEY = null;

	private String id;
	private long creationTime;
	private long lastAccessedTime;
	private boolean isNew;
	private int maxInactiveInterval;
	private JsonObject attributes;

	public ClientSession() {
		this.initDefaults();
	}

	public ClientSession(String id, long creationTime, long lastAccessedTime, JsonObject attributes) {
		this.id = id;
		this.creationTime = creationTime;
		this.lastAccessedTime = lastAccessedTime;
		this.isNew = false;
		this.maxInactiveInterval = CLIENT_SESSION_DEFAULT_MAX_ACTIVE_INTERVAL;
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
		return e instanceof JsonNull ? null : (T) e;
	}

	@Override
	public void attribute(String name, Object value) {
		if (value == null)
			this.attributes.add(name, JsonNull.INSTANCE);
		else if (value instanceof JsonElement)
			this.attributes.add(name, (JsonElement) value);
		else if (value instanceof Boolean)
			this.attributes.addProperty(name, (Boolean) value);
		else if (value instanceof Character)
			this.attributes.addProperty(name, (Character) value);
		else if (value instanceof Number)
			this.attributes.addProperty(name, (Number) value);
		else if (value instanceof String)
			this.attributes.addProperty(name, (String) value);
		else
			throw new InvalidParameterException(value.getClass().getName() + " is not a supported client-session attribute type");
	}

	@Override
	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	@Override
	public int maxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	@Override
	public void maxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public void invalidate() {
		this.initDefaults();
	}

	protected void initDefaults() {
		this.id = Utils.randomAscii(RANDOM, 32, true, true, true, null);
		this.creationTime = System.currentTimeMillis();
		this.lastAccessedTime = this.creationTime;
		this.isNew = true;
		this.maxInactiveInterval = CLIENT_SESSION_DEFAULT_MAX_ACTIVE_INTERVAL;
		this.attributes = new JsonObject();
	}

	protected static ClientSession load(ServletRequest request, boolean create) {
		// Get the client-session cookie
		ServletCookie cookie = request.cookie(CLIENT_SESSION_COOKIE_NAME);
		if (cookie == null)
			return create ? new ClientSession() : null;

		// Load secret key
		byte[] secretKey = ClientSession.CLIENT_SESSION_SECRET_KEY;
		if (secretKey == null)
			return create ? new ClientSession() : null;

		// Decode the cookie value while checking for any alteration
		byte[] bytes = decrypt(secretKey, cookie.value());
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
			return new ClientSession();

		// Restore session, update "lastAccessedTime" and keep current "maxInactiveInterval"
		String id = o.get("id").getAsString();
		long creationTime = o.get("creationTime").getAsLong();
		JsonObject attributes = o.getAsJsonObject("attributes").getAsJsonObject();
		ClientSession session = new ClientSession(id, creationTime, now, attributes);
		session.maxInactiveInterval(maxInactiveInterval);
		return session;
	}

	protected static void save(ClientSession session, ServletResponse response) {
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
		byte[] secretKey = ClientSession.CLIENT_SESSION_SECRET_KEY;
		if (secretKey == null) {
			synchronized (ClientSession.class) {
				if (ClientSession.CLIENT_SESSION_SECRET_KEY == null) {
					ClientSession.CLIENT_SESSION_SECRET_KEY = generateAES256SecretKey();
					System.out.println("Generated new secret key " + Utils.bytes2hex(ClientSession.CLIENT_SESSION_SECRET_KEY));
					secretKey = ClientSession.CLIENT_SESSION_SECRET_KEY;
				}
			}
		}

		// Encode cookie
		String value = encrypt(secretKey, json.getBytes(StandardCharsets.UTF_8));

		// Add cookie to response
		ServletCookie cookie = response.cookie(CLIENT_SESSION_COOKIE_NAME, value);
		cookie.secure(true);
		cookie.httpOnly(true);
		cookie.maxAge(session.maxInactiveInterval());
	}

	public static final String encrypt(byte[] key, byte[] data) {
		try {
			// Generate random IV as byte array
			byte[] iv = new byte[16];
			RANDOM.nextBytes(iv);

			// Generate timestamp as byte array
			long timestamp = System.currentTimeMillis();
			byte[] timestampBytes = Utils.long2bytes(timestamp);

			// Encrypt data as AES/CBC(secretKey, random IV, compress(data))
			SecretKeySpec aesKeySpec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec);
			byte[] encryptedData = cipher.doFinal(data);

			// Calculate HMAC signature : HMAC(secretKey, iv | timestamp | encryptedData)
			SecretKeySpec hmacKeySpec = new SecretKeySpec(key, "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(hmacKeySpec);
			mac.update(iv);
			mac.update(timestampBytes);
			mac.update(encryptedData);
			byte[] signature = mac.doFinal();

			// Renvoyer la valeur du cookie
			return Utils.bytes2hex(signature)
					+ "|" + Utils.bytes2hex(iv)
					+ "|" + Utils.bytes2hex(timestampBytes)
					+ "|" + Utils.bytes2hex(encryptedData);
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
			byte[] signature = Utils.hex2bytes(parts[0]);
			byte[] iv = Utils.hex2bytes(parts[1]);
			byte[] timestampBytes = Utils.hex2bytes(parts[2]);
			byte[] encryptedData = Utils.hex2bytes(parts[3]);

			// Verify HMAC signature : HMAC(secretKey, iv | timestamp | encryptedData)
			SecretKeySpec hmacKeySpec = new SecretKeySpec(key, "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
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

	public static final byte[] loadAES256SecretKey(String hex) {
		if (hex.length() != 64)
			throw new InvalidParameterException("AES key should be 256 bits (i.e. 32 bytes, i.e. 64 hexadecimal characters)");
		return Utils.hex2bytes(hex);
	}

}
