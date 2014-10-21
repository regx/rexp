package eu.fox7.rexp.isc.experiment;

import java.lang.ref.WeakReference;

public class MemoryMeasurer implements Measurer {
	private static final Runtime runtime = Runtime.getRuntime();

	public static long getUsedMemory() {
		return runtime.totalMemory() - runtime.freeMemory();
	}

	public static void gc() {
		Object obj = new Object();
		WeakReference<?> ref = new WeakReference<Object>(obj);
		obj = null;
		while (ref.get() != null) {
			System.gc();
		}
	}


	private long val1;
	private long val2;

	@Override
	public void begin() {
		gc();
		val1 = getUsedMemory();
	}

	@Override
	public void end() {
		val2 = getUsedMemory();
	}

	@Override
	public long result() {
		return val2 - val1;
	}

	@Override
	public boolean processResults(Object object, int min, int max) {
		return false;
	}
}
