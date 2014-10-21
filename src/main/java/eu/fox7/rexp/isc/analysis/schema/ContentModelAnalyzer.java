package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.schema.XSchemaMeta;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import nu.xom.Element;

import java.util.Map;
import java.util.Map.Entry;
public abstract class ContentModelAnalyzer extends SchemaAnalysis {
	public static final String COL_DOC = "schema";
	public static final String COL_EXP = "exp";

	public static void iterate(final ContentModelAnalyzer cma, final XSchema schema, final Object id) {
		for (Entry<String, RegExp> rule : schema.getRules().entrySet()) {
			RegExp contentModel = rule.getValue();
			final String typeName = rule.getKey();
			final String locator = Integer.toString(schema.getMeta().getTypeLineNumber(typeName));

			final MapListHandle outputHandle = new MapListHandle(cma.mapList);
			outputHandle.setNextClosure(new Runnable() {
				@Override
				public void run() {
					Map<String, String> map = cma.mapList.newMap();
					outputHandle.setMap(map);
					map.put(COL_DOC, id.toString());
					map.put(COL_EXP, locator);
				}
			});
			outputHandle.next();

			cma.process(schema, id, locator, typeName, contentModel, outputHandle);
		}
	}

	protected XmlMapUtils.MapList<String, String> mapList;

	public ContentModelAnalyzer() {
		mapList = new XmlMapUtils.MapList<String, String>();
	}

	@Override
	public void process(XSchema schema, Object id) {
		iterate(this, schema, id);
	}

	protected abstract void process(XSchema schema, Object id, String loc, String typeName, RegExp contentModel, MapListHandle mapListHandle);

	public void process(final ContentModelBundle bundle) {
		final Object id = bundle.getId();
		final XSchema schema = bundle.getSchema();
		RegExp contentModel = bundle.getContentModel();
		final String typeName = bundle.getTypeName();
		final String locator = getLineNumberString(schema, typeName);

		final MapListHandle outputHandle = new MapListHandle(mapList);
		outputHandle.setNextClosure(new Runnable() {
			@Override
			public void run() {
				Map<String, String> map = mapList.newMap();
				outputHandle.setMap(map);
				map.put(COL_DOC, id.toString());
				map.put(COL_EXP, locator);
			}
		});
		outputHandle.next();
		process(schema, id, locator, typeName, contentModel, outputHandle);
	}

	private static String getLineNumberString(XSchema schema, String typeName) {
		XSchemaMeta meta = schema.getMeta();
		Integer i = meta.getTypeLineNumber(typeName);
		if (i != null) {
			return Integer.toString(i);
		} else {
			Log.w("%s has no line number", typeName);
			return "";
		}
	}

	@Override
	public void fromXml(Element root) {
		XmlMapUtils.readMapsFromXmlElements(root, mapList);
	}

	@Override
	public Element toXml() {
		return XmlMapUtils.writeMapsToXmlElements(mapList);
	}

	@Override
	public String getJobMetaFileProperty() {
		return Director.PROP_SDT_RESULT;
	}
}
