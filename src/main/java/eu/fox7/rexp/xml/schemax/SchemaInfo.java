package eu.fox7.rexp.xml.schemax;

import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.util.UriHelper;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.InputStream;
import java.util.*;

public class SchemaInfo {
	private String targetNamespace;
	private Map<String, String> namespace2prefix;
	private Set<String> schemaIncludes;
	private Set<SchemaImportDecl> schemaImports;
	private boolean valid;

	public SchemaInfo() {
		targetNamespace = null;
		namespace2prefix = new HashMap<String, String>();
		schemaIncludes = new HashSet<String>();
		schemaImports = new HashSet<SchemaImportDecl>();
	}

	public SchemaInfo(InputStream inStream) {
		Element xsdRoot = XmlUtils.readXml(inStream);
		if (xsdRoot != null && SchemaUtil.isXmlSchema(xsdRoot)) {
			targetNamespace = SchemaUtil.getTargetNamespace(xsdRoot);
			namespace2prefix = SchemaUtil.findNamespaces(xsdRoot);
			schemaIncludes = SchemaUtil.findSchemaIncludes(xsdRoot);
			schemaImports = SchemaUtil.findSchemaImports(xsdRoot);
			valid = true;
		} else {
			Log.e("SchemaInfo could not be initialized");
		}
	}

	public boolean isValid() {
		return valid;
	}

	public boolean hasNamespace(String namespace) {
		return namespace2prefix.containsKey(namespace);
	}

	public String getNamespacePrefix(String namespace) {
		return namespace2prefix.get(namespace);
	}

	public boolean hasTargetNamespace() {
		return getTargetNamespace() != null;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public Set<String> getSchemaIncludes() {
		return schemaIncludes;
	}

	public Set<SchemaImportDecl> getSchemaImports() {
		return schemaImports;
	}

	public Set<String> getReferencedDocuments(String refBase) {
		Set<String> s = new LinkedHashSet<String>();

		Set<String> includes = getSchemaIncludes();
		if (includes != null) {
			for (String si : includes) {
				s.add(UriHelper.canonize(refBase, si));
			}
		}
		Set<SchemaImportDecl> imports = getSchemaImports();
		if (imports != null) {
			for (SchemaImportDecl si : imports) {
				String sl = si.getSchemaLocation();
				sl = UriHelper.canonize(refBase, sl);
				s.add(sl);
			}
		}
		return s;
	}
}
