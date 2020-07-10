package fr.techgp.nimbus.utils;

public final class FunctionalUtils {

	private FunctionalUtils() {
		//
	}

	@FunctionalInterface
	public static interface ConsumerWithException<T, E extends Exception> {
		void accept(T t) throws E;
	}

}
