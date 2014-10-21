package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.isc.analysis.basejobs.BatchJob;
import eu.fox7.rexp.isc.analysis.basejobs.XmlJob;
import eu.fox7.rexp.isc.analysis.util.Downloader;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.StreamX;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Callback;
import eu.fox7.rexp.util.mini.Transform;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

public class BatchDownloader extends XmlJob<BatchJob.Item<String>> {
	public static final String TAG_URL = "url";
	public static final String TAG_PATH = "path";

	private List<String> links;
	private Map<String, String> url2fileName;
	private String linkSourceFileName;
	private String downloadDir;
	private Callback<String> onDownloadListener;

	public BatchDownloader(List<String> links) {
		init();
		this.links = links;
	}

	public BatchDownloader() {
		init();
		links = new LinkedList<String>();
	}

	private void init() {
		this.url2fileName = new LinkedHashMap<String, String>();
		linkSourceFileName = PropertiesManager.getProperty(Director.PROP_LINK_FILE);
		downloadDir = PropertiesManager.getProperty(Director.PROP_DOWNLOAD_DIR);
	}

	public void registerOnDownloadListener(Callback<String> callback) {
		this.onDownloadListener = callback;
	}

	public void raiseOnDownloadListener(String surl) {
		if (onDownloadListener != null) {
			onDownloadListener.call(surl);
		}
	}

	@Override
	protected void onStart() {
		loadLinks();
		super.onStart();
	}

	public String getLinkSourceFilePath() {
		return linkSourceFileName;
	}

	public void setLinkSourceFileName(String linkSourceFilePath) {
		this.linkSourceFileName = linkSourceFilePath;
	}

	public String getDownloadDir() {
		return downloadDir;
	}

	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}

	private void loadLinks() {
		String name = getLinkSourceFilePath();
		try {
			File file = FileX.newFile(name);
			FileInputStream fis = new FileInputStream(file);
			Element element = XmlUtils.readXml(fis);
			XmlMapUtils.readCollectionFromXmlAttributes(element, links, GoogleLinkExtractor.TAG_URL);
		} catch (FileNotFoundException ex) {
			Log.w("Could not load list of links");
			Log.f("%s", ex);
		}
	}

	@Override
	protected Iterator<Item<String>> iterateItems() {
		return Item.wrap(links.iterator());
	}

	private UrlTransformer urlTransformer = new UrlTransformer();

	@Override
	protected void process(Item<String> item) {
		download(urlTransformer.transform(item.get()));
	}

	public String download(String surl) {
		if (!url2fileName.containsKey(surl)) {
			String fileName = Downloader.download(surl, downloadDir);
			if (fileName != null) {
				String filePath = fileName;//String.format("%s/%s", downloadDir, fileName);
				Log.i("Downloaded %s to %s", surl, filePath);
				url2fileName.put(surl, filePath);
				raiseOnDownloadListener(surl);
				return filePath;
			} else {
				Log.w("Failed to download %s", surl);
				return null;
			}
		} else {
			Log.i("Skipping already downloaded: %s", surl);
			return null;
		}
	}

	public String lookupLink(String surl) {
		return url2fileName.get(surl);
	}

	public Map<String, String> getRawMap() {
		return url2fileName;
	}

	@Override
	protected void fromXmlElement(Element root) {
		XmlMapUtils.readMapFromXmlAttributes(root, url2fileName, TAG_URL, TAG_PATH);
	}

	@Override
	public Element toXmlElement() {
		return XmlMapUtils.writeMapToXmlAttributes(url2fileName, TAG_URL, TAG_PATH);
	}

	@Override
	protected String getJobMetaFileProperty() {
		return Director.PROP_DOWNLOAD_FILE;
	}
}

class UrlTransformer implements Transform<String, String> {
	public static final String TRANSFORM_RULES_RESOURCE = "txt/urlTransformRules.txt";
	public static final String TRANSFORM_RULES_DELIM = ">>";
	private Map<String, String> rules;

	public UrlTransformer() {
		rules = new LinkedHashMap<String, String>();
		load();
	}

	@Override
	public String transform(String surl) {
		for (Entry<String, String> entry : rules.entrySet()) {
			if (surl.matches(entry.getKey())) {
				return surl.replaceAll(entry.getKey(), entry.getValue());
			}
		}
		return surl;
	}

	private void load() {
		InputStream ins = PropertiesManager.getResourceStream(TRANSFORM_RULES_RESOURCE);
		String str = StreamX.inputStreamToString(ins);
		UtilX.silentClose(ins);
		String[] lines = str.split("\r\n|\n");
		for (String line : lines) {
			String[] split = line.split(TRANSFORM_RULES_DELIM);
			if (split.length == 2) {
				rules.put(split[0], split[1]);
			}
		}
	}
}
