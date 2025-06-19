package fr.techgp.nimbus.utils;

import java.security.InvalidParameterException;

public final class StringUtils {

	private StringUtils() {
		//
	}

	public static final String OTHER_ASCII_PRINTABLE_CHARACTERS = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

	/**
	 * Cette méthode renvoie vrai si le caractère est un caractère ASCII imprimable, en dehors des lettres (a-z, A-Z) et des chiffres (0-9).
	 *
	 * @param c le caractère à tester
	 * @return true si le caractère est bien un caractère ASCII imprimable mais non alpha-numérique
	 */
	public static final boolean isOtherAsciiPrintableCharacter(char c) {
		return OTHER_ASCII_PRINTABLE_CHARACTERS.indexOf(c) >= 0;
	}

	/**
	 * Cette méthode renvoie true si la chaine est <code>null</code>, une chaine vide ou une une chaine ne contenant que des caractères d'espacement.
	 *
	 * @param s la chaine à tester
	 * @return true si la chaine est vide
	 */
	public static final boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

	/**
	 * Cette méthode renvoie true si la chaine n'est pas <code>null</code> et contient au moins un caractère autre qu'un caractère d'échappement.
	 *
	 * @param s la chaine à tester
	 * @return true si la chaine n'est pas vide
	 */
	public static final boolean isNotBlank(String s) {
		return s != null && !s.isBlank();
	}

	/**
	 * Cette méthode retourne la premmière valeur non vide de "values", ou null si elles sont toutes vides.
	 *
	 * @param values la liste des valeurs éventuelles
	 * @return la première valeur de "values" passant le test {@link StringUtils#isNotBlank(String)}, ou null si elles sont toutes vides
	 */
	public static final String coalesce(String... values) {
		for (String value : values) {
			if (isNotBlank(value))
				return value;
		}
		return null;
	}

	/**
	 * Cette méthode retourne soit "value" (si elle n'est pas vide), soit "defaultValue" sinon.
	 *
	 * @param value la valeur à retourner si elle n'est pas vide, au sens de {@link StringUtils#isNotBlank(String)}
	 * @param defaultValue la valeur à retourner si "value" est vide
	 * @return soit "value" (si elle n'est pas vide), soit "defaultValue" sinon
	 */
	public static final String withDefault(String value, String defaultValue) {
		return isBlank(value) ? defaultValue : value;
	}

	/**
	 * Cette méthode retourne un texte contenant "count" fois le texte en entrée "value".
	 *
	 * @param value le texte à répéter
	 * @param count le nombre de répétitions souhaitées
	 * @return le texte répétant la valeur le nombre de fois souhaitée
	 */
	public static final String repeat(String value, int count) {
		if (count < 0)
			throw new InvalidParameterException("count should be >= 0");
		if (count == 0)
			return "";
		if (count == 1)
			return value;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(value);
		}
		return sb.toString();
	}

	/**
	 * Checks if text <i>value</i> is surrounded by <i>start</i> and <i>end</i>
	 *
	 * <pre>
	 * - first char = <i>start</i>
	 * - last char = <i>end</i>
	 * - no other <i>end</i> except after an another <i>start</i>
	 * </pre>
	 *
	 * The algorithm simply increases depth each time a <i>start</i> is encountered and decreases
	 * depth each time a <i>end</i> is encountered. If depth decreases to 0 before end of string, it
	 * means there each two distinct blocks.
	 *
	 * @param value the string to test
	 * @param start the starting character ('(' for instance)
	 * @param end the terminating character (')' for instance)
	 * @return true if <i>value</i> is delimited by <i>start</i> and <i>end</i>, false otherwise
	 */
	public static final boolean checkDelimitedBy(String value, char start, char end) {
		if ((value.length() < 2) || (value.charAt(0) != start) || (value.charAt(value.length() - 1) != end))
			return false;
		int curDepth = 1;
		for (int i = 1; i < value.length() - 1; i++) {
			if (value.charAt(i) == start) {
				curDepth++;
			} else if (value.charAt(i) == end) {
				curDepth--;
				if (curDepth == 0)
					return false;
			}
		}
		return true;
	}

}
