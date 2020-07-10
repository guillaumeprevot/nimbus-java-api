package fr.techgp.nimbus.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Cette classe reprend les informations touvées sur Internet quant aux bonnes pratiques sur le hachage de mot de
 * passes. Merci pour ces explications détaillées sur le "pourquoi du comment".
 *
 * @see http://linuxfr.org/users/elyotna/journaux/l-art-de-stocker-des-mots-de-passe
 * @see http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
 * @see https://crackstation.net/hashing-security.htm
 * @see https://en.wikipedia.org/wiki/Password_strength
 * @see http://keepass.info/help/base/pwgenerator.html
 * @see https://pages.nist.gov/800-63-3/sp800-63b.html
 */
public final class CryptoUtils {

	private CryptoUtils() {
		//
	}

	/**
	 * Cette méthode calcule l'entropie H d'un mot de passe de longueur L en calculant la taille N de l'ensemble de caractères qu'il utilise.
	 * <br />
	 * Les ensembles de caractères considérés sont issus d'une page de <a href="https://en.wikipedia.org/wiki/Password_strength">Wikipédia</a>
	 * <ul>
	 * <li>les chiffres (0..9)</li>
	 * <li>les lettres minuscules utilisées en hexadécimal (a..f)</li>
	 * <li>les lettres majuscules utilisées en hexadécimal (A..F)</li>
	 * <li>les lettres minuscules de l'alphabet complet (a..z)</li>
	 * <li>les lettres majuscules de l'alphabet complet (A..Z)</li>
	 * <li>les caractères ASCII imprimables (" !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~")</li>
	 * <li>les caractères ASCII étendu (123 de plus)</li>
	 * </li>
	 *
	 * Toutefois, 2 ajustements ont été faits concernant la longueur :
	 * <ul>
	 * <li>les répétitions de caractères ne compte qu'à moitié dans L pour se rapprocher des résultats de KeePass</li>
	 * <li>l'ajout de caractères ASCII étendu (comme 'é') augmente la taille N de 1, en non pas automatiquement des 123 caractères étendus)</li>
	 * </ul>
	 *
	 * @param password le mot de passe dont on veut calculer l'entropie
	 * @param adjustRepetitions indique si on ajuste par rapport aux répétitions (devrait être "true")
	 * @return l'entropie
	 */
	public static final int evaluatePassword(final String password, final boolean adjustRepetitions) {
		String ascii = StringUtils.OTHER_ASCII_PRINTABLE_CHARACTERS;
		// On vérifie si le mot de passe contient au moins un caractère dans chaque ensemble suivant
		boolean hasDigit = false;
		boolean hasLowerHex = false;
		boolean hasUpperHex = false;
		boolean hasLowerOther = false;
		boolean hasUpperOther = false;
		boolean hasAscii = false;
		int otherCount = 0;
		// On compte le nombre de répétition pour diminuer sensiblement la "longueur" du mot de passe
		int repeatCount = 0;
		for (int i = 0; i < password.length(); i++) {
			char c = password.charAt(i);
			if (password.indexOf(c, i + 2) >= 0) {
				repeatCount++; // on compte la répétition
				continue; // on le traitera à sa dernière apparition
			}
			boolean isDigit = (c >= '0' && c <= '9');
			boolean isLower =  (c >= 'a' && c <= 'z');
			boolean isUpper = (c >= 'A' && c <= 'Z');
			boolean isAscii = (ascii.indexOf(c) >= 0);
			hasDigit = hasDigit || isDigit;
			hasLowerHex = hasLowerHex || (isLower && c <= 'f');
			hasUpperHex = hasUpperHex || (isUpper && c <= 'F');
			hasLowerOther = hasLowerOther || (isLower && c >= 'g');
			hasUpperOther = hasUpperOther || (isUpper && c >= 'G');
			hasAscii = hasAscii || isAscii;
			otherCount += (isDigit || isLower || isUpper || isAscii) ? 0 : 1;
		}
		// Calculer le nombre de caractères de l'ensemble auquel appartient ce mot de passe
		int size = hasDigit ? 10 : 0;
		size += hasLowerOther ? 26 : hasLowerHex ? 6 : 0;
		size += hasUpperOther ? 26 : hasUpperHex ? 6 : 0;
		size += hasAscii ? ascii.length() : 0;
		size += otherCount;
		// Calculer l'entropie obtenu par un mot de passe de cette longueur et cet ensemble (basé sur une formule Wikipédia)
		double length = password.length() - (adjustRepetitions ? 0.5 * repeatCount : 0);
		int entropy = (int) (length * Math.log(size) / Math.log(2));
		return entropy;
	}

	/**
	 * Le nom d'itérations. Plus c'est grand, mieux c'est mais attention car c'est aussi plus long :
	 *
	 * -   1 000 dans la spec et jusqu'à récemment
	 * -   1 000 à 2 000 pour TrueCrypt (en version 7.1a)
	 * -  10 000 dorénavant (comme décrit ici depuis 2016 : https://pages.nist.gov/800-63-3/sp800-63b.html)
	 * - 200 000 minimum pour VeraCrypt (mais un peu trop long !)
	 */
	public static final int PASSWORD_HASH_CURRENT_ITERATIONS = 10_000;

	/**
	 * Le sel de 512 bits (64 octets), généré aléatoirement pour se protéger d'une "rainbow table"
	 * construite spécifiquement pour cracker un compte dont on connaitrait le login.
	 *
	 * - 64 bits (8 octets) dans la spec
	 * - 240 bits (30 octets) jusqu'à récemment
	 * - 512 bits (64 octets) dorénavant, comme TrueCrypt et VeraCrypt
	 */
	public static final int PASSWORD_HASH_CURRENT_SALT_BYTES = 64;

	/**
	 * L'algorithme utilisé pour hacher les mots de passe, qui doit être relativement long afin
	 * de ralentir l'attaque "brute-force".
	 *
	 * - PBKDF2WithHmacSHA1 dans la spéc
	 * - PBKDF2WithHmacSHA256 augmenterait l'empreinte mémoire pour minimiser les attaques avec matériel spécifique (GPU / ASIC)
	 */
	public static final String PASSWORD_HASH_ALGORITHM = "PBKDF2WithHmacSHA1";

	/**
	 * La longueur du hash généré par {@link CryptoUtils#PASSWORD_HASH_ALGORITHM}.
	 *
	 * - PBKDF2WithHmacSHA1 génère des hash de 64 octets (128 caractères en hexa)
	 */
	public static final int PASSWORD_HASH_BYTES = 64;

	/**
	 * Cette méthode calcule l'empreinte du mot de passe donné afin de fournir une valeur pouvant être stockée en base
	 * et permettant plus tard de vérifier une tentative d'authentification.
	 *
	 * @param password le mot de passe à hasher pour être stocké
	 * @return une chaine de la forme iterations + ":" + hex(salt) + ":" + hex(PBKDF2WithHmacSHA1(password, salt, iterations))
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static final String hashPassword(final String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Préparer les paramètres de l'algorithme de hachage
		char[] chars = password.toCharArray();
		byte[] salt = RandomUtils.randomBytes(new SecureRandom(), PASSWORD_HASH_CURRENT_SALT_BYTES);
		int iterations = PASSWORD_HASH_CURRENT_ITERATIONS;
		int keyLength = PASSWORD_HASH_BYTES * 8;

		// Calculer le hash pour le mot de passe indiqué
		PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyLength);
		SecretKeyFactory skf = SecretKeyFactory.getInstance(PASSWORD_HASH_ALGORITHM);
		byte[] hash = skf.generateSecret(spec).getEncoded();

		// Renvoyer le résultat formaté sous la forme "iterations:saltHex:hashHex"
		return iterations + ":" + ConversionUtils.bytes2hex(salt) + ":" + ConversionUtils.bytes2hex(hash);
	}

	/**
	 * Cette méthode teste si le mot de passe "testedPassword" correspond au mot de passe précédemment "hashé" dans "storedPassword". Pour cela :
	 * <ol>
	 * <li>la méthode extrait le nombre d'itérations et le sel du début de "storedPassword"</li>
	 * <li>la méthode calcule le hash de "testedPassword" en utilisant le sel et le nombre d'itérations trouvés en (1)</li>
	 * <li>la méthode compare enfin le résultat de l'étape (2) avec le hash stocké à la fin de "storedPassword"</li>
	 * </ol>
	 *
	 * @param testedPassword le mot de passe à tester
	 * @param storedPassword un hash de la forme iterations + ":" + hex(salt) + ":" + hex(PBKDF2WithHmacSHA1(password, salt, iterations))
	 * @param errorHandler un gestionnaire pour des erreurs inattendues (normalement, aucune ne devrait être lancée)
	 * @return true si testedPassword est le bon mot de passe
	 */
	public static final boolean validatePassword(final String testedPassword, final String storedPassword, Consumer<Exception> errorHandler) {
		try {
			// Extraire les informations depuis le hash donnée, de la forme "iterations:saltHex:hashHex"
			String[] parts = storedPassword.split(":");
			int iterations = Integer.parseInt(parts[0]);
			byte[] salt = ConversionUtils.hex2bytes(parts[1]);
			byte[] hash = ConversionUtils.hex2bytes(parts[2]);

			// Calculer le hash pour le mot de passe à tester
			PBEKeySpec spec = new PBEKeySpec(testedPassword.toCharArray(), salt, iterations, hash.length * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(PASSWORD_HASH_ALGORITHM);
			byte[] testedHash = skf.generateSecret(spec).getEncoded();

			// Comparer les 2 hash en temps constant
			return slowEquals(hash, testedHash);
		} catch (Exception ex) {
			if (errorHandler != null)
				// Prévenir de l'erreur
				errorHandler.accept(ex);
			else
				// Ou tracer l'erreur
				System.err.println(ex.getClass().getName() + " : " + ex.getMessage());
			// Rejeter les mots de passe en attendant d'en savoir plus
			return false;
		}
	}

	/**
	 * Cette fonction compare 2 tableaux de byte mais <b>en temps constant</b> (en parcourant tout le tableau), dans le
	 * but de se protéger contre les attaques temporelles, contrairement à {@link Arrays#equals(byte[], byte[])}.
	 *
	 * @param a1 le premier tableau
	 * @param a2 le second tableau
	 * @return true si les 2 tableaux sont identiques
	 */
	private static final boolean slowEquals(final byte[] a1, final byte[] a2) {
		int diff = a1.length ^ a2.length;
		for (int i = 0; i < a1.length && i < a2.length; i++)
			diff |= a1[i] ^ a2[i];
		return diff == 0;
	}

	/**
	 * Calcule la somme de hachage SHA-1 de la chaine donnée et renvoie la représentation en hexa du résultat.
	 *
	 * @param value une chaine de caractères considérée comme UTF-8
	 * @return la somme de hachage SHA-1 de la chaine donnée au format hexadécimal
	 */
	public static final String sha1Hex(final String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(value.getBytes(StandardCharsets.UTF_8));
			byte[] digestBytes = digest.digest();
			return ConversionUtils.bytes2hex(digestBytes);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

}
