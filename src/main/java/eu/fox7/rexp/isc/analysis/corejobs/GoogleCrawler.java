package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.isc.analysis.basejobs.BatchJob.Item;
import eu.fox7.rexp.isc.analysis.basejobs.XmlJob;
import eu.fox7.rexp.isc.analysis.util.Downloader;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.mini.IntIterator;
import eu.fox7.rexp.xml.url.UriX;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import nu.xom.Element;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GoogleCrawler extends XmlJob<Item<Integer>> {
	public static void main(String[] args) {
		Director.setup();

		GoogleCrawler gc = new GoogleCrawler();
		gc.setDownloadPath(".");

		gc.setQuery("filetype:xsd \"maxoccurs=3\"");
		gc.execute();

		gc.setQuery("filetype:xsd \"maxoccurs=4\"");
		gc.execute();
	}

	private static class IntItemIterator extends IntIterator<Item<Integer>> {
		public IntItemIterator(int low, int high, int step) {
			super(low, high, step);
		}

		@Override
		protected Item<Integer> wrap(int i) {
			return new Item<Integer>(i);
		}
	}

	private static final String CSE_API_KEY_PROPERTY = "cse.apikey";
	private static final String CSE_CX_PROPERTY = "cse.cx";
	private static final String DOWNLOAD_URL = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&start=%s&num=%s&q=%s";
	private static String TAG_URL = "url";
	private static String TAG_FILE = "file";
	public static final String XSD_FILETYPE_QUERY = "filetype:xsd";
	public static final int MIN_START = 1;
	public static final int MAX_START = 100;
	public static final int MAX_STEP = 10;

	private Map<String, String> query2file;
	private String query;
	private int low;
	private int high;
	private int num;
	private String path;

	public GoogleCrawler() {
		query2file = new LinkedHashMap<String, String>();
		query = XSD_FILETYPE_QUERY;
		num = MAX_STEP;
		low = MIN_START;
		high = MAX_START;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setLow(int low) {
		this.low = low;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public void setDownloadPath(String path) {
		this.path = path;
	}

	@Override
	protected String getJobMetaFileProperty() {
		return Director.PROP_GOOGLE_FILE;
	}

	@Override
	protected void fromXmlElement(Element root) {
		XmlMapUtils.readMapFromXmlAttributes(root, query2file, TAG_URL, TAG_FILE);
	}

	@Override
	protected Element toXmlElement() {
		return XmlMapUtils.writeMapToXmlAttributes(query2file, TAG_URL, TAG_FILE);
	}

	@Override
	protected Iterator<Item<Integer>> iterateItems() {
		return new IntItemIterator(low, high, num);
	}

	@Override
	protected void process(Item<Integer> item) {
		int start = item.get();
		String queryUrlString = constructDownloadUrl(query, num, start);
		String queryId = String.format("%s#%s-%s", query, start, start + num - 1);
		if (path != null) {
			if (".".equals(path)) {
				path = DatatypeConverter.printBase64Binary(query.getBytes());
			}
			queryId += "@" + path;
		}

		if (!query2file.containsKey(queryId)) {
			String dir = PropertiesManager.getProperty(Director.PROP_GOOGLE_DIR);
			if (path != null) {
				dir += "/" + path;
			}
			String targetFileName = String.format("%s.json", start);
			String file = Downloader.downloadWithName(queryUrlString, dir, targetFileName);
			if (file != null) {
				String filePath = String.format("%s/%s", dir, targetFileName);
				Log.i("%s -> %s", queryUrlString, filePath);
				query2file.put(queryId, filePath);
				checkEarlyTermination(dir, targetFileName, start, num);
			} else {
				Log.i("Error while processing %s", queryUrlString);
			}

			targetFileName = new UriX(file).fullName();
			if (!checkIfMoreResults(dir, targetFileName, start, num)) {
				cancel();
			}
		} else {
			Log.i("Query already done, skipping: %s", queryUrlString);
		}
	}

	private String constructDownloadUrl(String query, int num, int start) {
		return constructDownloadUrl(getApiKey(), getCx(), query, num, start);
	}

	private String constructDownloadUrl(String apiKey, String cx, String query, int num, int start) {
		if (apiKey == null || cx == null) {
			throw new RuntimeException("Google CSE API key or cx not set, check local.properties");
		}
		return String.format(DOWNLOAD_URL, apiKey, cx, start, num, encode(query));
	}

	private static String getApiKey() {
		String key = PropertiesManager.getProperty(CSE_API_KEY_PROPERTY);
		if (key == null) {
			throw new RuntimeException(String.format("No Google API key defined. Please put the key %s in the file %s.", CSE_API_KEY_PROPERTY, LOCAL_PROPERTIES));
		}
		return key;
	}

	private static final String LOCAL_PROPERTIES = "src/mai/resources/local.properties";

	private static String getCx() {
		String cx = PropertiesManager.getProperty(CSE_CX_PROPERTY);
		if (cx == null) {
			throw new RuntimeException(String.format("No Google CSE CX defined. Please put the key %s in the file %s.", CSE_CX_PROPERTY, LOCAL_PROPERTIES));
		}
		return cx;
	}

	private static String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			Log.e("%s", ex);
			return null;
		}
	}

	private void checkEarlyTermination(String dir, String file, int start, int num) {
		int totalResults = GoogleLinkExtractor.checkTotalGoogleResults(FileX.newFile(dir, file));
		if (start + num > totalResults) {
			setRunnable(false);
		}
	}

	public Map<String, String> getRawMap() {
		return query2file;
	}
	
	public boolean checkIfMoreResults(String dir, String path, int start, int num) {
		File file = FileX.newFile(dir, path);
		String jsonStr = FileX.stringFromFile(file);
		if (jsonStr != null) {
			int n = getIntFromGoogleResult(jsonStr, "totalResults");
			return n >= start + num;
		} else {
			return true;
		}
	}
	public static String getStringFromGoogleResult(String jsonStr, String key) {
		try {
			JSONObject jo = new JSONObject(jsonStr);
			return jo.getJSONObject("queries").getJSONArray("request").getJSONObject(0).getString(key);
		} catch (JSONException ex) {
			Log.w("Could not check Google result: %s", ex);
			return null;
		}
	}

	public static int getIntFromGoogleResult(String jsonStr, String key) {
		try {
			JSONObject jo = new JSONObject(jsonStr);
			return jo.getJSONObject("queries").getJSONArray("request").getJSONObject(0).getInt(key);
		} catch (JSONException ex) {
			Log.w("Could not check Google result: %s", ex);
			return -1;
		}
	}
}
