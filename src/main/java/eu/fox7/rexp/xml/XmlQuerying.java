package eu.fox7.rexp.xml;

import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.xml.util.UriHelper;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.InputSource;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class XmlQuerying {
	private static final String XQUERY_MODULE_BASE = "xquery";
	@SuppressWarnings("deprecation")
	public static void xquery(String xmlFileName, String queryResourceName, OutputStream outStream) throws IOException, XPathException {
		Configuration config = new Configuration();
		StaticQueryContext sqc = new StaticQueryContext(config);
		DynamicQueryContext dqc = new DynamicQueryContext(config);
		InputStream queryStream = PropertiesManager.getResourceStream(queryResourceName);
		Reader reader = new InputStreamReader(queryStream);
		sqc.setModuleURIResolver(new ModuleAsResourceUriResolver(XQUERY_MODULE_BASE));
		XQueryExpression xqe = sqc.compileQuery(reader);
		reader.close();

		File xmlFile = FileX.newFile(xmlFileName);
		FileReader fr = new FileReader(xmlFile);
		InputSource inputSource = new InputSource(fr);
		SAXSource saxs = new SAXSource(inputSource);
		DocumentInfo di = sqc.buildDocument(saxs);
		dqc.setContextItem(di);

		Result resultHolder = new StreamResult(outStream);
		xqe.run(dqc, resultHolder, null);
	}
}

class ModuleAsResourceUriResolver implements ModuleURIResolver {
	private static final long serialVersionUID = 1L;
	private final String base;

	public ModuleAsResourceUriResolver(String base) {
		this.base = base;
	}

	@Override
	public StreamSource[] resolve(String moduleURI, String baseURI, String[] locations) throws XPathException {
		StreamSource[] sources = new StreamSource[locations.length];
		for (int i = 0; i < sources.length; i++) {
			String location = locations[i];
			location = base + "/" + UriHelper.fileNameFromUrlStr(location);
			InputStream inStream = PropertiesManager.getResourceStream(location);
			Reader reader = new InputStreamReader(inStream);
			sources[i] = new StreamSource(reader);
		}
		return sources;
	}
}
