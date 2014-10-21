package eu.fox7.rexp.xml.schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class XSchemaMeta {
	protected Map<String, Integer> type2meta;
	protected Map<String, Integer> element2meta;

	public XSchemaMeta() {
		type2meta = new LinkedHashMap<String, Integer>();
		element2meta = new LinkedHashMap<String, Integer>();
	}

	public void putTypeMeta(String typeName, int lineNumber) {
		type2meta.put(typeName, lineNumber);
	}

	public void putElementMeta(String elementName, int lineNumber) {
		element2meta.put(elementName, lineNumber);
	}

	public Integer getTypeLineNumber(String typeName) {
		return type2meta.containsKey(typeName) ? type2meta.get(typeName) : null;
	}

	public Integer getElementLineNumber(String elementName) {
		return element2meta.containsKey(elementName) ? element2meta.get(elementName) : null;
	}
}
