package eu.fox7.rexp.isc.cnfa.core;

public class CounterVariable {
	private static int counter = 1;
	protected int id;

	public static void resetCounter() {
		counter = 1;
	}

	public CounterVariable() {
		id = counter++;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CounterVariable other = (CounterVariable) obj;
		if (this.id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 31 * hash + this.id;
		return hash;
	}

	@Override
	public String toString() {
		return String.format("v%s", id);
	}
}
