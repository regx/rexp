package eu.fox7.rexp.xml.schemax;

import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static eu.fox7.rexp.xml.util.iterators.NodeIterable.iterateNodes;

public class SchemaUtil {
	private static boolean verbose = false;

	private static void log(String s, Object... obj) {
		if (verbose) {
			System.out.println(String.format(s, obj));
		}
	}

	public static void setVerbose(boolean verbose) {
		SchemaUtil.verbose = verbose;
	}

	public static final String XS_ELMNT_SCHEMA = "schema";
	public static final String XS_ATTR_SCHEMA_LOCATION = "schemaLocation";
	public static final String XS_ATTR_NAMESPACE = "namespace";
	private static final String XS_TARGET_NAMESPACE = "targetNamespace";

	private static final String[] testFiles = {
		".files/xml/xsd3.xsd",
		".files/xml/part/xsd0.xsd",
	};

	public static void main(String[] args) {
		String fileName = args.length > 0 ? args[0] : testFiles[0];
		setVerbose(true);
		File file = FileX.newFile(fileName);
		Element xsRoot = XmlUtils.readXml(file);
		findSchemaIncludes(xsRoot);
		findSchemaImports(xsRoot);
		findNamespaces(xsRoot);
	}

	public static Set<String> findSchemaIncludes(Element xsdRootelement) {
		Set<String> s = new LinkedHashSet<String>();
		XPathContext namespaces = new XPathContext();
		namespaces.addNamespace("xs", Xsd2XSchema.XSD_NAMESPACE);
		Nodes qr = xsdRootelement.query("/xs:schema/xs:include", namespaces);
		for (Node node : iterateNodes(qr)) {
			Element e = (Element) node;
			Attribute attrSchemaLocation = e.getAttribute(XS_ATTR_SCHEMA_LOCATION);
			if (attrSchemaLocation != null) {
				String schemaLocation = attrSchemaLocation.getValue();
				s.add(schemaLocation);
				log("include schemaLocation: %s", schemaLocation);
			}
		}
		return s;
	}

	public static Set<SchemaImportDecl> findSchemaImports(Element xsdRootelement) {
		Set<SchemaImportDecl> s = new LinkedHashSet<SchemaImportDecl>();
		XPathContext namespaces = new XPathContext();
		namespaces.addNamespace("xs", Xsd2XSchema.XSD_NAMESPACE);
		Nodes qr = xsdRootelement.query("/xs:schema/xs:import", namespaces);
		for (Node node : iterateNodes(qr)) {
			Element e = (Element) node;

			SchemaImportDecl schemaImport = new SchemaImportDecl();

			Attribute attrNamepace = e.getAttribute(XS_ATTR_NAMESPACE);
			if (attrNamepace != null) {
				String schemaNamespace = attrNamepace.getValue();
				schemaImport.setNamespace(schemaNamespace);
			}

			Attribute attrSchemaLocation = e.getAttribute(XS_ATTR_SCHEMA_LOCATION);
			if (attrSchemaLocation != null) {
				String schemaLocation = attrSchemaLocation.getValue();
				schemaImport.setSchemaLocation(schemaLocation);
			}

			if (schemaImport.isValidDeclaration()) {
				log("import: %s", schemaImport);
				s.add(schemaImport);
			}
		}
		return s;
	}

	public static String getTargetNamespace(Element xsdRootelement) {
		String tns = xsdRootelement.getAttributeValue(XS_TARGET_NAMESPACE);
		log("%s=%s", XS_TARGET_NAMESPACE, tns);
		return tns;
	}
	public static Map<String, String> findNamespaces(Element xsdRootElement) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 0; i < xsdRootElement.getNamespaceDeclarationCount(); i++) {
			String nsp = xsdRootElement.getNamespacePrefix(i);
			String nsu = xsdRootElement.getNamespaceURI(nsp);
			log("%s=%s", nsp, nsu);
			map.put(nsu, nsp);
		}
		return map;
	}

	public static boolean isXmlSchema(Element rootElement) {
		if (XS_ELMNT_SCHEMA.equals(rootElement.getLocalName())) {
			return true;
		} else {
			return false;
		}
	}
}
