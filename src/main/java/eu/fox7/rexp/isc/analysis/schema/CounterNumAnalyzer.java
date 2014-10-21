package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.extended.MaxCounter;
import eu.fox7.rexp.regexp.core.extended.MinCounter;
import eu.fox7.rexp.sdt.BottomUpIterator;
import eu.fox7.rexp.xml.schema.XSchema;

public class CounterNumAnalyzer extends ContentModelAnalyzer {
	private static final String COL_MIN = "min";
	private static final String COL_MAX = "max";
	private static final String COUNTER_ID = "counter";
	private static final String COUNTER_UNBOUNDED_STRING = "unbounded";
	private static final String COUNTER_UNDEFINED_STRING = "undefined";
	private static final int UNDEFINED_COUNTER = -2;

	@Override
	protected void process(XSchema schema, Object id, String loc, String typeName, RegExp contentModel, MapListHandle outputHandle) {
		int counterId = 0;
		for (RegExp re : BottomUpIterator.iterable(contentModel)) {
			if (re instanceof Counter) {
				if (counterId > 0) {
					outputHandle.next();
				}

				Counter c = (Counter) re;
				int min = c.getMinimum();
				int max = c.getMaximum();

				if (c instanceof MinCounter) {
					max = UNDEFINED_COUNTER;
				} else if (c instanceof MaxCounter) {
					min = UNDEFINED_COUNTER;
				}
				outputHandle.put(COL_MIN, formatCounterAsString(min));
				outputHandle.put(COL_MAX, formatCounterAsString(max));
				outputHandle.put(COUNTER_ID, String.valueOf(counterId++));
			}
		}
	}

	private static String formatCounterAsString(int num) {
		if (num >= 0) {
			return Integer.toString(num);
		} else {
			if (num == Counter.INFINITY) {
				return COUNTER_UNBOUNDED_STRING;
			} else {
				return COUNTER_UNDEFINED_STRING;
			}
		}
	}

	@Override
	public String getJobMetaFileProperty() {
		return Director.PROP_CNUM_RESULT;
	}
}
