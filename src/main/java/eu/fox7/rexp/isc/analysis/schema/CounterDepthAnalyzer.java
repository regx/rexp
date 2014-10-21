package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.corejobs.SchemaAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.cm.RegExpProperties;
import eu.fox7.rexp.op.NestingDepth;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.schema.XSchema;

import java.io.File;

public class CounterDepthAnalyzer extends ContentModelAnalyzer {
	private static final String COL_DEPTH = "depth";
	private static final boolean extend = true;
	private static final String COL_NT_DEPTH = "ntdepth";
	private static final String COL_STAR_DEPTH = "stardepth";
	private static final String COL_COMBINED_DEPTH = "combineddepth";
	private static final String COL_PARSE_DEPTH = "parsedepth";

	private NestingDepth nd = new NestingDepth(Counter.class);
	private NestingDepth ndNt = new NestingDepth(RegExpProperties.IS_NT_COUNTER);
	private NestingDepth ndSl = new NestingDepth(RegExpProperties.IS_STAR_LIKE);
	private NestingDepth ndCb = new NestingDepth(RegExpProperties.IS_STAR_LIKE_OR_NT_COUNTER);
	private NestingDepth ndPr = new NestingDepth(RegExpProperties.TRUE);

	@Override
	protected void process(XSchema schema, Object id, String loc, String typeName, RegExp contentModel, MapListHandle outputHandle) {
		if (!RegExpProperties.HAS_NT_COUNTER.transform(contentModel)) {
			return;
		}
		int depth = nd.calculateNestingDepth(contentModel);
		if (depth > 2) {
			Log.i("Doc=%s, Depth=%s, cm=%s", id, depth, contentModel);
		} else {
			Log.d("Doc=%s, Depth=%s, cm=%s", id, depth, contentModel);
		}
		outputHandle.put(COL_DEPTH, Integer.toString(depth));
		if (extend) {
			process(outputHandle, COL_NT_DEPTH, ndNt, id, loc, contentModel, 0);
			process(outputHandle, COL_STAR_DEPTH, ndSl, id, loc, contentModel, 4);
			process(outputHandle, COL_COMBINED_DEPTH, ndCb, loc, id, contentModel, 0);
			contentModel = new eu.fox7.rexp.isc.analysis.schema.cm.RegExpFlattener().flatten(contentModel);
			process(outputHandle, COL_PARSE_DEPTH, ndPr, id, loc, contentModel, 13);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		sb = new StringBuilder();
	}

	@Override
	public void onStop() {
		File file = FileX.newFile(Director.resolve(SchemaAnalyzer.PROP_ANALYSIS_DEEP));
		FileX.stringToFile(sb.toString(), file);
		super.onStop();
	}

	private StringBuilder sb;

	private int process(MapListHandle outputHandle, String colName, NestingDepth nd, Object id, Object loc, RegExp contentModel, int logThreshold) {
		int d = nd.calculateNestingDepth(contentModel);
		outputHandle.put(colName, Integer.toString(d));
		if (logThreshold > 0 && d >= logThreshold) {
			sb.append(String.format("DEEP: %s %s file -> %s", colName, d, id));
			sb.append(CsvUtil.newLine);
			sb.append(String.format("DEEP: %s %s line -> %s", colName, d, loc));
			sb.append(CsvUtil.newLine);
			sb.append(String.format("DEEP: %s %s expr -> %s", colName, d, contentModel));
			sb.append(CsvUtil.newLine);
			sb.append(CsvUtil.newLine);
		}
		return d;
	}

	@Override
	public String getJobMetaFileProperty() {
		return Director.PROP_CDEPTH_RESULT;
	}
}
