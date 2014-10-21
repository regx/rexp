package eu.fox7.rexp.xml.url.resolver;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.util.Downloader;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.RuleTransformer;
import eu.fox7.rexp.xml.util.UriHelper;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

public class CustomEntityResolver implements EntityResolver {
	public static void main(String[] args) throws Exception {
		Director.setup();
		String systemId = "file:/D:/home/dev/data/.rexp/xsd/filtered/www.assembla.com/code/IS29500/subversion/node/live/89/branches/Part1DCOR2/OfficeOpenXML-XMLSchema-Strict/wml.xsd";
		String prefix = "D:/home/dev/data/.rexp/xsd/filtered";
		String cache = "D:/home/dev/data/.rexp/xsd/cache";
		String url = "http://www.assembla.com/code/IS29500/subversion/node/live/89/branches/Part1DCOR2/OfficeOpenXML-XMLSchema-Strict/wml.xsd";
		System.out.println(resolveFileToHttp(prefix, systemId));
		System.out.println(resolveHttpToFile(prefix, url));
		System.out.println(UriHelper.compressPath("a/1/2/../../b/././c/."));
		System.out.println(resolveFileToCache(prefix, cache, systemId));

	}
	
	private String context;
	private String cache;
	private boolean useCacheExclusively;
	private static boolean doDownload = true;

	public CustomEntityResolver() {
		this(null);
	}

	public CustomEntityResolver(String context) {
		this(context, context);
	}

	public CustomEntityResolver(String context, String cache) {
		setContext(context);
		setCache(cache);
		useCacheExclusively = false;
	}

	public final void setContext(String context) {
		if (context != null) {
			this.context = UriHelper.compressPath(context);
		}
	}

	public final void setCache(String cache) {
		if (cache != null) {
			this.cache = UriHelper.compressPath(FileX.toAbsoluteExternal(FileX.newFile(cache)));
		}
	}

	public void setUseExclusiveCacheUsage(boolean useCacheExclusively) {
		this.useCacheExclusively = useCacheExclusively;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId == null) {
			Log.w("systemId is null for publicId %s", publicId);
			return null;
		}

		if (systemId.startsWith("http")) {
			String localId = Downloader.fileNameEscape(resolveHttpToFile(context, systemId));
			String localId2 = Downloader.fileNameEscape(resolveHttpToFile(cache, systemId));
			try {
				URI uri = new URL(localId).toURI();
				URI uri2 = new URL(localId2).toURI();
				if (new File(uri).exists()) {
					systemId = localId;
				} else if (new File(uri2).exists()) {
					systemId = localId2;
				} else {
					if (doDownload) {
						String dwnldFileName = delegateDownload(systemId, cache);
						if (dwnldFileName != null) {
							systemId = "file:/" + dwnldFileName;
						} else {
							systemId = null;
						}
					}
				}
			} catch (URISyntaxException ex) {
				Log.w("Could not resolve uri for %s: %s", systemId, ex);
			}
		} else if (context != null && systemId.startsWith("file:")) {
			try {
				URI uri = new URL(systemId).toURI();
				URI uri2 = new URL(resolveFileToCache(context, cache, systemId)).toURI();
				if (new File(uri).exists()) {
					Log.v("File found in context: %s", uri);
				}
				if (new File(uri2).exists()) {
					Log.v("File found in cache: %s", uri2);
					systemId = uri2.toString();
				} else {
					String ctx = systemId.matches(".*" + Pattern.quote(context) + ".*") ? context : cache;
					String remoteId = resolveFileToHttp(ctx, systemId);
					if (!doDownload) {
						systemId = remoteId;
					} else {
						if (remoteId.startsWith("file")) {
							throw new URISyntaxException(remoteId, "Protocol must be http");
						}
						String dwnldFileName = delegateDownload(remoteId, cache);
						if (dwnldFileName != null) {
							systemId = "file:/" + dwnldFileName;
						} else {
							systemId = null;
						}
					}
				}
			} catch (URISyntaxException ex) {
				Log.w("Could not resolve uri for %s: %s", systemId, ex);
			}
		}

		return new InputSource(systemId);
	}

	private String delegateDownload(String urlStr, String directory) {
		if (useCacheExclusively) {
			return null;
		}
		return Downloader.rawDownloadByUrlString(urlStr, directory, null, true, true, true);
	}
	

	public static String resolveFileToCache(String prefix, String cache, String systemId) {
		String rule = "file:/?/?($prefix/?)(.*)>>file:/${cache}/$2";
		String vars = "prefix=" + prefix + "," + "cache=" + cache;
		RuleTransformer rt = new RuleTransformer(rule, vars);
		return rt.apply(systemId);
	}

	public static String resolveFileToHttp(String prefix, String systemId) {
		String rule = "file://?/?($prefix/?)(.*)>>http://$2";
		String vars = "prefix=" + prefix;
		RuleTransformer rt = new RuleTransformer(rule, vars);
		return rt.apply(systemId);
	}

	public static String resolveHttpToFile(String prefix, String systemId) {
		String rule = "https?://(.*)>>file:/${prefix}/$1";
		String vars = "prefix=" + prefix;
		RuleTransformer rt = new RuleTransformer(rule, vars);
		return rt.apply(systemId);
	}
}
