package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.sdt.SdtCheck;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.schema.XSchema;

public class SdtAnalyzer extends ContentModelAnalyzer {
	private static final String COL_SDT = "sdt";

	private SdtCheck sdt = new SdtCheck();

	@Override
	protected void process(XSchema schema, Object id, String loc, String typeName, RegExp contentModel, MapListHandle outputHandle) {
		boolean b = sdt.isStronglyDeterministic(contentModel);

		outputHandle.put(COL_SDT, Boolean.valueOf(b).toString());

		if (b) {
			Log.d("sdt re in %s: %s", id, contentModel);
		} else {
			Log.d("NON-sdt re in %s: %s", id, contentModel);
		}
	}

	@Override
	public String getJobMetaFileProperty() {
		return Director.PROP_SDT_RESULT;
	}
}
