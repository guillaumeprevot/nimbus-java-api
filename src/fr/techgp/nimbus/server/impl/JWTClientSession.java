package fr.techgp.nimbus.server.impl;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.techgp.nimbus.server.Session.ClientSession;
import fr.techgp.nimbus.utils.ConversionUtils;
import fr.techgp.nimbus.utils.RandomUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;

/**
 * Implementation of a session stored in a JWT on the client side.
 */
public class JWTClientSession implements ClientSession {

	/** The name of the cookie storing session on the client-side */
	private static final String CLIENT_SESSION_COOKIE_NAME = "nimbus-client-session";
	/** The algorithm used to generate the server key for client session signature */
	private static final MacAlgorithm CLIENT_SESSION_KEY_ALGORITHM = Jwts.SIG.HS256;
	/** The source of randomness for session id and encryption */
	private static final SecureRandom RANDOM = new SecureRandom();

	private final ServletRequest request;
	private String id;
	private long creationTime;
	private long lastAccessedTime;
	private boolean isNew;
	private int maxInactiveInterval;
	private HashMap<String, Object> attributes;

	public JWTClientSession(ServletRequest request) {
		this.request = request;
		this.initDefaults();
	}

	public JWTClientSession(ServletRequest request, String id, long creationTime, long lastAccessedTime, int maxInactiveInterval, Map<String, Object> attributes) {
		this.request = request;
		this.id = id;
		this.creationTime = creationTime;
		this.lastAccessedTime = lastAccessedTime;
		this.isNew = false;
		this.maxInactiveInterval = maxInactiveInterval;
		this.attributes = new HashMap<>(attributes);
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
		return (T) this.attributes.get(name);
	}

	@Override
	public void attribute(String name, Object value) {
		if (value == null)
			this.attributes.remove(name);
		else
			this.attributes.put(name, value);
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
		this.id = RandomUtils.randomAscii(RANDOM, 32, true, true, true, null);
		this.creationTime = System.currentTimeMillis();
		this.lastAccessedTime = this.creationTime;
		this.isNew = true;
		this.maxInactiveInterval = this.request.getSessionConfig().getTimeout();
		this.attributes = new HashMap<>();
	}

	protected static JWTClientSession load(ServletRequest request, boolean create) {
		// Get the client-session cookie (no cookie => no session yet)
		ServletCookie cookie = request.cookie(CLIENT_SESSION_COOKIE_NAME);
		if (cookie == null)
			return create ? new JWTClientSession(request) : null;

		// Load secret key (no secret key => invalidates any previous session)
		byte[] secretKey = request.getSessionConfig().getSecretKey();
		if (secretKey == null)
			return create ? new JWTClientSession(request) : null;

		// Decode the JWT
		Jws<Claims> jwt;
		try {
			jwt = Jwts.parser()
					.verifyWith(Keys.hmacShaKeyFor(secretKey))
					.build()
					.parseSignedClaims(cookie.value());
		} catch (JwtException ex) {
			// JWT has expired or has been tempered with
			return create ? new JWTClientSession(request) : null;
		}

		// Restore session, update "lastAccessedTime" and keep current "maxInactiveInterval"
		Claims claims = jwt.getPayload();
		String id = claims.getId();
		long creationTime = claims.getIssuedAt().getTime();
		long lastAccessedTime = System.currentTimeMillis();
		int maxInactiveInterval = Integer.parseInt((String) claims.get("maxInactiveInterval"));
		HashMap<String, Object> attributes = new HashMap<>(claims); // Claims is now Immutable, copy before removing
		attributes.remove(Claims.ID);
		attributes.remove(Claims.EXPIRATION);
		attributes.remove(Claims.ISSUER);
		attributes.remove(Claims.ISSUED_AT);
		attributes.remove("maxInactiveInterval");
		return new JWTClientSession(request, id, creationTime, lastAccessedTime, maxInactiveInterval, attributes);
	}

	protected static void save(JWTClientSession session, ServletResponse response) {
		// Skip saving if no client-session is used
		if (session == null)
			return;

		// Get (or create) secret key for client session encryption
		SessionConfig config = session.request.getSessionConfig();
		byte[] secretKey = config.getSecretKey();
		if (secretKey == null) {
			synchronized (config) {
				if (config.getSecretKey() == null) {
					secretKey = generateClientSessionSecretKey();
					config.setSecretKey(secretKey);
					System.out.println("Generated new secret key " + ConversionUtils.bytes2hex(secretKey));
				}
			}
		}

		// Encode cookie

		String value = Jwts.builder()
			.claims()
				.id(session.id)
				.expiration(new Date(session.lastAccessedTime() + session.maxInactiveInterval() * 1000))
				.issuer("Nimbus")
				.issuedAt(new Date(session.creationTime))
				.add("maxInactiveInterval", Integer.toString(session.maxInactiveInterval))
				.add(session.attributes)
				.and()
			.signWith(Keys.hmacShaKeyFor(secretKey))
			.compact();

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

	public static final byte[] generateClientSessionSecretKey() {
		return CLIENT_SESSION_KEY_ALGORITHM.key().build().getEncoded();
	}

	public static void main(String[] args) {
		if (args.length == 1 && "generateKey".equals(args[0])) {
			byte[] keyBytes = generateClientSessionSecretKey();
			String keyString = ConversionUtils.bytes2hex(keyBytes);
			System.out.println(keyString);
		}
	}

}
