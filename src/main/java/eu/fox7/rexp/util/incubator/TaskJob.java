package eu.fox7.rexp.util.incubator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class TaskJob<T> {
	private final ExecutorService owner;
	private final Callable<T> callable;
	private EnhancedFuture<T> future;

	public TaskJob(ExecutorService owner, Callable<T> callable) {
		this.owner = owner;
		this.callable = callable;
	}

	public synchronized EnhancedFuture<T> submit() {
		future = new EnhancedFuture<T>(owner.submit(callable));
		return future;
	}

	public synchronized void cancel() {
		if (future != null) {
			future.cancel(true);
		}
	}

	public synchronized boolean isDone() {
		return future.isDone();
	}

	public synchronized boolean isCancelled() {
		return future.isCancelled();
	}
}