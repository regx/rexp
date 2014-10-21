package eu.fox7.rexp.xml.url.resolver;

import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.url.MappedUSH;
import eu.fox7.rexp.xml.url.MappedUSHF;
import eu.fox7.rexp.xml.url.UrlOpener;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import static eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader.TAG_PATH;
import static eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader.TAG_URL;
import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

public class HttpLocalResolver implements UrlOpener {
	public static void main(String[] args) {
		setup();
		register();
		MappedUSHF.testUrlConnection("http://base.google.com/base/base.xsd");
		unregister();
	}

	public static class UrlMapping {
		private Map<String, String> url2fileName;

		public UrlMapping() {
			this(resolve(PROP_DOWNLOAD_FILE));
		}

		public UrlMapping(String urlMappingFileName) {
			url2fileName = new LinkedHashMap<String, String>();
			load(url2fileName, urlMappingFileName);
		}

		public static void load(Map<String, String> url2fileName, String urlMappingFileName) {
			File urlMappingFile = FileX.newFile(urlMappingFileName);
			Element urlMappingRootElement = XmlUtils.readXml(urlMappingFile);
			XmlMapUtils.readMapFromXmlAttributes(urlMappingRootElement, url2fileName, TAG_URL, TAG_PATH);
		}

		public String get(String key) {
			return url2fileName.get(key);
		}

		public Map<String, String> getMap() {
			return url2fileName;
		}
	}

	public static void register() {
		UrlOpener opener = new HttpLocalResolver();
		MappedUSH.getInstance().register(opener, PROTOCOLS);
	}

	public static void unregister() {
		MappedUSH.getInstance().unregister(PROTOCOLS);
	}

	public static final String[] PROTOCOLS = {"http", "https"};
	private UrlMapping urlMapping;
	private String fileBase;

	public HttpLocalResolver() {
		urlMapping = new UrlMapping();
		fileBase = UtilX.getCd();
	}

	public void setFileBase(String fileBase) {
		this.fileBase = fileBase;
	}

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		String fileName = urlMapping.get(u.toExternalForm());

		if (fileName != null) {
			String fileUrl = FileX.newFile(fileBase, fileName).toURI().toURL().toExternalForm();
			return new URL(fileUrl).openConnection();
		} else {
			return null;
		}
	}
}
