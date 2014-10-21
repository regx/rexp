package eu.fox7.rexp.data;

public class EpsilonSymbol extends CharSymbol {
	public static final EpsilonSymbol INSTANCE = new EpsilonSymbol();

	private EpsilonSymbol() {
		super('\u0395');
	}
}