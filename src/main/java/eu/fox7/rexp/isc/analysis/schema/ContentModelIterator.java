package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.xml.schema.XSchema;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class ContentModelIterator implements Iterator<ContentModelBundle> {
	private final XSchema schema;
	private final Object id;
	private final Iterator<Entry<String, RegExp>> it;
	private int num;

	public ContentModelIterator(XSchema schema, Object id) {
		this.schema = schema;
		this.id = id;
		num = 0;
		Set<Entry<String, RegExp>> entrySet = schema.getComplexRules().entrySet();
		it = entrySet.iterator();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public ContentModelBundle next() {
		Entry<String, RegExp> entry = it.next();
		return new ContentModelBundle(schema, id, entry.getKey(), entry.getValue(), num++);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported");
	}
}
