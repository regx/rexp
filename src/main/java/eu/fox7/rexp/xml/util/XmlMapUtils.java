package eu.fox7.rexp.xml.util;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.*;
import java.util.Map.Entry;
public class XmlMapUtils {
	public static final String ROOT_ELEMENT_NAME = "items";
	public static final String DEFAULT_ITEM_NAME = "item";
	

	public static abstract class XmlItemBuilder {
		private final String itemName;
		protected Element element;

		public XmlItemBuilder() {
			this(DEFAULT_ITEM_NAME);
		}

		public XmlItemBuilder(String name) {
			this.itemName = name;
		}

		public void init() {
			element = new Element(itemName);
		}

		public abstract void add(String name, String value);

		public abstract Element get();
	}

	public static class XmlItemElementBuilder extends XmlItemBuilder {
		public static final XmlItemElementBuilder INSTANCE = new XmlItemElementBuilder();

		@Override
		public void add(String name, String value) {
			Element child = new Element(name);
			child.appendChild(value);
			element.appendChild(child);
		}

		@Override
		public Element get() {
			return element;
		}
	}

	public static class XmlItemAttributeBuilder extends XmlItemBuilder {
		public static final XmlItemAttributeBuilder INSTANCE = new XmlItemAttributeBuilder();

		@Override
		public void add(String name, String value) {
			Attribute attribute = new Attribute(name, value);
			element.addAttribute(attribute);
		}

		@Override
		public Element get() {
			return element;
		}
	}

	public static interface XmlSerialiser<T> {
		Element toXml(T o);
	}

	public static class XmlAttributedItemSerializer<T> implements XmlSerialiser<T> {
		private final String name;

		public XmlAttributedItemSerializer(String name) {
			this.name = name;
		}

		@Override
		public Element toXml(T object) {
			Element element = new Element(DEFAULT_ITEM_NAME);
			Attribute attr = new Attribute(name, object.toString());
			element.addAttribute(attr);
			return element;
		}
	}

	public static class EntryXmlSerialiser<K, V> implements XmlSerialiser<Entry<K, V>> {
		protected final XmlItemBuilder builder;
		private final String keyName;
		private final String valueName;

		public EntryXmlSerialiser(XmlItemBuilder builder, String keyName, String valueName) {
			this.builder = builder;
			this.keyName = keyName;
			this.valueName = valueName;
		}

		@Override
		public Element toXml(Entry<K, V> entry) {
			builder.init();
			builder.add(keyName, String.valueOf(entry.getKey()));
			builder.add(valueName, String.valueOf(entry.getValue()));
			return builder.get();
		}
	}

	public static class MapXmlSerialiser<K, V> implements XmlSerialiser<Map<K, V>> {
		protected final XmlItemBuilder builder;

		public MapXmlSerialiser(XmlItemBuilder builder) {
			this.builder = builder;
		}

		@Override
		public Element toXml(Map<K, V> map) {
			builder.init();
			for (Entry<K, V> entry : map.entrySet()) {
				builder.add(entry.getKey().toString(), entry.getValue().toString());
			}
			return builder.get();
		}
	}
	

	public static interface Parser<V> {
		V parse(String s);
	}

	public static final Parser<String> STRING_PARSER = new Parser<String>() {
		@Override
		public String parse(String s) {
			return s;
		}
	};

	public static final Parser<Integer> INTEGER_PARSER = new Parser<Integer>() {
		@Override
		public Integer parse(String s) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				return 0;
			}
		}
	};
	

	public static <T> Element writeCollectionToXml(Collection<T> c, XmlSerialiser<T> serialiser) {
		Element root = new Element(ROOT_ELEMENT_NAME);
		for (T o : c) {
			root.appendChild(serialiser.toXml(o));
		}
		return root;
	}
	

	private static <K, V> Element writeMapToXml(Map<K, V> map, XmlItemBuilder builder, String keyName, String valueName) {
		EntryXmlSerialiser<K, V> serialiser = new EntryXmlSerialiser<K, V>(builder, keyName, valueName);
		return writeCollectionToXml(map.entrySet(), serialiser);
	}

	public static <K, V> Element writeMapToXmlElements(Map<K, V> map, String keyName, String valueName) {
		return writeMapToXml(map, XmlItemElementBuilder.INSTANCE, keyName, valueName);
	}

	public static <K, V> Element writeMapToXmlAttributes(Map<K, V> map, String keyName, String valueName) {
		return writeMapToXml(map, XmlItemAttributeBuilder.INSTANCE, keyName, valueName);
	}
	


	private static <K, V> Element writeMapsToXml(Collection<Map<K, V>> maps, XmlItemBuilder builder) {
		MapXmlSerialiser<K, V> serialiser = new MapXmlSerialiser<K, V>(builder);
		return writeCollectionToXml(maps, serialiser);
	}
	public static synchronized <K, V> Element writeMapsToXmlElements(Collection<Map<K, V>> maps) {
		return writeMapsToXml(maps, XmlItemElementBuilder.INSTANCE);
	}
	public static synchronized <K, V> Element writeMapsToXmlAttributes(Collection<Map<K, V>> maps) {
		return writeMapsToXml(maps, XmlItemAttributeBuilder.INSTANCE);
	}
	

	public static interface XmlDeserialiser<T> {
		T fromXml(Element e);
	}

	public static class Pair<K, V> implements Entry<K, V> {
		private final K key;
		private V value;

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			return this.value = value;
		}

		@Override
		public String toString() {
			return String.format("(%s, %s)", key, value);
		}
	}

	public static interface XmlDecomposer {
		Iterator<Pair<String, String>> iterator(Element element);
	}

	public static class XmlElementDecomposer implements XmlDecomposer {
		public static final XmlElementDecomposer INSTANCE = new XmlElementDecomposer();

		@Override
		public Iterator<Pair<String, String>> iterator(Element element) {
			final Elements fElements = element.getChildElements();
			return new Iterator<Pair<String, String>>() {
				private int currentIndex = 0;

				@Override
				public boolean hasNext() {
					return currentIndex < fElements.size();
				}

				@Override
				public Pair<String, String> next() {
					Element current = fElements.get(currentIndex++);
					return new Pair<String, String>(current.getLocalName(), current.getValue());
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported");
				}
			};
		}

	}

	public static class XmlAttributeDecomposer implements XmlDecomposer {
		public static final XmlAttributeDecomposer INSTANCE = new XmlAttributeDecomposer();

		@Override
		public Iterator<Pair<String, String>> iterator(Element element) {
			final Element fElement = element;
			return new Iterator<Pair<String, String>>() {

				private int currentIndex = 0;

				@Override
				public boolean hasNext() {
					return currentIndex < fElement.getAttributeCount();
				}

				@Override
				public Pair<String, String> next() {
					Attribute current = fElement.getAttribute(currentIndex++);
					return new Pair<String, String>(current.getLocalName(), current.getValue());
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported");
				}
			};
		}
	}

	public static class EntryXmlDeserialiser<K, V> implements XmlDeserialiser<Entry<K, V>> {
		private final XmlDecomposer decomposer;
		protected final Parser<K> kp;
		protected final Parser<V> vp;
		private final String keyName;
		private final String valueName;

		public EntryXmlDeserialiser(XmlDecomposer decomposer, Parser<K> kp, Parser<V> vp, String keyName, String valueName) {
			this.decomposer = decomposer;
			this.kp = kp;
			this.vp = vp;
			this.keyName = keyName;
			this.valueName = valueName;
		}

		@Override
		public Entry<K, V> fromXml(Element element) {
			K key = null;
			V val = null;
			Iterator<Pair<String, String>> it = decomposer.iterator(element);
			while (it.hasNext()) {
				Pair<String, String> current = it.next();
				String skey = current.getKey();
				String sval = current.getValue();
				if (keyName.equals(skey)) {
					key = kp.parse(sval);
				} else if (valueName.equals(skey)) {
					val = vp.parse(sval);
				}
			}
			return new Pair<K, V>(key, val);
		}
	}

	public static class MapXmlDeserialiser<K, V> implements XmlDeserialiser<Map<K, V>> {
		private final XmlDecomposer decomposer;
		protected final Parser<K> kp;
		protected final Parser<V> vp;

		public MapXmlDeserialiser(XmlDecomposer decomposer, Parser<K> kp, Parser<V> vp) {
			this.decomposer = decomposer;
			this.kp = kp;
			this.vp = vp;
		}

		@Override
		public Map<K, V> fromXml(Element element) {
			Map<K, V> map = new LinkedHashMap<K, V>();
			Iterator<Pair<String, String>> it = decomposer.iterator(element);
			while (it.hasNext()) {
				Pair<String, String> current = it.next();
				K key = kp.parse(current.getKey());
				V val = vp.parse(current.getValue());
				map.put(key, val);
			}
			return map;
		}
	}

	public static class XmlAttributedItemDeserializer<T> implements XmlDeserialiser<T> {
		private final String name;
		private final Parser<T> parser;

		public XmlAttributedItemDeserializer(String name, Parser<T> parser) {
			this.name = name;
			this.parser = parser;
		}

		@Override
		public T fromXml(Element e) {
			Attribute attr = e.getAttribute(name);
			if (attr != null) {
				return parser.parse(attr.getValue());
			} else {
				return null;
			}
		}
	}
	

	public static <T> void readCollectionFromXml(Element root, Collection<T> c, XmlDeserialiser<T> deserialiser) {
		Elements es = root.getChildElements();
		for (int i = 0; i < es.size(); i++) {
			Element element = es.get(i);
			c.add(deserialiser.fromXml(element));
		}
	}

	public static <T> void readCollectionFromXmlAttributes(Element root, Collection<T> c, String tagName, Parser<T> parser) {
		XmlDeserialiser<T> deserialiser = new XmlAttributedItemDeserializer<T>(tagName, parser);
		readCollectionFromXml(root, c, deserialiser);
	}

	public static void readCollectionFromXmlAttributes(Element root, Collection<String> c, String tagName) {
		XmlDeserialiser<String> deserialiser = new XmlAttributedItemDeserializer<String>(tagName, STRING_PARSER);
		readCollectionFromXml(root, c, deserialiser);
	}

	public static <K, V> void readMapFromXml(Element root, Map<K, V> map, EntryXmlDeserialiser<K, V> deserialiser) {
		Elements es = root.getChildElements();
		for (int i = 0; i < es.size(); i++) {
			Element element = es.get(i);
			Entry<K, V> entry = deserialiser.fromXml(element);
			map.put(entry.getKey(), entry.getValue());
		}
	}

	public static <K, V> void readMapFromXml(Element root, Map<K, V> map, XmlDecomposer decomposer, Parser<K> kp, Parser<V> vp, String keyName, String valueName) {
		EntryXmlDeserialiser<K, V> deserialiser = new EntryXmlDeserialiser<K, V>(decomposer, kp, vp, keyName, valueName);
		readMapFromXml(root, map, deserialiser);
	}

	public static void readMapFromXml(Element root, Map<String, String> map, XmlDecomposer decomposer, String keyName, String valueName) {
		EntryXmlDeserialiser<String, String> deserialiser = new EntryXmlDeserialiser<String, String>(decomposer, STRING_PARSER, STRING_PARSER, keyName, valueName);
		readMapFromXml(root, map, deserialiser);
	}

	public static void readMapFromXmlAttributes(Element root, Map<String, String> map, String keyName, String valueName) {
		XmlDecomposer decomposer = new XmlAttributeDecomposer();
		readMapFromXml(root, map, decomposer, keyName, valueName);
	}
	


	public static <K, V> void readMapsFromXml(Element root, Collection<Map<K, V>> maps, XmlDecomposer decomposer, Parser<K> kp, Parser<V> vp) {
		MapXmlDeserialiser<K, V> deserialiser = new MapXmlDeserialiser<K, V>(decomposer, kp, vp);
		readCollectionFromXml(root, maps, deserialiser);
	}

	public static void readMapsFromXml(Element root, Collection<Map<String, String>> maps, XmlDecomposer decomposer) {
		MapXmlDeserialiser<String, String> deserialiser = new MapXmlDeserialiser<String, String>(decomposer, STRING_PARSER, STRING_PARSER);
		readCollectionFromXml(root, maps, deserialiser);
	}

	public static void readMapsFromXmlElements(Element root, Collection<Map<String, String>> maps) {
		MapXmlDeserialiser<String, String> deserialiser = new MapXmlDeserialiser<String, String>(XmlElementDecomposer.INSTANCE, STRING_PARSER, STRING_PARSER);
		readCollectionFromXml(root, maps, deserialiser);
	}

	public static void readMapsFromXmlAttributes(Element root, Collection<Map<String, String>> maps) {
		MapXmlDeserialiser<String, String> deserialiser = new MapXmlDeserialiser<String, String>(XmlAttributeDecomposer.INSTANCE, STRING_PARSER, STRING_PARSER);
		readCollectionFromXml(root, maps, deserialiser);
	}
	

	public static class MapList<K, V> extends LinkedList<Map<K, V>> {
		private static final long serialVersionUID = 1L;

		public Map<K, V> newMap() {
			Map<K, V> map = new LinkedHashMap<K, V>();
			add(map);
			return map;
		}

		public boolean containsEntry(K key, V value) {
			for (Map<K, V> map : this) {
				if (map.containsKey(key)) {
					if (map.get(key).equals(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public static class StringMapList extends MapList<String, String> {
		private static final long serialVersionUID = 1L;
	}
}
