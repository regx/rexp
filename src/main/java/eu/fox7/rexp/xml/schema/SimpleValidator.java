package eu.fox7.rexp.xml.schema;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.SymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.op.Evaluator;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.sdt.BottomUpIterator;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class SimpleValidator {
	public static final String ANYTYPE = "anyType";

	protected final XSchema schema;

	public static void main(String[] args) throws Exception {
		Log.configureRootLogger(Level.FINE);

		String schemaFileName = args.length > 0 ? args[0] : ".files/xml/xsd3.xsd";
		Xsd2XSchema schemaReader = new Xsd2XSchema();
		schemaReader.process(new FileInputStream(schemaFileName));
		XSchema schema = schemaReader.getResult();

		String docFileName = args.length > 1 ? args[1] : ".files/xml/xsd3.xml";
		Element root = XmlUtils.readXml(new FileInputStream(docFileName));

		SimpleValidator validator = new SimpleValidator(schema);
		System.out.println(String.format("Valid: %s", validator.validate(root)));
	}

	public SimpleValidator(XSchema schema) {
		this.schema = schema;
	}

	public boolean validate(Element element) {
		for (XSymbol rootSymbol : schema.getRootElements()) {
			String typeName = rootSymbol.getType();
			if (validate(element, typeName)) {
				return true;
			}
		}
		return false;
	}

	protected boolean validate(Element element, String typeName) {
		String elementName = element.getLocalName();

		Log.d("Examining %s#%s", elementName, typeName);
		RegExp cm = schema.getContent(typeName);

		List<XSymbol> symbols = new ArrayList<XSymbol>();
		Elements children = element.getChildElements();
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			String childElementName = child.getLocalName();
			String childTypeName = determineTypeNameFromContentModel(childElementName, cm);
			if (childTypeName == null) {
				return false;
			} else if (!validate(child, childTypeName)) {
				return false;
			} else {
				XSymbol symbol = new XSymbol(childElementName, childTypeName);
				symbols.add(symbol);
			}
		}
		if (ANYTYPE.equals(typeName)) {
			Log.d("Element %s is %s, skipping", elementName, typeName);
			return true;
		} else {
			if (symbols.size() > 0) {
				Word word = new SymbolWord(symbols);
				Log.d("Checking word %s for model %s", word, cm);
				boolean eval = Evaluator.eval(cm, word);
				return eval;
			} else {
				Log.d("Element %s with type %s has no children", elementName, typeName);
				return validateAttributes(element, typeName);
			}
		}
	}

	private static String determineTypeNameFromContentModel(String elementName, RegExp cm) {
		for (RegExp r : BottomUpIterator.iterable(cm)) {
			if (r instanceof ReSymbol) {
				ReSymbol rs = (ReSymbol) r;
				Symbol s = rs.getSymbol();
				if (s instanceof XSymbol) {
					XSymbol xs = (XSymbol) s;
					if (elementName.equals(xs.getName())) {
						return xs.getType();
					}
				} else {
					Log.w("Unexpected symbol type in model");
				}
			}
		}
		Log.w("Could not find type for element %s in %s", elementName, cm);
		return null;
	}

	protected boolean validateAttributes(Element element, String typeName) {
		Set<String> attrs = schema.getAttributes(typeName);
		if (attrs != null) {
			Set<String> elementAttrs = getElementAttributeNames(element);
			return attrs.containsAll(elementAttrs);
		}
		return false;
	}

	private static Set<String> getElementAttributeNames(Element element) {
		Set<String> attrNames = new HashSet<String>();
		for (int i = 0; i < element.getAttributeCount(); i++) {
			Attribute attr = element.getAttribute(i);
			attrNames.add(attr.getLocalName());
		}
		return attrNames;
	}
}
