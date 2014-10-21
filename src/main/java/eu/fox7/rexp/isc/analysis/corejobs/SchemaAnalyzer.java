package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.isc.analysis.basejobs.FileJob;
import eu.fox7.rexp.isc.analysis.schema.*;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import eu.fox7.rexp.xml.url.resolver.CustomEntityResolver;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Collection;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.PROP_XSD_FILTERED_DIR;

public class SchemaAnalyzer extends FileJob {
	public static final String PROP_MAX_OCC = "analysis.maxocc";
	public static final String PROP_HAS_COUNTER = "analysis.hascounter";
	public static final String PROP_ANALYSIS_DEEP = "analysis.deep";
	public static final String PROP_EXP_COMPLEX = "analysis.expcmplx";
	public static final String PROP_EXP_SEQ = "analysis.expseq";

	private SchemaAnalysis[] schemaActions;
	private boolean clear = false;
	private boolean useCache = false;
	private boolean useOnlyCache = false;

	public SchemaAnalyzer(SchemaAnalysis... schemaActions) {
		this.schemaActions = schemaActions;
	}

	public SchemaAnalyzer(Collection<SchemaProcessor> schemaActions) {
		this(schemaActions.toArray(new SchemaAnalysis[schemaActions.size()]));
	}

	public void setClear(boolean clear) {
		this.clear = clear;
	}

	public void setCaching(boolean useCache) {
		this.useCache = useCache;
	}

	public void setUseOnlyCache(boolean useOnlyCache) {
		this.useOnlyCache = useOnlyCache;
	}

	@Override
	protected void process(File file, String relativeFileName) {
		try {
			Xsd2XSchema converter = new Xsd2XSchema();
			if (useCache) {
				String context = FileX.toAbsoluteExternal(getJobDirectory());
				String cache = Director.resolve("dir.xsd.cache");
				CustomEntityResolver es = new CustomEntityResolver(context, cache);
				es.setUseExclusiveCacheUsage(useOnlyCache);
				converter.setEntityResolver(es);
			}
			converter.process(file);
			XSchema schema = converter.getResult();
			process(schema, relativeFileName);
		} catch (FileNotFoundException ex) {
			Log.w(ex.toString());
		} catch (SAXException ex) {
			Log.w(ex.toString());
		} catch (IOException ex) {
			Log.w(ex.toString());
		} catch (InternalError ex) {
			Log.w(ex.toString());
		}
	}

	private void process(XSchema schema, Object id) {

		ContentModelIterator cmi = new ContentModelIterator(schema, id);
		for (ContentModelBundle bundle : UtilX.iterate(cmi)) {
			for (SchemaProcessor schemaAction : schemaActions) {
				if (schemaAction instanceof ContentModelAnalyzer) {
					ContentModelAnalyzer cma = (ContentModelAnalyzer) schemaAction;
					cma.process(bundle);
				} else {
				}
			}
		}

		for (SchemaProcessor schemaAction : schemaActions) {
			if (!(schemaAction instanceof ContentModelAnalyzer)) {
				Log.w("Only content model analysers can be inlined. Analyser: %s", schemaAction.getClass().getSimpleName());
				schemaAction.process(schema, id);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!clear) {
			load();
		}
		for (SchemaAnalysis sa : schemaActions) {
			sa.onStart();
		}
	}

	private void load() {
		for (SchemaAnalysis schemaAction : schemaActions) {
			InputStream source = sourceAsInstreamProperty(schemaAction);
			if (source != null) {
				Element e = XmlUtils.readXml(source);
				schemaAction.fromXml(e);
			}
		}
	}

	@Override
	protected void onStop() {
		for (SchemaAnalysis sa : schemaActions) {
			sa.onStop();
		}
		store();
		super.onStop();
	}

	private void store() {
		for (SchemaAnalysis schemaAction : schemaActions) {
			Element e = schemaAction.toXml();
			OutputStream o = targetAsOutStreamProperty(schemaAction);
			XmlUtils.serializeXml(e, o);
			UtilX.silentClose(o);
		}
	}

	private static InputStream sourceAsInstreamProperty(SchemaAnalysis schemaAction) {
		try {
			String source = schemaAction.getMetaFileName();
			File file = FileX.newFile(source);
			return new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			return null;
		} catch (NullPointerException ex) {
			return null;
		}
	}

	private static OutputStream targetAsOutStreamProperty(SchemaAnalysis schemaAction) {
		try {
			String target = schemaAction.getMetaFileName();
			File outFile = FileX.newFile(target);
			FileX.prepareOutFile(outFile);
			return new FileOutputStream(outFile);
		} catch (FileNotFoundException ex) {
			return null;
		} catch (NullPointerException ex) {
			return null;
		}
	}

	@Override
	protected String getJobDirectoryProperty() {
		return PROP_XSD_FILTERED_DIR;
	}
}
