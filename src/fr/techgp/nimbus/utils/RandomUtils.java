package fr.techgp.nimbus.utils;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.Random;

public final class RandomUtils {

	private RandomUtils() {
		//
	}

	/**
	 * Cette méthode retourne un tableau de "length" octets aléatoires en utilisant le générateur ({@link Random} ou {@link SecureRandom}).
	 *
	 * @param random le générateur aléatoire à utiliser
	 * @param length le nombre d'octets à générer
	 * @return le tableau contenant le nombre souhaité d'octets aléatoires
	 */
	public static final byte[] randomBytes(Random random, int length) {
		byte[] result = new byte[length];
		random.nextBytes(result);
		return result;
	}

	/**
	 * Cette méthode génère une chaine de caractères aléatoire de la longueur demandée, dont l'ensemble de caractères ASCII est configurable.
	 *
	 * @param random le générateur aléatoire à utiliser
	 * @param count la taille de la chaine de caractères à générer
	 * @param lowercase indique si les lettres minuscules [a-z] sont autorisées
	 * @param uppercase indique si les lettres majuscules [A-Z] sont autorisées
	 * @param digits indique si les chiffres [0-9] sont autorisées
	 * @param others indique un tableau de caractères ASCII supplémentaires autorisés
	 * @return une chaine de caractères aléatoires de la longueur souhaitée
	 */
	public static final String randomAscii(Random random, int count, boolean lowercase, boolean uppercase, boolean digits, char[] others) {
		if (count < 0)
			throw new InvalidParameterException("String length " + count + " is invalid.");
		if (count == 0)
			return "";
		// Préparer dans "builder" le liste des caractères autorisés
		StringBuilder builder = new StringBuilder();
		if (lowercase)
			for (char c = 'a'; c <= 'z'; c++) { builder.append(c); }
		if (uppercase)
			for (char c = 'A'; c <= 'Z'; c++) { builder.append(c); }
		if (digits)
			for (char c = '0'; c <= '9'; c++) { builder.append(c); }
		if (others != null)
			for (char c : others) { if (StringUtils.isOtherAsciiPrintableCharacter(c)) builder.append(c); }
		char[] pool = builder.toString().toCharArray();
		// Générer la chaine de caractères aléatoires en prenant à chaque fois 1 caractère aléatoirement parmi les caractères autorisés
		builder.setLength(count);
		for (int i = 0; i < count; i++) {
			int offset = random.nextInt(pool.length);
			builder.setCharAt(i, pool[offset]);
		}
		return builder.toString();
	}

}
