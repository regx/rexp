package eu.fox7.rexp.isc.cnfa.update;

import eu.fox7.rexp.isc.cnfa.core.Valuation;

import java.util.*;

public class UpdateSet implements Update, Iterable<Update> {
	public static final Update SKIP = new UpdateSet();

	protected Set<Update> set;

	public static UpdateSet make(Update... updates) {
		Set<Update> set = new LinkedHashSet<Update>();
		for (Update update : updates) {
			if (update instanceof UpdateSet) {
				if (((UpdateSet) update).size() > 0) {
					set.add(update);
				}
			} else {
				set.add(update);
			}
		}
		return new UpdateSet(set.toArray(new Update[set.size()]));
	}

	private UpdateSet(Update... updates) {
		this.set = new HashSet<Update>(Arrays.asList(updates));
	}

	@Override
	public Iterator<Update> iterator() {
		return set.iterator();
	}

	public int size() {
		return set.size();
	}

	@Override
	public void applyTo(Valuation vo) {
		for (Update u : set) {
			u.applyTo(vo);
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
		final UpdateSet other = (UpdateSet) obj;
		if (this.set != other.set && (this.set == null || !this.set.equals(other.set))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + (this.set != null ? this.set.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean touched = false;
		sb.append("{");
		for (Update u : set) {
			if (touched) {
				sb.append(", ");
			}
			sb.append(u);
			touched = true;
		}
		sb.append("}");
		return sb.toString();
	}
}
