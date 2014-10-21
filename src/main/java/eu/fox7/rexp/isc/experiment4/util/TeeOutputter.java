package eu.fox7.rexp.isc.experiment4.util;

public class TeeOutputter implements Outputter {
	private final Outputter[] outputters;

	public TeeOutputter(Outputter... outputters) {
		this.outputters = outputters;
	}

	@Override
	public Outputter put(Object key, Object value) {
		for (Outputter outputter : outputters) {
			outputter.put(key, value);
		}
		return this;
	}

	@Override
	public void flush() {
		for (Outputter outputter : outputters) {
			outputter.flush();
		}
	}
}
