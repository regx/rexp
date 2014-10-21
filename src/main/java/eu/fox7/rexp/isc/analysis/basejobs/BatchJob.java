package eu.fox7.rexp.isc.analysis.basejobs;

import eu.fox7.rexp.isc.analysis.basejobs.BatchJob.Item;
import eu.fox7.rexp.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.UUID;

public abstract class BatchJob<T extends Item<?>> extends Job {
	public static class Item<T> {
		private final T item;

		public Item(T item) {
			this.item = item;
		}

		public T get() {
			return item;
		}

		public static <T> Iterator<Item<T>> wrap(final Iterator<T> iterator) {
			return new Iterator<Item<T>>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public Item<T> next() {
					return new Item<T>(iterator.next());
				}

				@Override
				public void remove() {
					iterator.remove();
				}
			};
		}

		@Override
		public String toString() {
			return String.valueOf(item);
		}
	}

	public static String getId() {
		return UUID.randomUUID().toString();
	}

	private T current;
	private boolean skipable;
	private boolean cancelled;

	public BatchJob() {
		super();
		this.skipable = true;
	}

	public void setSkipable(boolean skip) {
		this.skipable = skip;
	}

	@Override
	protected void work() {
		cancelled = false;
		Iterator<T> iterator = iterateItems();
		if (skipable) {
			skipToCurrent(iterator);
		}
		while (!cancelled && isRunnable() && iterator.hasNext()) {
			current = iterator.next();
			try {
				process(current);
			} catch (RuntimeException ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				Log.e("Job aborted: %s", sw.toString());
			}
		}
	}

	protected void cancel() {
		cancelled = true;
	}

	private void skipToCurrent(Iterator<T> iterator) {
		if (current != null) {
			while (iterator.hasNext() && !iterator.next().equals(current)) {
			}
		}
	}

	protected abstract Iterator<T> iterateItems();

	protected abstract void process(T item);
}
