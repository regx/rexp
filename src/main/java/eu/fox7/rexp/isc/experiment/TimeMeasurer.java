package eu.fox7.rexp.isc.experiment;

public class TimeMeasurer implements Measurer {
	private long val1;
	private long val2;

	@Override
	public void begin() {
		MemoryMeasurer.gc();
		val1 = System.nanoTime();
	}

	@Override
	public void end() {
		val2 = System.nanoTime();
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
