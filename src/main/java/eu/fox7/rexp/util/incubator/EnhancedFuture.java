package eu.fox7.rexp.util.incubator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class EnhancedFuture<T> implements Future<T> {
	private final Future<T> future;

	public EnhancedFuture(Future<T> future) {
		this.future = future;

	}

	public T uncheckedGet() {
		try {
			return get();
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (ExecutionException ex) {
			throw new RuntimeException(ex);
		}
	}

	public T getOrElse(T value) {
		try {
			return get();
		} catch (InterruptedException ex) {
			return value;
		} catch (ExecutionException ex) {
			return value;
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}
}
