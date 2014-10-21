package eu.fox7.rexp.isc.experiment2.measure;

import java.util.Map;

public class TimeMeasurer implements Measurer {
	public static String KEY_TIME_MEASURER = "nanoseconds";
	protected long val1;
	protected long val2;

	@Override
	public void begin() {
		MemoryMeasurer.cleanJvm();
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

	public void beginTimeWithoutClean() {
		val1 = System.nanoTime();
	}

	public void beginTime() {
		this.begin();
	}

	public void endTime() {
		this.end();
	}

	public long returnBeginTimeWithoutClean() {
		val1 = System.nanoTime();
		return val1;
	}

	public long returnBeginTime() {
		this.begin();
		return val1;
	}

	public long returnEndTime() {
		this.end();
		return val2;
	}

	public long resultTime() {
		return result();
	}

	@Override
	public void writeResult(Map<String, String> outputMap) {
		outputMap.put(KEY_TIME_MEASURER, String.valueOf(result()));
	}

	@Override
	public void setContext(Object context) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
