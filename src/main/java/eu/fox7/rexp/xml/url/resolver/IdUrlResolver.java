package eu.fox7.rexp.xml.url.resolver;

import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.url.MappedUSH;
import eu.fox7.rexp.xml.url.MappedUSHF;
import eu.fox7.rexp.xml.url.UrlOpener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class IdUrlResolver extends URLConnection implements UrlOpener {
	public static void main(String[] args) {
		UtilX.enableHttps();
		MappedUSH.getInstance().register(IdUrlResolver.class, "http", "https");
		String host = "localhost";
		MappedUSHF.testUrlConnection(String.format("http://%s/docs", host));
		MappedUSHF.testUrlConnection(String.format("https://%s/docs/pub", host));
		MappedUSH.getInstance().unregister("https");
		MappedUSHF.testUrlConnection(String.format("https://%s/docs", host));
		MappedUSHF.testUrlConnection(String.format("http://%s/docs", host));
	}

	public IdUrlResolver(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
	}

	@Override
	public InputStream getInputStream() throws IOException {
		byte[] b = getURL().toExternalForm().getBytes();
		ByteArrayInputStream bis = new ByteArrayInputStream(b);
		return bis;
	}

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		if (u.toExternalForm().contains("pub")) {
			return null;
		} else {
			return new IdUrlResolver(u);
		}
	}
}
