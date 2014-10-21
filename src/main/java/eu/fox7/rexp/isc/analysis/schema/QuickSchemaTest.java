package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.isc.analysis.corejobs.SchemaAnalyzer;
import eu.fox7.rexp.op.NestingDepth;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.sdt.BottomUpIterator;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.PrettyPrinter;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.url.resolver.EmptySchemaResolver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

public class QuickSchemaTest extends SchemaAnalysis {
	public static void main(String[] args) {
		setup();

		EmptySchemaResolver resolver = new EmptySchemaResolver();

		SchemaAnalyzer sa = new SchemaAnalyzer(new QuickSchemaTest());
		sa.setJobInputDirectoryPath(resolve(PROP_XSD_DIR));
		sa.execute();

		resolver.unregister();
	}

	@Override
	public void process(XSchema schema, Object id) {
		for (Entry<String, RegExp> rule : schema.getRules().entrySet()) {
			String typeName = rule.getKey();
			RegExp contentModel = rule.getValue();

			NestingDepth ndr = new NestingDepth(RegExp.class);
			int rDepth = ndr.calculateNestingDepth(contentModel);

			NestingDepth ndc = new NestingDepth(Counter.class);
			int cDepth = ndc.calculateNestingDepth(contentModel);

			int high = findHighestCounter(contentModel);

			Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("doc", id);
			map.put(typeName, contentModel);
			map.put("high", high);
			map.put("depth", rDepth);
			map.put("cdepth", cDepth);
			Log.i(PrettyPrinter.toString(map));
		}
	}

	private static int findHighestCounter(RegExp contentModel) {
		int m = -1;
		for (RegExp re : BottomUpIterator.iterable(contentModel)) {
			if (re instanceof Counter) {
				Counter c = (Counter) re;
				int min = c.getMinimum();
				int max = c.getMaximum();
				if (min > m) {
					m = min;
				}
				if (max > m) {
					m = max;
				}
			}
		}
		return m;
	}
}
