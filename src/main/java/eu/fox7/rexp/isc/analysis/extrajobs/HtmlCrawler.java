package eu.fox7.rexp.isc.analysis.extrajobs;

import eu.fox7.rexp.isc.analysis.basejobs.FileJob;
import eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.util.FileIterator;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Callback;
import eu.fox7.rexp.xml.url.UriX;
import eu.fox7.rexp.xml.util.UriHelper;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;
import nu.xom.Node;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;
import static eu.fox7.rexp.xml.util.iterators.NodeIterable.iterateNodes;

public class HtmlCrawler extends FileJob {
	public static void main(String[] args) throws Exception {
		setup();
		jobRun();
	}

	public static void simpleRun() {
		String fileName = "./xsd/raw/c1d2h.xhtml";
		String urlStr = "http://www.doctohelp.com/FAKE-EXTRA-PATH/c1d2h.xsd";
		processHtml(fileName, urlStr, null);
	}

	public static void jobRun() {
		HtmlCrawler hc = new HtmlCrawler();
		hc.setJobInputDirectoryPath(resolve(PROP_DOWNLOAD_DIR));
		hc.setJobOutputDirectoryPath(resolve(PROP_DOWNLOAD_DIR));
		hc.setJobInOutFile(resolve(PROP_DOWNLOAD_FILE));
		hc.execute();
	}

	private BatchDownloader downloader;
	private boolean abortIfNotInDb = false;

	public HtmlCrawler() {
		downloader = new BatchDownloader();
	}

	public void setJobOutputDirectoryPath(String directoryName) {
		downloader.setDownloadDir(directoryName);
	}

	public void setJobInOutFile(String fileName) {
		downloader.setLinkSourceFileName(fileName);
	}

	@Override
	protected void onStart() {
		super.onStart();
		downloader.load();
	}

	@Override
	protected void onStop() {
		downloader.save();
		super.onStop();
	}

	@Override
	protected void process(File file, String relativeFileName) {
		String baseDir = getJobInputDirectoryPath();
		String fileName = FileIterator.rebase(baseDir, relativeFileName);
		String urlOrigin = UtilX.reverseLookup(downloader.getRawMap(), fileName);//downloader.lookupFileName(fileName);
		if (urlOrigin != null || !abortIfNotInDb) {
			if (urlOrigin == null) {
				urlOrigin = "http://" + fileName.replaceFirst("^" + Pattern.quote(baseDir) + "/?", "");
			}

			Log.d("Examining %s, %s", urlOrigin, fileName);
			processHtml(fileName, urlOrigin, new Callback<String>() {
				@Override
				public void call(String urlStr) {
					downloader.download(urlStr);
				}
			});
		} else {
			Log.w("Could not find %s in known downloads, skipping", fileName);
		}
	}

	@Override
	protected String getJobDirectoryProperty() {
		return Director.PROP_DOWNLOAD_DIR;
	}

	protected static void processHtml(String fileName, String urlStr, Callback<String> callback) {
		String base = new UriX(urlStr).base();
		File file = FileX.newFile(fileName);
		Element element = XmlUtils.readHtml(file);
		if (element != null) {
			Log.v("HTML content: %s", urlStr);
			Set<String> links = new LinkedHashSet<String>();
			findHrefs(element, links);
			links = UriHelper.canonizeUrlStrs(links, base);
			for (String link : links) {
				if (link.endsWith(".xsd")) {
					Log.i("Found %s in %s ", link, fileName);
					if (callback != null) {
						callback.call(link);
					}
				}
			}
		} else {
			Log.v("Non-HTML content: %s", urlStr);
		}
	}

	public static void findHrefs(Element root, Set<String> outSet) {
		for (Node node : iterateNodes(root.query("//@href"))) {
			String value = node.getValue();
			outSet.add(value);
		}
	}
}
