package eu.fox7.rexp.xml.schema;

import com.sun.xml.xsom.*;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.parser.SchemaDocument;
import com.sun.xml.xsom.parser.XSOMParser;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.Epsilon;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.extended.*;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.StreamX;
import eu.fox7.rexp.xml.schemax.SchemaInfo;
import eu.fox7.rexp.xml.url.resolver.CustomEntityResolver;
import eu.fox7.rexp.xml.util.XmlUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class Xsd2XSchema {
	private static boolean verbose = false;
	public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	private static final String NULL_TARGET_NAMESPACE = "NONE";
	private static final String ANON_CT_PREFIX = "anonymousType";
	private static final String ANON_ST_PREFIX = "simpleAnonymousType";
	private static final String BROKEN_TYPE = "BROKEN";

	private static final String[] testFiles = {
		".files/xml/edc-violated.xsd",
		".files/xml/xsd2.xsd",
		".files/xml/loop.xsd",
		".files/xml/mg.xsd",
		".files/xml/part/xsd0.xsd",
		".files/xml/broken.xsd",
		"../../data/.rexp/xsd/filtered/coordinateReferenceSystems.xsd",
		"../../data/.rexp/xsd/filtered/mpeg7-2004-with mpeg7v2-namespace.xsd",
		"../../data/.rexp/xsd/filtered/bad/geometryBasic0d1d.xsd",
		".files/xml/part/xsdx.xsd",
		"../../data/.rexp/xsd/filtered/a3group-web.googlecode.com/svn-history/r51/trunk/Source/App_WebReferences/com/paypal/sandbox/www/eBLBaseComponents.xsd",
	};

	public static void main(String[] args) throws Exception {
		Director.setup();
		String context = FileX.toAbsoluteExternal(FileX.newFile(Director.resolve("dir.xsd.filtered")));
		String cache = Director.resolve("dir.xsd.cache");
		CustomEntityResolver es = new CustomEntityResolver(context, cache);
		es.setUseExclusiveCacheUsage(true);

		String fileName = args.length > 0 ? args[0] : testFiles[10];
		Log.configureRootLogger(Level.FINEST);
		Xsd2XSchema.setVerbose(true);
		Xsd2XSchema processor = new Xsd2XSchema();

		processor.setEntityResolver(es);
		processor.process(FileX.newFile(fileName));
		System.out.println(processor.getResult().toPrettyString());
	}

	private static void log(String s, Object... obj) {
		if (verbose) {
			System.out.println(String.format(s, obj));
		}
	}

	protected XSchema schema;
	protected int anonCtCount;
	private int anonStCount;
	private SchemaInfo schemaInfo;
	private EntityResolver entityResolver;

	private Map<XSType, String> typeNameMap;

	private void init() {
		schema = new XSchema();
		anonCtCount = 0;
		anonStCount = 0;
		typeNameMap = new LinkedHashMap<XSType, String>();
	}

	private void flush() {
		typeNameMap = null;
	}

	public static void setVerbose(boolean verbose) {
		Xsd2XSchema.verbose = verbose;
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public XSchema getResult() {
		return schema;
	}

	public void process(InputStream inStream) throws SAXException {
		process(new InputSource(inStream));
	}

	public void process(File file) throws SAXException, IOException {
		process(new InputSource(file.toURI().toURL().toExternalForm()));
	}

	public void process(InputSource inSource) throws SAXException {
		init();
		XSOMParser parser = new XSOMParser(XmlUtils.newSAXParserFactory());
		if (entityResolver != null) {
			parser.setEntityResolver(entityResolver);
		}
		inSource = preAnalyze(inSource);
		parser.parse(inSource);
		try {
			process(parser);
		} catch (InternalError e) {
			Log.e("Internal error in XSOM");
			schema.flagError();
			throw e;
		}
		flush();
	}

	private InputSource preAnalyze(InputSource inSource) {
		InputSource returnedSource = inSource;
		InputStream processStream = null;
		InputStream bis = inSource.getByteStream();
		if (bis != null) {
			byte[] ba = StreamX.toBytes(bis);
			processStream = new ByteArrayInputStream(ba);
			ByteArrayInputStream streamCopy = new ByteArrayInputStream(ba);
			returnedSource = new InputSource(streamCopy);
		} else if (inSource.getSystemId() != null) {
			try {
				processStream = new URL(inSource.getSystemId()).openStream();
			} catch (IOException ex) {
				Log.e("Error analyzing schema %s", inSource.getSystemId());
			}
		}
		if (processStream != null) {
			schemaInfo = new SchemaInfo(processStream);
		} else {
			schemaInfo = new SchemaInfo();
			Log.w("No preanalysis done");
		}
		return returnedSource;
	}


	protected void process(XSOMParser parser) throws SAXException {
		XSSchemaSet xset = safeGetParserResult(parser);
		Collection<XSSchema> schemas;
		if (xset != null) {
			schemas = xset.getSchemas();
		} else {
			Log.w("XSOM failed getting regular result");
			schemas = new LinkedHashSet<XSSchema>();
			for (SchemaDocument doc : parser.getDocuments()) {
				XSSchema xsSchema = doc.getSchema();
				schemas.add(xsSchema);
				String tnsStr = xsSchema.getTargetNamespace();
				tnsStr = tnsStr.length() > 0 ? tnsStr : NULL_TARGET_NAMESPACE;
				log("Schema with target namespace: %s", tnsStr);
			}
		}

		for (XSSchema xsSchema : schemas) {
			if (XSD_NAMESPACE.equals(xsSchema.getTargetNamespace())) {
				continue;
			}

			for (XSElementDecl e : xsSchema.getElementDecls().values()) {
				processElementDecl(e, true);
			}

			for (XSComplexType ct : xsSchema.getComplexTypes().values()) {
				process(ct, null);
			}
		}
	}
	

	protected String processElementDecl(XSElementDecl e, boolean isRoot) {
		String elementName = getElementName(e);
		log("elem def: %s", elementName);

		XSType type = safeGetType(e);
		if (type == null) {
			return BROKEN_TYPE;
		}
		String lookaheadTypeName = null;
		if (e.isGlobal()) {
			if (schema.hasElement(elementName)) {
				return schema.getElementTypeName(elementName);
			} else {
				lookaheadTypeName = getTypeName(type, type.isComplexType());
				schema.addElement(elementName, lookaheadTypeName);

				Locator locator = e.getLocator();
				if (locator != null) {
					int n = locator.getLineNumber();
					schema.getMeta().putElementMeta(elementName, n);
				}
			}
		}

		if (type.isComplexType()) {
			String typeName = process(type.asComplexType(), lookaheadTypeName);
			return typeName;
		} else if (type.isSimpleType()) {
			String typeName = process(type.asSimpleType());
			return typeName;
		} else {
			throw new RuntimeException("Simple or complex type expected");
		}
	}

	protected String process(XSComplexType ct, String ctName) {
		if (ctName == null) {
			ctName = getTypeName(ct, true);
		}

		XSType baseType = safeGetBaseType(ct);
		if (baseType != null) {
			String baseTypeName = getPrefixedName(baseType);
			log("Base type: %s", baseTypeName);
		}

		if (!schema.hasComplexType(ctName)) {
			schema.markComplexType(ctName);
			log("complex type: %s", ctName);
			safeProcessAttributes(ct);

			XSParticle part = safeGetParticle(ct);
			if (part == null) {
				log("complex type is empty");
			} else {
				schema.addComplexRule(ctName, process(part, ct.isMixed()));
				Locator locator = ct.getLocator();
				if (locator != null) {
					int n = locator.getLineNumber();
					schema.getMeta().putTypeMeta(ctName, n);
				}
			}
		} else {
			log("known complex type: %s", ctName);
		}
		return ctName;
	}
	

	protected RegExp process(XSParticle part, boolean mixed) {
		XSTerm term = safeGetTerm(part);
		if (term == null) {
			return new ReSymbol(new XSymbol(BROKEN_TYPE, BROKEN_TYPE));
		}
		RegExp compositor = process(term, mixed);
		return counter(part, compositor);
	}

	private RegExp process(XSTerm term, boolean mixed) {
		if (term.isWildcard()) {
			log("wildcard");
			return new ReSymbol(new XSymbol("wildcard", "wildcard"));
		} else if (term.isModelGroup()) {
			log("model group");
			XSModelGroup mg = term.asModelGroup();
			return process(mg, mixed);
		} else if (term.isModelGroupDecl()) {
			log("model group declaration");
			XSModelGroup mg = term.asModelGroupDecl().getModelGroup();
			return process(mg, mixed);
		} else if (term.isElementDecl()) {
			log("particle is element: %s", term.asElementDecl().getName());
			XSElementDecl ed = term.asElementDecl();
			String etype = processElementDecl(ed, false);
			return new ReSymbol(new XSymbol(getElementName(ed), etype));
		} else {
			throw new RuntimeException("Unsupported particle");
		}
	}

	protected RegExp process(XSModelGroup mg, boolean mixed) {
		if (mg.getSize() > 0) {
			if (mg.getSize() > 1) {
				return compositor(mg, processChildParticles(mixed, mg.getChildren()), mixed);
			} else {
				XSParticle child = mg.getChild(0);
				log("single child: %s", safeGetTerm(child));
				return compositor(mg, processChildParticles(mixed, child), mixed);
			}
		} else {
			return new ReSymbol(new XSymbol("emptymodel", "emptymodel"));
		}
	}

	protected RegExp[] processChildParticles(boolean mixed, XSParticle... pa) {
		List<RegExp> list = new LinkedList<RegExp>();
		for (XSParticle child : pa) {
			log("child: %s", safeGetTerm(child));
			list.add(process(child, mixed));
		}
		return list.toArray(new RegExp[pa.length]);
	}

	private RegExp counter(XSParticle part, RegExp re) {
		final BigInteger minBigInt = part.getMinOccurs();
		final BigInteger maxBigInt = part.getMaxOccurs();
		int min = 1;
		int max = 1;

		if (minBigInt != null && minBigInt.compareTo(BigInteger.ONE) != 0) {
			log("min: %s", minBigInt);
			min = minBigInt.intValue();
		}
		if (maxBigInt != null && maxBigInt.compareTo(BigInteger.ONE) != 0) {
			if (maxBigInt == BigInteger.valueOf(XSParticle.UNBOUNDED)) {
				log("max: unbounded");
				max = Counter.INFINITY;
			} else {
				log("max: %s", minBigInt);
				max = maxBigInt.intValue();
			}
		}

		if (maxBigInt == null && minBigInt != null && min != 1) {
			return new MinCounter(re, min);
		} else if (minBigInt == null && maxBigInt != null && max != 1) {
			return new MaxCounter(re, max);
		}

		if (min != 1 || max != 1) {
			return new Counter(re, min, max);
		} else {
			return re;
		}
	}

	private RegExp compositor(XSModelGroup mg, RegExp[] ra, boolean mixed) {
		Compositor comp = mg.getCompositor();
		if (Compositor.ALL.equals(comp)) {
			log("compositor: ALL");
			return new All(ra);
		} else if (Compositor.CHOICE.equals(comp)) {
			log("compositor: CHOICE");
			return new Choice(ra);
		}
		if (Compositor.SEQUENCE.equals(comp)) {
			log("compositor: SEQ");
			return new Sequence(ra);
		} else {
			throw new RuntimeException("Unsupported compositor");
		}
	}

	protected String process(XSSimpleType st) {
		String stName = getTypeName(st, false);
		if (!schema.hasSimplexType(stName)) {
			log("simple type: %s", stName);
			if (st.isPrimitive()) {
				log("primitive: %s", st);
				schema.addSimpleRule(stName, newSimpleNode(stName, "primitive"));
			} else if (st.isList()) {
				log("list: %s", st.asList());
				XSSimpleType lt = st.asList().getItemType();
				schema.addSimpleRule(stName, newSimpleNode(stName, "list"));
				process(lt);
			} else if (st.isUnion()) {
				XSUnionSimpleType u = st.asUnion();
				schema.addSimpleRule(stName, newSimpleNode(stName, "union"));
				log("union: %s", u.getName());
				for (int i = 0; i < u.getMemberSize(); i++) {
					XSSimpleType m = u.getMember(i);
					log("union member: %s", m.getName());
					process(m);
				}
			} else if (st.isRestriction()) {
				XSRestrictionSimpleType r = st.asRestriction();
				log("restriction: %s", r.getName());
				schema.addSimpleRule(stName, newSimpleNode(stName, "restriction"));
				for (XSFacet f : r.getDeclaredFacets()) {
					log("facet: %s", f.getName());
				}
			}
		} else {
			log("known simple type: %s", stName);
		}
		return stName;
	}

	private void processAttributes(XSComplexType ct) {
		for (XSAttributeUse au : ct.getAttributeUses()) {
			XSAttributeDecl ad = au.getDecl();
			String attrName = ad.getName();
			process(ad.getType());
			log("attribute: %s, type: %s, required: %s", attrName, ad.getType(), au.isRequired());
			log("def val: %s, fixed val: %s", ad.getDefaultValue(), ad.getFixedValue());

			schema.addAttribute(ct.getName(), attrName);
		}

		XSWildcard wc = ct.getAttributeWildcard();
		if (wc != null) {
			log("attribute wildcard");
			switch (wc.getMode()) {
				case XSWildcard.STRTICT:
				case XSWildcard.LAX:
				case XSWildcard.SKIP:
			}
		}
	}

	private RegExp newSimpleNode(String stName, String type) {
		if (!schema.hasSimplexType(type)) {
			schema.addSimpleRule(type, new Epsilon());
		}
		return new ReSymbol(new XStSymbol(stName, type));
	}
	

	private String getTypeName(XSType type, boolean complex) {
		String typeName = getPrefixedName(type);
		if (typeName == null) {
			if (complex) {
				if (typeNameMap.containsKey(type)) {
					return typeNameMap.get(type);
				} else {
					typeName = ANON_CT_PREFIX + anonCtCount++;
					typeNameMap.put(type, typeName);
				}
			} else {
				typeName = ANON_ST_PREFIX + anonStCount++;
			}
		}
		return typeName;
	}

	private String getPrefixedName(XSDeclaration decl) {
		String name = decl.getName();
		if (name != null && schemaInfo.hasTargetNamespace()) {
			String tns = decl.getTargetNamespace();
			if (schemaInfo.hasNamespace(tns)) {
				String nsp = schemaInfo.getNamespacePrefix(tns);
				return (nsp != null && nsp.length() > 0)
					? String.format("%s:%s", nsp, name)
					: name;
			}
		}
		return name;
	}

	private String getElementName(XSElementDecl element) {
		return element.getName();
	}
	

	private XSSchemaSet safeGetParserResult(XSOMParser parser) {
		try {
			XSSchemaSet xset = parser.getResult();
			return xset;
		} catch (InternalError e) {
			Log.e("XSD seems to be broken");
			schema.flagError();
			return null;
		} catch (SAXException ex) {
			return null;
		} catch (NullPointerException ex) {
			Log.e("Unknown XSOM exception");
			return null;
		}
	}

	private XSType safeGetType(XSElementDecl elementDecl) {
		try {
			return elementDecl.getType();
		} catch (InternalError e) {
			logInternalError("Error processing element: %s", e);
			schema.flagError();
			return null;
		}
	}

	private XSTerm safeGetTerm(XSParticle part) {
		try {
			return part.getTerm();
		} catch (InternalError e) {
			logInternalError("Error processing term: %s", e);
			schema.flagError();
			return null;
		}
	}

	private void safeProcessAttributes(XSComplexType ct) {
		try {
			processAttributes(ct);
		} catch (InternalError e) {
			logInternalError("Error processing attributes: %s", e);
			schema.flagError();
		}
	}

	private XSParticle safeGetParticle(XSComplexType ct) {
		try {
			return ct.getContentType().asParticle();
		} catch (InternalError e) {
			logInternalError("Error processing particle: %s", e);
			schema.flagError();
			return null;
		}
	}

	private XSType safeGetBaseType(XSComplexType ct) {
		try {
			return ct.getBaseType();
		} catch (InternalError e) {
			logInternalError("Error processing base type: %s", e);
			schema.flagError();
			return null;
		}
	}

	private void logInternalError(String s, Throwable e) {
	}
}
