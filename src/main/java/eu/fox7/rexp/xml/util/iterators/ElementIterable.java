package eu.fox7.rexp.xml.util.iterators;

import nu.xom.Element;

import java.util.Iterator;

public class ElementIterable implements Iterable<Element> {
	private final Element element;

	public ElementIterable(Element element) {
		this.element = element;
	}

	@Override
	public Iterator<Element> iterator() {
		return new ElementIterator(element);
	}

	public static ElementIterable iterateElements(Element element) {
		return new ElementIterable(element);
	}
}
