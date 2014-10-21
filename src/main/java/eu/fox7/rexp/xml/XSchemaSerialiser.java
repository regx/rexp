package eu.fox7.rexp.xml;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.Epsilon;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.extended.*;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.schema.XStSymbol;
import eu.fox7.rexp.xml.schema.XSymbol;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class XSchemaSerialiser {
	private static final boolean COLLATE_COUNTER = true;

	private static final String ELMT_SCHEMA = "schema";
	private static final String ELMT_TYPE = "complexType";
	private static final String ELMT_SEQUENCE = "sequence";
	private static final String ELMT_CHOICE = "choice";
	private static final String ELMT_ALL = "all";
	private static final String ELMT_ELEMENT = "element";
	private static final String ELMT_COUNTER = "counter";

	private static final String ATTR_NAME = "name";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_LINE = "line";
	private static final String ATTR_MINOCCURS = "minOccurs";
	private static final String ATTR_MAXOCCURS = "maxOccurs";

	public static void main(String[] args) throws Exception {
		String fileName = args.length > 0 ? args[0] : ".files/xml/xsd2.xsd";
		Xsd2XSchema processor = new Xsd2XSchema();
		processor.process(new FileInputStream(fileName));
		XSchema schema = processor.getResult();
		Element element = schemaToXml(schema);
		XmlUtils.serializeXml(element, System.out);
		System.out.println("----");
		element = schemaToXml(schemaFromXml(element));
		XmlUtils.serializeXml(element, System.out);
	}
	

	public static Element schemaToXml(XSchema schema) {
		Element root = new Element(ELMT_SCHEMA);
		for (Entry<String, RegExp> rule : schema.getRules().entrySet()) {
			String typeName = rule.getKey();
			Element element = new Element(ELMT_TYPE);
			element.addAttribute(new Attribute(ATTR_NAME, typeName));
			RegExp cm = rule.getValue();
			Element cme = regExpToXml(cm);
			if (cme != null) {
				element.appendChild(cme);
				root.appendChild(element);
			}
			Integer n = schema.getMeta().getTypeLineNumber(typeName);
			if (n != null) {
				element.addAttribute(new Attribute(ATTR_LINE, n.toString()));
			}
		}
		return root;
	}

	public static Element regExpToXml(RegExp cm, Attribute... attributes) {
		if (cm instanceof ReSymbol) {
			ReSymbol rs = (ReSymbol) cm;
			Symbol s = rs.getSymbol();
			XSymbol xs = (XSymbol) s;
			if (xs instanceof XStSymbol) {
				return null;
			}
			Element element = new Element(ELMT_ELEMENT);
			element.addAttribute(new Attribute(ATTR_NAME, xs.getName()));
			element.addAttribute(new Attribute(ATTR_TYPE, xs.getType()));
			attachAttributes(element, attributes);
			return element;
		} else if (cm instanceof Counter) {
			Counter c = (Counter) cm;
			Attribute minAttr = new Attribute(ATTR_MINOCCURS, String.valueOf(c.getMinimum()));
			Attribute maxAttr = new Attribute(ATTR_MAXOCCURS, c.getMaximumAsString());
			if (COLLATE_COUNTER) {
				return regExpToXml(c.getFirst(), minAttr, maxAttr);
			} else {
				Element element = new Element(ELMT_COUNTER);
				element.addAttribute(minAttr);
				element.addAttribute(maxAttr);
				element.appendChild(regExpToXml(c.getFirst()));
				return element;
			}
		} else if (cm instanceof Sequence) {
			ArityN re = (ArityN) cm;
			return nArityToXml(ELMT_SEQUENCE, re, attributes);
		} else if (cm instanceof Choice) {
			ArityN re = (ArityN) cm;
			return nArityToXml(ELMT_CHOICE, re, attributes);
		} else if (cm instanceof All) {
			ArityN re = (ArityN) cm;
			return nArityToXml(ELMT_ALL, re, attributes);
		} else if (cm instanceof Epsilon) {
			return null;
		} else {
			throw new RuntimeException("Unsupported type: " + cm.getClass());
		}
	}

	private static Element nArityToXml(String name, ArityN re, Attribute... attributes) {
		Element element = new Element(name);
		attachAttributes(element, attributes);
		for (RegExp r : re) {
			element.appendChild(regExpToXml(r));
		}
		return element;
	}

	private static void attachAttributes(Element element, Attribute... attributes) {
		for (Attribute attr : attributes) {
			element.addAttribute(attr);
		}
	}
	

	public static XSchema schemaFromXml(Element root) {
		XSchema schema = new XSchema();
		Elements es = root.getChildElements();
		for (int i = 0; i < es.size(); i++) {
			Element element = es.get(i);
			if (ELMT_TYPE.equals(element.getLocalName())) {
				String typeName = element.getAttributeValue(ATTR_NAME);
				if (typeName != null) {
					if (element.getChildCount() == 1) {
						RegExp cm = regExpFromXml(element.getChildElements().get(0));
						schema.addComplexRule(typeName, cm);
					}
				}
			}
		}
		return schema;
	}

	public static RegExp regExpFromXml(Element element) {
		String elementName = element.getLocalName();
		RegExp re;
		if (ELMT_ELEMENT.equals(elementName)) {
			String name = element.getAttributeValue(ATTR_NAME);
			String type = element.getAttributeValue(ATTR_TYPE);
			re = new ReSymbol(new XSymbol(name, type));
		} else if (ELMT_COUNTER.equals(elementName)) {
			if (element.getChildCount() == 1) {
				re = regExpFromXml(element.getChildElements().get(0));
			} else {
				throw new RuntimeException("Unexpected number of children");
			}
		} else if (ELMT_SEQUENCE.equals(elementName)) {
			re = new Sequence(nArityFromXml(element.getChildElements()));
		} else if (ELMT_CHOICE.equals(elementName)) {
			re = new Choice(nArityFromXml(element.getChildElements()));
		} else if (ELMT_ALL.equals(elementName)) {
			re = new All(nArityFromXml(element.getChildElements()));
		} else {
			throw new RuntimeException("Unexpected element: " + elementName);
		}
		return wrapCounter(re, element);
	}

	private static RegExp[] nArityFromXml(Elements elements) {
		List<RegExp> list = new LinkedList<RegExp>();
		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);
			list.add(regExpFromXml(element));
		}
		RegExp[] result = new RegExp[list.size()];
		result = list.toArray(result);
		return result;
	}

	private static RegExp wrapCounter(RegExp re, Element element) {
		String minOccurs = element.getAttributeValue(ATTR_MINOCCURS);
		String maxOccurs = element.getAttributeValue(ATTR_MAXOCCURS);
		if (minOccurs != null && maxOccurs != null) {
			int min = Counter.parseMinumumFromString(minOccurs);
			int max = Counter.parseMaximumFromString(maxOccurs);
			if (min != Counter.PARSE_ERROR && max != Counter.PARSE_ERROR) {
				re = new Counter(re, min, max);
			}
		} else if (minOccurs != null) {
			int min = Counter.parseMinumumFromString(minOccurs);
			if (min != Counter.PARSE_ERROR) {
				re = new MinCounter(re, min);
			}
		} else if (maxOccurs != null) {
			int max = Counter.parseMaximumFromString(maxOccurs);
			if (max != Counter.PARSE_ERROR) {
				re = new MaxCounter(re, max);
			}
		}
		return re;
	}
}
