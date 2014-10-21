package eu.fox7.rexp.isc.analysis.extrajobs;

import eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader;
import eu.fox7.rexp.isc.analysis.corejobs.GoogleCrawler;
import eu.fox7.rexp.isc.analysis.corejobs.GoogleLinkExtractor;
import eu.fox7.rexp.isc.analysis.util.FileIterator;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.url.UriX;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.setup;

public class DataCleaner {
	public static void main(String[] args) {
		setup();
		DataCleaner cleaner = new DataCleaner();
		cleaner.setSimulation(false);
		cleaner.collectDownloadData();
		cleaner.removeDownloadsWithMissingFiles();
		cleaner.downloadMissingFiles();
		cleaner.moveUnexpectedPathDownloads();
		cleaner.overwriteUnexpectedSourceDownloads();
		cleaner.commitDownloadData();
		cleaner.rebuildGoogleData();
	}

	private boolean simulation = false;
	private BatchDownloader downloadManager;
	private String basePath;

	private Map<String, String> mapFileMissing = new LinkedHashMap<String, String>();
	private Map<String, String> mapUnexpectedPath = new LinkedHashMap<String, String>();
	private Map<String, String> mapUnexpectedUrl = new LinkedHashMap<String, String>();

	public DataCleaner() {
		downloadManager = new BatchDownloader();
		downloadManager.load();
		basePath = downloadManager.getDownloadDir();
		reinitDownloadCheck();
	}

	public void setSimulation(boolean simulation) {
		this.simulation = simulation;
	}

	public final void reinitDownloadCheck() {
		mapFileMissing = new LinkedHashMap<String, String>();
		mapUnexpectedPath = new LinkedHashMap<String, String>();
		mapUnexpectedUrl = new LinkedHashMap<String, String>();
	}

	public void collectDownloadData() {
		for (Entry<String, String> entry : downloadManager.getRawMap().entrySet()) {
			String urlStr = entry.getKey();
			String pathStr = entry.getValue();

			checkFileExistence(urlStr, pathStr);
			checkExpectedPaths(urlStr, pathStr);
			checkExpectedUrls(urlStr, pathStr);
		}
	}

	public void checkFileExistence(String urlStr, String pathStr) {
		if (!FileX.newFile(pathStr).exists()) {
			Log.w("EX: %s does not exist, origin %s", pathStr, urlStr);
			mapFileMissing.put(urlStr, pathStr);
		} else {
			Log.v("EX: %s exists", pathStr);
		}
	}

	public void checkExpectedPaths(String urlStr, String pathStr) {
		String expectedPath = getExpectedPath(urlStr);
		if (!pathStr.equals(expectedPath)) {
			Log.w("TN: %s should be stored in %s, but is in %s", urlStr, expectedPath, pathStr);
			mapUnexpectedPath.put(urlStr, pathStr);
		} else {
			Log.v("TN: %s is correctly stored in %s", urlStr, pathStr);
		}
	}

	public void checkExpectedUrls(String urlStr, String pathStr) {
		String expectedUrlStr = UriX.inverseFullHostedPath("http", pathStr);
		if (!urlStr.equals(expectedUrlStr)) {
			Log.w("US: %s was expected to be from %s, but is from %s", pathStr, expectedUrlStr, urlStr);
			mapUnexpectedUrl.put(urlStr, pathStr);
		} else {
			Log.v("US: %s was correctly from %s", pathStr, urlStr);
		}
	}

	public void removeDownloadsWithMissingFiles() {
		for (Entry<String, String> entry : mapFileMissing.entrySet()) {
			String urlStr = entry.getKey();
			Log.d("EX: Removing %s", urlStr);
			downloadManager.getRawMap().remove(urlStr);
		}
	}

	public void downloadMissingFiles() {
		for (Entry<String, String> entry : mapFileMissing.entrySet()) {
			String urlStr = entry.getKey();
			Log.d("EX: Trying to redownload %s", urlStr);
			if (!simulation) {
				String downloadPath = downloadManager.download(urlStr);
				if (downloadPath != null) {
					Log.v("Redownload successufl for %s: %s", urlStr, downloadPath);
				} else {
					Log.w("Could not redownload %s", urlStr);
				}
			}
		}
	}

	public void moveUnexpectedPathDownloads() {
		for (Entry<String, String> entry : mapUnexpectedPath.entrySet()) {
			String urlStr = entry.getKey();
			String pathStr = entry.getValue();
			String expectedPath = getExpectedPath(urlStr);
			Log.d("TN: Moving %s to %s", pathStr, expectedPath);
			if (!simulation) {
				try {
					boolean b = FileX.newFile(pathStr).renameTo(FileX.newFile(expectedPath));
					if (!b) {
						Log.w("Could not move %s to %s", pathStr, expectedPath);
					}
				} catch (NullPointerException npe) {
					Log.e("Cound not move file %s to %s", pathStr, expectedPath);
				} catch (SecurityException ex) {
					Log.e("Cound not move file %s to %s", pathStr, expectedPath);
				}
			}
			downloadManager.getRawMap().put(urlStr, expectedPath);
		}
	}

	public void overwriteUnexpectedSourceDownloads() {
		for (Entry<String, String> entry : mapUnexpectedUrl.entrySet()) {
			String oldUrlStr = entry.getKey();
			String pathStr = downloadManager.getRawMap().get(oldUrlStr);
			String newUrlStr = UriX.inverseFullHostedPath("http", pathStr);
			Log.d("US: Changing %s to %s for %s", oldUrlStr, newUrlStr, pathStr);
			downloadManager.getRawMap().remove(oldUrlStr);
			downloadManager.getRawMap().put(newUrlStr, pathStr);
		}
	}

	public void commitDownloadData() {
		if (!simulation) {
			downloadManager.save();
		}
	}

	private String getExpectedPath(String urlStr) {
		String expectedPath = new UriX(urlStr).externalFullHostedPath();//getNiceTargetPath(urlStr);
		expectedPath = UriX.concatBaseWithSub(basePath, expectedPath);
		return expectedPath;
	}
	

	private static final String UNKOWN_SOURCE = "UNKNOWN";
	private static final boolean CHECK_GOOGLE_QUERY_IN_FILE = true;

	public void rebuildGoogleData() {
		GoogleCrawler googleManager = new GoogleCrawler();
		GoogleLinkExtractor linkManager = new GoogleLinkExtractor();
		googleManager.load();
		FileIterator fit = linkManager.getFileIterator();
		while (fit.hasNext()) {
			File f = fit.next();
			String relativeFileName = fit.getPathRelativeToBase(f);
			Map<String, String> source2path = googleManager.getRawMap();

			if (CHECK_GOOGLE_QUERY_IN_FILE) {
				String jsonStr = FileX.stringFromFile(f);
				String query = GoogleCrawler.getStringFromGoogleResult(jsonStr, "searchTerms");
				int start = GoogleCrawler.getIntFromGoogleResult(jsonStr, "startIndex");
				String source = String.format("%s#%s", query, start);
				source2path.put(source, relativeFileName);
			} else {
				String source = inferSourceString(relativeFileName);
				String expectedSource = source.replace(UNKOWN_SOURCE, GoogleCrawler.XSD_FILETYPE_QUERY);
				if (UtilX.reverseLookup(source2path, expectedSource) == null) {
					Log.d("%s has no source, using %s", relativeFileName, source);
					source2path.put(source, relativeFileName);
				} else {
					Log.v("%s has source %s, skipping", relativeFileName, source);
				}
			}
		}
		if (!simulation) {
			googleManager.save();
		}
	}
	private static String inferSourceString(String relativeFileName) {
		UriX uri = new UriX(relativeFileName);
		String key = UriX.trimDot(uri.subPath());

		String range = "";
		try {
			String fileName = uri.name();
			int n = Integer.parseInt(fileName);
			range = String.format("%s-%s", n, n + 10);
		} catch (NumberFormatException ignored) {
		}

		String result = UNKOWN_SOURCE;
		if (range.length() > 0) {
			result += "#" + range;
		}
		if (key.length() > 0) {
			result += "@" + key;
		}

		return result;
	}
}
