package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.XmlQuerying;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import eu.fox7.rexp.xml.util.iterators.NodeIterable;
import net.sf.saxon.trans.XPathException;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IscResults {
	private static final String XQR_ISB = "xquery/isb.xquery";
	private static final String ATTR_ID = "executor";
	public static final String ISC_RESULTS_OUTPUT = "./analysis/%s.txt";

	public static void main(String[] args) {
		Director.setup();
		IscResults processor = new IscResults();
		String fileName = IscBenchmark.DEFAULT_FILE_NAME;
		processor.process(fileName);
	}

	public IscResults() {
		verbose = false;
	}

	private boolean verbose;

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void process(String fileName) {
		Element transformedResultRoot = load(fileName);
		traverse(transformedResultRoot);
	}

	private Element load(String fileName) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XmlQuerying.xquery(fileName, XQR_ISB, bos);
			bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			bos.close();
			Element element = XmlUtils.readXml(bis);
			bis.close();
			return element;
		} catch (IOException ex) {
			Log.e("%s", ex);
		} catch (XPathException ex) {
			Log.e("%s", ex);
		}
		return null;
	}

	private void traverse(Element transformedResultRoot) {
		Nodes nodes = transformedResultRoot.query("items");
		for (Node node : NodeIterable.iterateNodes(nodes)) {
			Element e = (Element) node;
			String id = e.getAttributeValue(ATTR_ID);

			List<Map<String, String>> outMapList = new LinkedList<Map<String, String>>();
			XmlMapUtils.readMapsFromXmlElements(e, outMapList);
			output(id, outMapList);
		}
	}

	private void output(String id, List<Map<String, String>> mapList) {

		if (id != null && !id.isEmpty()) {
			String fileName = String.format(ISC_RESULTS_OUTPUT, id);
			FileOutputStream fos = null;
			try {
				File file = FileX.newFile(fileName);
				fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				PrintStream ps = new PrintStream(bos);
				CsvUtil.print(ps, mapList);
				ps.flush();
			} catch (IOException e) {
				Log.e("Could not write file %s: %s", fileName, e);
			} finally {
				UtilX.silentClose(fos);
			}
		}

		if (verbose) {
			System.out.println(id);
		}
	}
}
