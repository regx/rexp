package eu.fox7.rexp.xml.schema;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.xml.util.XmlUtils;
import eu.fox7.rexp.xml.util.iterators.ElementIterable;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XsdObfuscator {
	public static void main(String[] args) {
		Director.setup();
		String[] fileNames = {
			"D:/home/dev/data/.rexp/xsd/cache/ddmsence.googlecode.com/svn-history/r746/trunk/data/schemas/3.1/DDMS/gml.xsd",
		};
		String fileName = fileNames[0];
		File file = new File(fileName);
		Element e = XmlUtils.readXml(file);

		XsdObfuscator o = new XsdObfuscator();
		o.obfuscate(e);

		XmlUtils.serializeXml(e, System.out);
	}

	private Map<String, String> names;
	private int nameCount;

	public XsdObfuscator() {
		names = new LinkedHashMap<String, String>();
		nameCount = 0;
	}

	public void obfuscate(Element e) {
		collectNames(e);
		morphRefs(e);
	}

	private void collectNames(Element element) {
		for (Element e : ElementIterable.iterateElements(element)) {
			Attribute attName = e.getAttribute("name");
			if (attName != null) {
				String clearVal = attName.getValue();
				String obfcVal;
				if (names.containsKey(clearVal)) {
					obfcVal = names.get(clearVal);
				} else {
					obfcVal = "a" + nameCount++;
					names.put(clearVal, obfcVal);
				}

				attName.setValue(obfcVal);
			}
			collectNames(e);
		}
	}

	void collectNamespaces(Element element) {
		int n = element.getNamespaceDeclarationCount();
		for (int i = 0; i < n; i++) {
			String prefix = element.getNamespacePrefix(i);
			String uri = element.getNamespaceURI(prefix);
			names.put(prefix, uri);
		}
	}

	void deleteComments(Node node) {
		int n = node.getChildCount();
		List<Node> deletable = new LinkedList<Node>();
		for (int i = 0; i < n; i++) {
			Node child = node.getChild(i);
			if (child instanceof nu.xom.Comment) {
				deletable.add(node);
			} else {
				deleteComments(child);
			}
		}
		for (Node node2 : deletable) {
			node2.detach();
		}
	}

	private void morphRefs(Element element) {
		for (Element e : ElementIterable.iterateElements(element)) {
			morphNames(e, "type");
			morphNames(e, "ref");
			morphNames(e, "base");
			morphNames(e, "itemType");
			morphRefs(e);
		}
	}

	private void morphNames(Element e, String attName) {
		Attribute attType = e.getAttribute(attName);
		if (attType != null) {
			String origVal = attType.getValue();
			origVal = origVal.replaceFirst("[^:]*:", "");
			if (names.containsKey(origVal)) {
				String obfcVal = attType.getValue().replace(origVal, names.get(origVal));
				attType.setValue(obfcVal);
			}
		}
	}
}
