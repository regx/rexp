package eu.fox7.rexp.xml;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.schema.cm.transducer.RegExpTransducer;
import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.Epsilon;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.extended.ArityN;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Transform;
import eu.fox7.rexp.xml.util.XmlUtils;
import eu.fox7.rexp.xml.util.iterators.ElementIterable;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import org.reflections.Reflections;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
public class RegExpXmlIo implements Transform<RegExp, RegExp> {
	public static void main(String[] args) {
		Director.setup();
		testDeserialize(testSerialize("a(b|c)d*e{1,3}gh"));
		testQuery("a(b|c)d*e{1,3}gh");
	}

	static String testSerialize(String regExpStr) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		Element e = serialize(re);
		String xmlStr = XmlUtils.elementToXmlString(e);
		Log.i("re -> xml \"%s\":\n%s", re, xmlStr);
		return xmlStr;
	}

	static RegExp testDeserialize(String xmlStr) {
		InputStream bis = new ByteArrayInputStream(xmlStr.getBytes());
		Element e = XmlUtils.readXml(bis);
		UtilX.silentClose(bis);
		RegExp re = deserialize(e, DEFAULT_TRANSFORM);
		Log.i("re <- xml \"%s\"", re);
		return re;
	}

	static Nodes testQuery(String regExpStr) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		Element e = serialize(re);
		String query = "//ReSymbol";
		Nodes qr = e.query(query);
		Log.i("re: %s, query: %s, result: %s", re, query, qr.size());
		return qr;
	}
	

	private static RegExpXmlIo INSTANCE = new RegExpXmlIo();

	public static RegExp apply(RegExp re) {
		return INSTANCE.transform(re);
	}

	@Override
	public RegExp transform(RegExp re) {
		return INSTANCE.transform(re);
	}
	

	private static final String RE_SYMBOL_VALUE = "value";
	private static final String COUNTER_MIN = "min";
	private static final String COUNTER_MAX = "max";

	public static Element serialize(RegExp re) {
		String name = re.getClass().getSimpleName();
		Element e = new Element(name);
		for (RegExp r : re) {
			Element c = serialize(r);
			e.appendChild(c);
		}

		if (re instanceof ReSymbol) {
			ReSymbol s = (ReSymbol) re;
			Attribute at = new Attribute(RE_SYMBOL_VALUE, s.toString());
			e.addAttribute(at);
		} else if (re instanceof Counter) {
			Counter r1 = (Counter) re;
			Attribute at1 = new Attribute(COUNTER_MIN, String.valueOf(r1.getMinimum()));
			e.addAttribute(at1);
			Attribute at2 = new Attribute(COUNTER_MAX, r1.getMaximumAsString());
			e.addAttribute(at2);
		}
		return e;
	}

	public static final Transform<Symbol, String> DEFAULT_TRANSFORM = new Transform<Symbol, String>() {
		@Override
		public Symbol transform(String data) {
			return new CharSymbol(data.charAt(0));
		}
	};

	public static RegExp deserialize(Element root, Transform<Symbol, String> transform) {
		String name = root.getLocalName();
		Log.d(name);

		String resVal = root.getAttributeValue(RE_SYMBOL_VALUE);
		String minStr = root.getAttributeValue(COUNTER_MIN);
		String maxStr = root.getAttributeValue(COUNTER_MAX);
		Log.v("%s %s %s", resVal, minStr, maxStr);

		List<RegExp> list = new LinkedList<RegExp>();
		for (Element e : ElementIterable.iterateElements(root)) {
			list.add(deserialize(e, transform));
		}

		Class<?> type = RE_SIMPLE_NAME_2_CLASS.get(name);
		if (ReSymbol.class.isAssignableFrom(type)) {
			if (resVal != null) {
				Symbol s = transform.transform(resVal);
				return new ReSymbol(s);
			}
		} else if (Counter.class.isAssignableFrom(type)) {
			if (minStr != null && maxStr != null) {
				RegExp r1 = list.get(0);
				int min = Counter.parseMinumumFromString(minStr);
				int max = Counter.parseMaximumFromString(maxStr);
				return new Counter(r1, min, max);
			}
		} else if (Binary.class.isAssignableFrom(type)) {
			RegExp r1 = list.get(0);
			RegExp r2 = list.get(1);
			return RegExpTransducer.make(type, r1, r2);
		} else if (Unary.class.isAssignableFrom(type)) {
			RegExp r1 = list.get(0);
			return RegExpTransducer.make(type, r1);
		} else if (ArityN.class.isAssignableFrom(type)) {
			RegExp[] ra = list.toArray(new RegExp[list.size()]);
			return RegExpTransducer.make(type, ra);
		}

		Log.w("Undefined XML to RegExp deserialization");
		return Epsilon.INSTANCE;
	}
	

	private static Map<String, Class<?>> RE_SIMPLE_NAME_2_CLASS;

	static {
		RE_SIMPLE_NAME_2_CLASS = new LinkedHashMap<String, Class<?>>();
		collectRegExpClassMapping();
	}

	private static void collectRegExpClassMapping() {
		String packageName = RegExp.class.getPackage().getName();
		int endIndex = packageName.lastIndexOf(".");
		packageName = packageName.substring(0, endIndex);
		Reflections ref = new Reflections(packageName);
		Set<Class<? extends RegExp>> s = ref.getSubTypesOf(RegExp.class);
		for (Class<? extends RegExp> c : s) {
			if ((c.getModifiers() & Modifier.ABSTRACT) == 0) {
				RE_SIMPLE_NAME_2_CLASS.put(c.getSimpleName(), c);
			}
		}
	}
}
