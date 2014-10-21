package eu.fox7.rexp.util;

public class RefHolder<T> {
	private T obj;

	public RefHolder(T obj) {
		this.obj = obj;
	}

	public T get() {
		return obj;
	}

	public void set(T obj) {
		this.obj = obj;
	}

	@Override
	public String toString() {
		return String.valueOf(obj);
	}

	@Override
	public int hashCode() {
		return obj != null ? obj.hashCode() : 0;
	}

	@Override
	public boolean equals(Object other) {
		return obj != null ? obj.equals(other) : other == obj;
	}
}
