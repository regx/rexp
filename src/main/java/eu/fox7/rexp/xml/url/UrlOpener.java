package eu.fox7.rexp.xml.url;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public interface UrlOpener {
	URLConnection openConnection(URL u) throws IOException;
}