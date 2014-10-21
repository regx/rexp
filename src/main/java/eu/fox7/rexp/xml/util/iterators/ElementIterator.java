package eu.fox7.rexp.xml.util.iterators;

import nu.xom.Element;
import nu.xom.Elements;

import java.util.Iterator;

public class ElementIterator implements Iterator<Element> {
	private final Elements elements;
	private int index;

	public ElementIterator(Element element) {
		this.elements = element.getChildElements();
		index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < elements.size();
	}

	@Override
	public Element next() {
		return elements.get(index++);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported.");
	}

}
