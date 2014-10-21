package eu.fox7.rexp.xml.url.resolver;

import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.url.MappedUSH;
import eu.fox7.rexp.xml.url.MappedUSHF;
import eu.fox7.rexp.xml.url.UrlOpener;
import eu.fox7.rexp.xml.url.resolver.HttpLocalResolver.UrlMapping;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.setup;

public class FileLocalResolver implements UrlOpener {
	public static void main(String[] args) {
		setup();
		final String fileName = "./xsd/raw/base1.xsd";
		MappedUSHF.resetUrlHandlerFactorySetup();
		FileLocalResolver.register("./xsd/raw/base.xsd", "./xsd/raw");
		MappedUSHF.testUrlConnection(toUrlStr(fileName));
		FileLocalResolver.unregister();
	}

	public static void register(String rootFilePath, String rootFileContext) {
		FileLocalResolver opener = new FileLocalResolver();

		opener.setRootFileContext(rootFileContext);
		opener.setRootFilePath(rootFilePath);
		opener.setUseHttpLookup(false);

		MappedUSH.getInstance().register(opener, PROTOCOL);
	}

	public static void register() {
		UrlOpener opener = new FileLocalResolver();
		MappedUSH.getInstance().register(opener, PROTOCOL);
	}

	public static void unregister() {
		MappedUSH.getInstance().unregister(PROTOCOL);
	}

	public static final String PROTOCOL = "file";
	private static final String PREFIX = PROTOCOL + ":/*";
	private String basePath;
	private UrlMapping urlMapping;
	private boolean useHttpLookup;
	private String rootFilePath;
	private String rootFileContext;

	public FileLocalResolver() {
		basePath = UtilX.getCd();
		urlMapping = new UrlMapping();
		useHttpLookup = true;
	}

	public void setRootFilePath(String rootFilePath) {
		this.rootFilePath = rootFilePath;
	}

	public void setRootFileContext(String rootFileContext) {
		this.rootFileContext = rootFileContext;
	}

	public void setUseHttpLookup(boolean useHttpLookup) {
		this.useHttpLookup = useHttpLookup;
	}

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		if (isNotRoot(u)) {
			return MappedUSH.cachedOpenConnection(u);
		}

		String baseUrlStr = FileX.newFile(basePath).getCanonicalFile().toURI().toURL().toExternalForm();
		String relativePath = u.toExternalForm().replaceFirst(PREFIX, "");
		relativePath = transform(relativePath);
		String fileUrlStr = baseUrlStr + relativePath;
		Log.d("File url: %s", fileUrlStr);

		if (useHttpLookup) {
			String httpUrlStr = UtilX.reverseLookup(urlMapping.getMap(), relativePath);
			if (httpUrlStr != null) {
				Log.d("Http url: %s", httpUrlStr);
				u = new URL(httpUrlStr);
				return u.openConnection();
			}
		}
		u = new URL(fileUrlStr);
		return MappedUSH.cachedOpenConnection(u);
	}

	private boolean isNotRoot(URL u) {
		if (rootFilePath == null) {
			return !u.toExternalForm().matches(PROTOCOL + ":/*\\..*");
		} else {
			return rootFilePath.equals(u.toExternalForm());
		}
	}

	private String getHttpContextStr() {
		if (rootFilePath != null && rootFileContext != null) {
			String truncatedPath = rootFilePath.replaceFirst(rootFileContext, "");
			String httpContext = UtilX.reverseLookup(urlMapping.getMap(), rootFilePath);
			if (httpContext != null) {
				return httpContext = UtilX.replaceLast(httpContext, truncatedPath, "");
			}
		}
		return null;
	}

	private String transform(String relativePath) {
		String httpContext = getHttpContextStr();
		if (httpContext != null) {
			String requestPath = relativePath.replaceFirst(rootFileContext, "");
			String requestHttpUrlStr = httpContext + requestPath;
			String requestFileUrlStr = urlMapping.get(requestHttpUrlStr);
			return (requestFileUrlStr != null) ? requestFileUrlStr : relativePath;
		} else {
			return relativePath;
		}
	}

	public static String toUrlStr(String fileName) {
		return PROTOCOL + ":///" + fileName;
	}
}
