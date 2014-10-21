package eu.fox7.rexp.isc.analysis2.mvn;

import eu.fox7.rexp.isc.analysis.basejobs.BatchJob.Item;
import eu.fox7.rexp.isc.analysis.basejobs.XmlJob;
import eu.fox7.rexp.isc.analysis2.mvn.MvnRepo.Doc;
import eu.fox7.rexp.util.BeanMarshaller;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.url.UriX;
import eu.fox7.rexp.xml.util.XmlFileMapList;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlMapUtils.StringMapList;
import nu.xom.Element;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

public class MvnCrawler extends XmlJob<Item<Doc>> implements MvnJarScanner.ScanAction {
	public static void main(String[] args) {
		setup();
		MvnCrawler mc = new MvnCrawler();
		mc.setRootId(-304974526);
		mc.execute();
	}

	private Collection<Doc> docs;
	private MvnJarScanner scanner;

	private int rootId;
	private boolean recursive;
	private boolean doScan;

	private XmlFileMapList scanData;
	private String scanDataPath;

	private String mvnBaseDir;

	public MvnCrawler() {
		docs = new LinkedList<Doc>();
		scanner = new MvnJarScanner(this);

		rootId = MvnRepo.CENTRAL_ID;
		recursive = false;
		doScan = true;
		scanDataPath = resolve(PROP_MVN_XSD);
		mvnBaseDir = resolve(PROP_XSD_DIR);
	}

	public void setRootId(int rootId) {
		this.rootId = rootId;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public void setDoScan(boolean doScan) {
		this.doScan = doScan;
	}

	public void setTargetDir(String mvnBaseDir) {
		this.mvnBaseDir = mvnBaseDir;
	}

	@Override
	protected String getJobMetaFileProperty() {
		return PROP_MVN_REPO;
	}

	@Override
	protected Element toXmlElement() {
		StringMapList maps = new StringMapList();
		BeanMarshaller.toMapList(maps, docs);
		return XmlMapUtils.writeMapsToXmlAttributes(maps);
	}

	@Override
	protected void fromXmlElement(Element root) {
		StringMapList maps = new StringMapList();
		XmlMapUtils.readMapsFromXmlAttributes(root, maps);
		docs = new LinkedList<Doc>();
		BeanMarshaller.toBeanList(docs, maps, Doc.class);
	}

	@Override
	protected Iterator<Item<Doc>> iterateItems() {
		return Item.wrap(new MvnIterator(rootId, recursive));
	}

	@Override
	protected void process(Item<Doc> item) {
		Doc doc = item.get();
		docs.add(doc);
		if (doScan) {
			scanner.scan(doc);
		}
	}
	

	@Override
	public void load() {
		super.load();
		scanData = new XmlFileMapList(scanDataPath);
		scanData.load();
	}

	@Override
	public void save() {
		super.save();
		scanData.save();
	}

	@Override
	public void onScan(String jarPath, String innerPath, InputStream inputStream) {
		if (innerPath.endsWith(".xsd")) {
			String filePath = UriX.concatPaths(mvnBaseDir, jarPath, innerPath);
			File xsdFile = FileX.newFile(filePath);
			if (!FileX.streamToFile(inputStream, xsdFile)) {
				filePath = "";
			}

			Map<String, String> map = scanData.newMap();
			map.put("jar", jarPath);
			map.put("xsd", innerPath);
			map.put("file", filePath);

			Log.d("In jar %s, found XSD file %s, stored in %s", jarPath, innerPath, filePath);
		} else {
			Log.v("In jar %s, found non-XSD file %s", jarPath, innerPath);
		}
	}
}
