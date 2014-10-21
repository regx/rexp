package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.cli.sa.CmdSaGoogleLinks;
import eu.fox7.rexp.isc.analysis.basejobs.BatchJob;
import eu.fox7.rexp.isc.analysis.basejobs.XmlJob;
import eu.fox7.rexp.isc.analysis.corejobs.GoogleLinkExtractor.GoogleQueryResultItem;
import eu.fox7.rexp.isc.analysis.util.FileIterator;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.StreamX;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import nu.xom.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GoogleLinkExtractor extends XmlJob<GoogleQueryResultItem> {
	public static void main(String[] args) {
		Director.setup();
		new CmdSaGoogleLinks().test();
	}

	private static class JsonArrayIterator implements Iterator<Object> {
		private JSONArray ja;
		private int i = 0;

		public JsonArrayIterator(JSONArray ja) {
			this.ja = ja;
		}

		@Override
		public boolean hasNext() {
			return ja != null && i < ja.length();
		}

		@Override
		public Object next() {
			try {
				JSONObject jo = ja.getJSONObject(i++);
				return jo;
			} catch (JSONException ex) {
				Log.e(ex.toString());
				i = Integer.MAX_VALUE;
				return null;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported");
		}
	}

	public static class GoogleQueryResultItem extends BatchJob.Item<JSONObject> {
		private final String fileName;

		public GoogleQueryResultItem(JSONObject item, String fileName) {
			super(item);
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}
	}

	private static final String ITEMS_JSON_KEY = "items";
	private static final String LINK_JSON_KEY = "link";
	public static final String TAG_URL = "url";
	public static final String FILE_TAG = "file";
	private Map<String, String> links2file;
	private String jobDirectoryPath;

	public GoogleLinkExtractor() {
		links2file = new LinkedHashMap<String, String>();
		jobDirectoryPath = PropertiesManager.getProperty(Director.PROP_GOOGLE_DIR);
	}

	public String getJobInputDirectoryPath() {
		return jobDirectoryPath;
	}

	public void setJobInputDirectoryPath(String sourceFileName) {
		this.jobDirectoryPath = sourceFileName;
	}

	@Override
	protected String getJobMetaFileProperty() {
		return Director.PROP_LINK_FILE;
	}

	@Override
	protected void fromXmlElement(Element root) {
		XmlMapUtils.readMapFromXmlAttributes(root, links2file, TAG_URL, FILE_TAG);
	}

	@Override
	protected Element toXmlElement() {
		return XmlMapUtils.writeMapToXmlAttributes(links2file, TAG_URL, FILE_TAG);
	}

	@Override
	protected Iterator<GoogleQueryResultItem> iterateItems() {
		final FileIterator fit = getFileIterator();

		return new Iterator<GoogleQueryResultItem>() {
			@Override
			public boolean hasNext() {
				return fit.hasNext();
			}

			@Override
			public GoogleQueryResultItem next() {
				File f = fit.next();
				String relativeFileName = fit.getPathRelativeToBase(f);
				JSONObject jo = loadJson(f);
				return new GoogleQueryResultItem(jo, relativeFileName);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}

	private static JSONObject loadJson(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			String jsonStr = StreamX.inputStreamToString(fis);
			UtilX.silentClose(fis);
			return new JSONObject(jsonStr);
		} catch (FileNotFoundException ex) {
			Log.w("File not found: %s", file);
		} catch (JSONException ex) {
			Log.w("Error parsing JSON in file: %s", file);
			Log.w("Could not parse JSON: %s", ex);
		}
		return null;
	}

	@Override
	protected void process(GoogleQueryResultItem item) {
		try {
			JSONObject jo = item.get();
			JSONArray ja = jo.getJSONArray(ITEMS_JSON_KEY);
			for (Object o : UtilX.iterate(new JsonArrayIterator(ja))) {
				String link = ((JSONObject) o).getString(LINK_JSON_KEY);
				if (link != null) {
					String fileName = item.getFileName().replaceFirst("\\./", getJobInputDirectoryPath() + "/");
					if (!links2file.containsKey(link)) {
						Log.i("Found link %s", link);
						links2file.put(link, fileName);
					} else {
						Log.w("Duplicate link %s, file name: %s", link, fileName);
					}
				}
			}
		} catch (JSONException ex) {
			Log.w(ex.toString());
		} catch (NullPointerException ex) {
			Log.w("Error parsing JSON file: ", item.getFileName());
		}
	}

	public static int checkTotalGoogleResults(File jsonFile) {
		int result = Integer.MAX_VALUE;
		try {
			JSONObject jo = loadJson(jsonFile);
			result = jo.getJSONObject("queries")
				.getJSONArray("request")
				.getJSONObject(0)
				.getInt("totalResults");
		} catch (JSONException ignored) {
		} catch (NullPointerException ignored) {
		}
		return result;
	}

	public Map<String, String> getRawMap() {
		return links2file;
	}

	public FileIterator getFileIterator() {
		File baseDir = FileX.newFile(getJobInputDirectoryPath());
		final FileIterator fit = new FileIterator(baseDir);
		return fit;
	}
}
