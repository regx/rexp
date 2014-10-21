package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.util.mini.IntIterator;

import java.util.Iterator;

public class BasicInputs implements Iterable<BasicInput> {
	private final int low;
	private final int high;
	private final int stepSize;

	public BasicInputs(int low, int high, int stepSize) {
		this.low = low;
		this.high = high;
		this.stepSize = stepSize;
	}

	@Override
	public Iterator<BasicInput> iterator() {
		return new IntIterator<BasicInput>(low, high, stepSize) {
			@Override
			protected BasicInput wrap(int i) {
				return BasicInput.make(i);
			}
		};
	}
}
