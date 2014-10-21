package eu.fox7.rexp.xml.schema;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.util.PrettyPrinter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class XSchema {
	protected Map<String, RegExp> type2content;
	protected Map<String, RegExp> ctype2content;
	protected Map<String, RegExp> stype2content;
	protected Map<String, String> element2type;
	protected Map<String, Set<String>> type2attributess;
	protected Set<String> complexTypeNames;
	protected Set<XSymbol> rootElements;
	protected boolean error;

	protected XSchemaMeta meta;

	public XSchema() {
		type2content = new LinkedHashMap<String, RegExp>();
		ctype2content = new LinkedHashMap<String, RegExp>();
		stype2content = new LinkedHashMap<String, RegExp>();
		element2type = new LinkedHashMap<String, String>();
		rootElements = new LinkedHashSet<XSymbol>();

		complexTypeNames = new LinkedHashSet<String>();
		error = false;
		type2attributess = new LinkedHashMap<String, Set<String>>();

		meta = new XSchemaMeta();
	}

	public void addComplexRule(String typeName, RegExp contentModel) {
		ctype2content.put(typeName, contentModel);
		type2content.put(typeName, contentModel);
	}

	public void addSimpleRule(String typeName, RegExp contentModel) {
		stype2content.put(typeName, contentModel);
		type2content.put(typeName, contentModel);
	}

	public boolean hasComplexType(String typeName) {
		return complexTypeNames.contains(typeName);
	}

	public void markComplexType(String typeName) {
		complexTypeNames.add(typeName);
	}

	public boolean hasSimplexType(String typeName) {
		return type2content.containsKey(typeName);
	}

	public RegExp getContent(String typeName) {
		return type2content.get(typeName);
	}

	public void addElement(String elementName, String typeName) {
		element2type.put(elementName, typeName);
		rootElements.add(new XSymbol(elementName, typeName));
	}

	public boolean hasElement(String elementName) {
		return element2type.containsKey(elementName);
	}

	public String getElementTypeName(String elementName) {
		return element2type.get(elementName);
	}

	public Map<String, RegExp> getRules() {
		return type2content;
	}

	public Map<String, RegExp> getComplexRules() {
		return ctype2content;
	}

	public Set<XSymbol> getRootElements() {
		return rootElements;
	}

	public boolean hasErrorOccured() {
		return error;
	}

	public void flagError() {
		error = true;
	}

	public void addAttribute(String typeName, String attributeName) {
		Set<String> set = type2attributess.containsKey(typeName)
			? type2attributess.get(attributeName)
			: new LinkedHashSet<String>();
		type2attributess.put(typeName, set);
	}

	public Set<String> getAttributes(String typeName) {
		return type2attributess.get(typeName);
	}

	@Override
	public String toString() {
		return toPlainString();
	}

	public String toPlainString() {
		return String.format("[rules:%s, elements:%s]", getRules().toString(), element2type.toString());
	}

	public String toPrettyString() {
		String rules = PrettyPrinter.toStringBuilder(getRules(), 1).toString();
		String elements = PrettyPrinter.toStringBuilder(element2type, 1).toString();
		return String.format("{\n\trules=%s, \n\telements=%s\n}", rules, elements);
	}

	public XSchemaMeta getMeta() {
		return meta;
	}
}
