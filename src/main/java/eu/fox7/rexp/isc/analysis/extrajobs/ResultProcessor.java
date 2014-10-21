package eu.fox7.rexp.isc.analysis.extrajobs;

import eu.fox7.rexp.isc.analysis.basejobs.Job;
import eu.fox7.rexp.isc.analysis.schema.CounterDepthAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.CounterNumAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.CounterSummarizer;
import eu.fox7.rexp.isc.analysis.schema.SdtAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.cm.ReFormAnalyzer;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.XmlQuerying;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import net.sf.saxon.trans.XPathException;
import nu.xom.Element;
import nu.xom.Nodes;

import java.io.*;
import java.util.*;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

public class ResultProcessor extends Job {
	private static final String PROP_SDT_RESULT_2 = "analysis.sdt.2";
	private static final String PROP_CDEPTH_RESULT_2 = "analysis.depth.2";
	private static final String PROP_CNUM_RESULT_2 = "analysis.counters.2";
	private static final String PROP_RET_RESULT_2 = "analysis.ret.2";

	private static final String QR_SDT = "xquery/sdt.xquery";
	private static final String QR_CDEPTH = "xquery/nesting_x.xquery";
	private static final String QR_CNUM = "xquery/counters.xquery";
	private static final String QR_RET = "xquery/ret.xquery";

	public static void main(String[] args) throws Exception {
		setup();
		new ResultProcessor().extraResultProcessing();
	}

	private Set<Class<?>> analyses;

	public ResultProcessor() {
		analyses = new LinkedHashSet<Class<?>>();
	}

	public void setAnalyses(Set<Class<?>> analyses) {
		this.analyses = analyses;
	}

	@Override
	protected void work() {
		try {
			transformSelect();
		} catch (XPathException ex) {
			Log.w("Could not process results, invalid XPath: %s", ex);
		} catch (IOException ex) {
			Log.w("Could not read/write results: %s", ex);
		}
	}

	static void transformAll() throws XPathException, IOException {
		transform(QR_SDT, resolve(PROP_SDT_RESULT), resolve(PROP_SDT_RESULT_2));
		transform(QR_CDEPTH, resolve(PROP_CDEPTH_RESULT), resolve(PROP_CDEPTH_RESULT_2));
		transform(QR_CNUM, resolve(PROP_CNUM_RESULT), resolve(PROP_CNUM_RESULT_2));
		transform(QR_RET, resolve(PROP_RET_RESULT), resolve(PROP_RET_RESULT_2));
	}

	private void transformSelect() throws XPathException, IOException {
		if (analyses.contains(SdtAnalyzer.class)) {
			transform(QR_SDT, resolve(PROP_SDT_RESULT), resolve(PROP_SDT_RESULT_2));
		}
		if (analyses.contains(CounterDepthAnalyzer.class)) {
			transform(QR_CDEPTH, resolve(PROP_CDEPTH_RESULT), resolve(PROP_CDEPTH_RESULT_2));
		}
		if (analyses.contains(CounterNumAnalyzer.class)) {
			Log.i("Running optimized counter value analysis");
			CounterSummarizer.INSTANCE.apply(resolve(PROP_CNUM_RESULT), resolve(PROP_CNUM_RESULT_2));
		}
		if (analyses.contains(ReFormAnalyzer.class)) {
			transform(QR_RET, resolve(PROP_RET_RESULT), resolve(PROP_RET_RESULT_2));
		}
	}

	private static void transform(String queryResourceName, String inFileName, String outFileName) throws XPathException, IOException {
		Log.i("Running query resource: %s", queryResourceName);
		unsafeTransform(queryResourceName, inFileName, outFileName);
	}

	private static void unsafeTransform(String queryResourceName, String inFileName, String outFileName) throws XPathException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XmlQuerying.xquery(inFileName, queryResourceName, bos);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		bos.close();
		File outFile = FileX.newFile(outFileName);
		FileOutputStream fos = new FileOutputStream(outFile);
		XmlUtils.normalizeXml(bis, fos);
		bis.close();
		fos.close();
	}

	public void extraResultProcessing() {
		CsvUtil.DELIM = ';';
		processOccurs("min.csv", "minglobal");
		processOccurs("max.csv", "maxglobal");
		processNesting("depth.csv");
		File file = FileX.newFile(resolve("analysis.ret.2"));
		if (file.exists()) {
			Element analysisRoot = XmlUtils.readXml(file);
			processReAnalysis(analysisRoot, "re_classes.csv");
			processRest("./analysis/analysis.csv", analysisRoot);
		}
		CsvUtil.DELIM = CsvUtil.DEFAULT_DELIM;
	}

	void processOccurs(String fileName, String xpath) {
		File file = FileX.newFile(resolve("analysis.counters.2"));
		if (!file.exists()) {
			return;
		}
		Element root = XmlUtils.readXml(file);
		Nodes nodes = root.query(xpath);
		Element e = (Element) nodes.get(0);
		List<Map<String, String>> maps = new LinkedList<Map<String, String>>();
		XmlMapUtils.readMapsFromXmlElements(e, maps);
		writeCsv(fileName, maps);
	}

	private static void writeCsv(String fileName, List<Map<String, String>> maps) {
		File file = FileX.newFile("./analysis", fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			PrintStream ps = new PrintStream(bos);
			CsvUtil.print(ps, maps);
			ps.flush();
		} catch (IOException e) {
			Log.e("Could not write file %s: %s", file, e);
		} finally {
			UtilX.silentClose(fos);
		}
	}
	

	private static final String SUFFIX = "global";
	private static final String EL_DEPTH = "depth";
	private static final String EL_NT_DEPTH = "ntdepth";
	private static final String EL_CMB_DEPTH = "combineddepth";
	private static final String EL_STAR_DEPTH = "stardepth";
	private static final String EL_PARSE_DEPTH = "parsedepth";

	void processNesting(String fileName) {
		List<Map<String, String>> maps = new LinkedList<Map<String, String>>();

		File file = FileX.newFile(resolve("analysis.depth.2"));
		if (!file.exists()) {
			return;
		}
		Element root = XmlUtils.readXml(file);

		List<Map<String, String>> acvdMaps = getMap(root, EL_DEPTH + SUFFIX);
		List<Map<String, String>> ntvdMaps = getMap(root, EL_NT_DEPTH + SUFFIX);
		List<Map<String, String>> cmbdMaps = getMap(root, EL_CMB_DEPTH + SUFFIX);
		List<Map<String, String>> starMaps = getMap(root, EL_STAR_DEPTH + SUFFIX);
		List<Map<String, String>> prseMaps = getMap(root, EL_PARSE_DEPTH + SUFFIX);


		for (int i = 0; i < prseMaps.size() + 3; i++) {
			String depth = String.valueOf(i);
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("depth", depth);
			map.put("counter", select(acvdMaps, EL_DEPTH, depth));
			map.put("ntcounter", select(ntvdMaps, EL_NT_DEPTH, depth));
			map.put("combined", select(cmbdMaps, EL_CMB_DEPTH, depth));
			map.put("star", select(starMaps, EL_STAR_DEPTH, depth));
			map.put("parse", select(prseMaps, EL_PARSE_DEPTH, depth));
			maps.add(map);
		}

		writeCsv(fileName, maps);
	}

	private static List<Map<String, String>> getMap(Element root, String xpath) {
		List<Map<String, String>> map = new LinkedList<Map<String, String>>();
		Nodes nodes = root.query(xpath);
		try {
			Element e = (Element) nodes.get(0);
			XmlMapUtils.readMapsFromXmlElements(e, map);
		} catch (Exception ex) {
			Log.w("Unexpected event during reading of map: %s", ex);
		}
		return map;
	}

	private static String select(List<Map<String, String>> maps, String key, String depth) {
		for (Map<String, String> map : maps) {
			String fkey = map.get(key);
			if (depth.equals(fkey)) {
				return map.get("count");
			}
		}
		return "";
	}
	

	void processReAnalysis(Element root, String formsFileName) {
		Nodes nodes = root.query("forms");
		Element e = (Element) nodes.get(0);
		List<Map<String, String>> maps = new LinkedList<Map<String, String>>();
		XmlMapUtils.readMapsFromXmlElements(e, maps);
		writeCsv(formsFileName, maps);
	}

	void processRest(String fileName, Element root) {
		StringBuilder sb = new StringBuilder();
		sb.append("key;value");
		process(sb, root, "exp", "global/item/exp");
		process(sb, root, "sore", "global/item/sore");
		process(sb, root, "soreu", "global/item/soreu");
		process(sb, root, "chare", "global/item/chare");
		process(sb, root, "cchare", "global/item/cchare");
		process(sb, root, "wchare", "global/item/wchare");
		process(sb, root, "cwchare", "global/item/cwchare");
		procesExternalLogs(sb);
		Log.i("%s", sb);
		FileX.stringToFile(sb.toString(), FileX.newFile(fileName));
	}

	void procesExternalLogs(Appendable sb) {
		String ntExpFile = resolve("analysis.hascounter");
		String ntExp = FileX.stringFromFile(ntExpFile);

		String maxOccFile = resolve("analysis.maxocc");
		String maxOcc = FileX.stringFromFile(maxOccFile).replaceAll("\n.*", "");

		Element root = XmlUtils.readXml(FileX.newFile(resolve("analysis.sdt.2")));
		String sdt = "";
		try {
			sdt = root.query("item/sdt").get(0).getValue();
		} catch (Exception ex) {
			Log.w("%s", ex);
		}

		try {
			sb.append("\n");
			sb.append(String.format("%s;%s", "nt_exp", ntExp));
			sb.append("\n");
			sb.append(String.format("%s;%s", "max_occ", maxOcc));
			sb.append("\n");
			sb.append(String.format("%s;%s", "sdt", sdt));
		} catch (IOException ex) {
			Log.w("%s", ex);
		}
	}

	private static void process(Appendable sb, Element root, String key, String selector) {
		try {
			String s = root.query(selector).get(0).getValue();
			sb.append("\n");
			sb.append(String.format("%s;%s", key, s));
		} catch (Exception ex) {
			Log.w("%s", ex);
		}
	}
}
