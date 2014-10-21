package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Key;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import eu.fox7.rexp.xml.util.iterators.NodeIterable;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class CounterSummarizer {
	public static void main(String[] args) {
		Director.setup();
		CounterSummarizer cs = new CounterSummarizer();
		cs.apply("./analysis/counters.xml", "./analysis/counters2.xml");
	}

	public static CounterSummarizer INSTANCE = new CounterSummarizer();

	public void apply(String inputFileName, String outputFileName) {
		Comparator<String> strCmp = new StringAsNumberComparator();
		Comparator<Key<String>> keyCmp = new Key.KeyComparator<String>(strCmp);

		Map<Key<String>, Integer> schemaMinToCount = new TreeMap<Key<String>, Integer>(keyCmp);
		Map<Key<String>, Integer> schemaMaxToCount = new TreeMap<Key<String>, Integer>(keyCmp);
		File inputFile = FileX.newFile(inputFileName);
		Element inputRoot = XmlUtils.readXml(inputFile);
		Nodes items = inputRoot.query("item");
		for (Node node : NodeIterable.iterateNodes(items)) {
			Element item = (Element) node;
			Element schema = item.getFirstChildElement("schema");
			Element min = item.getFirstChildElement("min");
			Element max = item.getFirstChildElement("max");
			if (schema != null) {
				if (min != null) {
					Key<String> minKey = new Key<String>(schema.getValue(), min.getValue());
					UtilX.mapIncrement(schemaMinToCount, minKey, 1);
				}
				if (max != null) {
					Key<String> maxKey = new Key<String>(schema.getValue(), max.getValue());
					UtilX.mapIncrement(schemaMaxToCount, maxKey, 1);
				}
			}
		}
		Element minLocalElement = new Element("minlocal");
		for (Entry<Key<String>, Integer> entry : schemaMinToCount.entrySet()) {
			Key<String> key = entry.getKey();
			String schema = key.get(0);
			String min = key.get(1);
			Integer count = entry.getValue();
			Element element = new Element("item");
			element.appendChild(makeElem("schema", String.valueOf(schema)));
			element.appendChild(makeElem("min", String.valueOf(min)));
			element.appendChild(makeElem("count", String.valueOf(count)));
			minLocalElement.appendChild(element);
		}

		Element maxLocalElement = new Element("maxlocal");
		for (Entry<Key<String>, Integer> entry : schemaMaxToCount.entrySet()) {
			Key<String> key = entry.getKey();
			String schema = key.get(0);
			String max = key.get(1);
			Integer count = entry.getValue();
			Element element = new Element("item");
			element.appendChild(makeElem("schema", String.valueOf(schema)));
			element.appendChild(makeElem("max", String.valueOf(max)));
			element.appendChild(makeElem("count", String.valueOf(count)));
			maxLocalElement.appendChild(element);
		}
		Map<String, Integer> minToCount = new TreeMap<String, Integer>(strCmp);
		for (Entry<Key<String>, Integer> entry : schemaMinToCount.entrySet()) {
			String key = entry.getKey().get(1);
			UtilX.mapIncrement(minToCount, key, entry.getValue());
		}
		Element minGlobalElement = XmlMapUtils.writeMapToXmlElements(minToCount, "min", "count");
		minGlobalElement.setLocalName("minglobal");

		Map<String, Integer> maxToCount = new TreeMap<String, Integer>(strCmp);
		for (Entry<Key<String>, Integer> entry : schemaMaxToCount.entrySet()) {
			String key = entry.getKey().get(1);
			UtilX.mapIncrement(maxToCount, key, entry.getValue());
		}
		Element maxGlobalElement = XmlMapUtils.writeMapToXmlElements(maxToCount, "max", "count");
		maxGlobalElement.setLocalName("maxglobal");
		Element outputRoot = new Element("items");
		outputRoot.appendChild(minLocalElement);
		outputRoot.appendChild(maxLocalElement);
		outputRoot.appendChild(minGlobalElement);
		outputRoot.appendChild(maxGlobalElement);
		File outputFile = FileX.newFile(outputFileName);
		XmlUtils.serializeXml(outputRoot, outputFile);
	}
	

	public static Element makeElem(String name, String content) {
		Element e = new Element(name);
		e.appendChild(content);
		return e;
	}

	public static class StringAsNumberComparator implements Comparator<String> {
		@Override
		public int compare(String a, String b) {
			if (a != null && b != null) {
				try {
					int x = Integer.parseInt(a);
					int y = Integer.parseInt(b);
					if (x == y) {
						return 0;
					} else {
						return x < y ? -1 : 1;
					}
				} catch (NumberFormatException ex) {
					return a.compareTo(b);
				}
			}
			return String.valueOf(a).compareTo(String.valueOf(b));
		}
	}
}
