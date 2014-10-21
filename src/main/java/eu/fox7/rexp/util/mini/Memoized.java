package eu.fox7.rexp.util.mini;

public class Memoized<S, T> implements Transform<S, T> {
	private S val;
	private final Transform<S, T> func;

	public Memoized(Transform<S, T> func) {
		this.func = func;
	}

	@Override
	public S transform(T data) {
		if (val == null) {
			val = func.transform(data);
		}
		return val;
	}
}
