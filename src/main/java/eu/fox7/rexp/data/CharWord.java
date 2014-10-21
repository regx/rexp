package eu.fox7.rexp.data;

import java.util.Arrays;

public class CharWord extends Word {
	private static String buildString(int len, char c) {
		char[] ca = new char[len];
		Arrays.fill(ca, c);
		return new String(ca);
	}

	public static CharWord create(int len, char c) {
		return new CharWord(buildString(len, c));
	}

	private String str;

	public CharWord(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public Symbol getSymbol(int index) {
		return new CharSymbol(str.charAt(index));
	}

	@Override
	public int getLength() {
		return str.length();
	}
}
