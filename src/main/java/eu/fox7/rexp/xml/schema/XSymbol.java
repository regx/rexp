package eu.fox7.rexp.xml.schema;

import eu.fox7.rexp.data.Symbol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XSymbol implements Symbol, Comparable<XSymbol> {
	protected String name;
	protected String type;

	public XSymbol(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("[%s#%s]", name, type);
	}

	public XSymbol fromString(String str) {
		Pattern p = Pattern.compile("\\[(\\w*)#(\\w*)\\]");
		Matcher m = p.matcher(str);
		if (m.matches()) {
			return new XSymbol(m.group(1), m.group(2));
		} else {
			throw new RuntimeException("Invalid string for symbol");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final XSymbol other = (XSymbol) obj;
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(XSymbol o) {
		return this.toString().compareTo(o.toString());
	}
}
