package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.File;
import java.io.OutputStream;
import java.util.*;

public class ResultHolder {

	public static interface ResultAdder {
		void add(Map<String, String> map);
	}

	public static ResultAdder pairResultAdder(final Pair<Object> pair) {
		return new ResultAdder() {
			@Override
			public void add(Map<String, String> map) {
				map.put(pair.getFirst().toString(), pair.getSecond().toString());
			}
		};
	}

	private static boolean verbose = false;

	private ResultAdder resultAdder;


	public static void main(String[] args) {
		ResultHolder r = new ResultHolder();
		r.write(System.out);
		System.out.println(r);
	}

	public static void setVerbose(boolean verbose) {
		ResultHolder.verbose = verbose;
	}

	protected List<Map<String, String>> mapList;

	public ResultHolder() {
		mapList = new LinkedList<Map<String, String>>();
	}

	public Map<String, String> createResult() {
		logLast();
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (resultAdder != null) {
			resultAdder.add(map);
		}
		mapList.add(map);
		return map;
	}

	public void setResultMeta(ResultAdder resultAdder) {
		this.resultAdder = resultAdder;
	}

	protected Element toXmlElement() {
		return XmlMapUtils.writeMapsToXmlElements(mapList);
	}

	public void write(OutputStream os) {
		Element e = toXmlElement();
		XmlUtils.serializeXml(e, System.out);
	}

	public void write(File file) {
		Element e = toXmlElement();
		XmlUtils.serializeXml(e, file);
	}

	protected void fromXmlElement(Element root) {
		XmlMapUtils.readMapsFromXmlElements(root, mapList);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		CsvUtil.print(sb, mapList);
		return sb.toString();
	}

	private void logLast() {
		if (verbose && mapList.size() > 0) {
			ListIterator<Map<String, String>> it = mapList.listIterator();
			Map<String, String> current = null;
			while (it.hasNext()) {
				current = it.next();
			}
			if (current != null) {
				StringBuilder sb = new StringBuilder();
				CsvUtil.print(sb, mapList);
				Log.i("%s", sb);
			}
		}
	}

	public List<Map<String, String>> getMapList() {
		return mapList;
	}
}
