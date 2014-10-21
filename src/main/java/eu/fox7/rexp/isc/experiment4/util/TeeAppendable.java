package eu.fox7.rexp.isc.experiment4.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TeeAppendable implements Appendable, Closeable {
	private List<Appendable> wrapped;

	public TeeAppendable(List<Appendable> inputs) {
		this.wrapped = inputs;
	}

	public TeeAppendable(Appendable... inputs) {
		this.wrapped = Arrays.asList(inputs);
	}

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		for (Appendable o : wrapped) {
			o.append(csq);
		}
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		for (Appendable o : wrapped) {
			o.append(csq, start, end);
		}
		return this;
	}

	@Override
	public Appendable append(char c) throws IOException {
		for (Appendable o : wrapped) {
			o.append(c);
		}
		return this;
	}

	@Override
	public void close() throws IOException {
		for (Appendable o : wrapped) {
			if (o instanceof Closeable) {
				Closeable c = (Closeable) o;
				c.close();
			}
		}
	}
}
