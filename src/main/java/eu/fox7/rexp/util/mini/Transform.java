package eu.fox7.rexp.util.mini;

public interface Transform<S, T> {
	S transform(T data);
}
