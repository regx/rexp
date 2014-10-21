package eu.fox7.rexp.tree.nfa.lw;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class NfaState implements Serializable {
	private static final long serialVersionUID = 1L;
	private static AtomicInteger atomicCounter = new AtomicInteger();
	private int id;
	public static void resetCounter() {
		atomicCounter.set(0);
	}

	public NfaState() {
		id = atomicCounter.getAndIncrement();
	}

	@Override
	public String toString() {
		return "q" + id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NfaState other = (NfaState) obj;
		if (this.id != other.id) {
			return false;
		}
		return true;
	}

	public int getId() {
		return id;
	}
}
