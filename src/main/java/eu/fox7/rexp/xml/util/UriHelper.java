package eu.fox7.rexp.xml.util;

import eu.fox7.rexp.isc.analysis.util.Downloader;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Transform;
import eu.fox7.rexp.xml.url.UriX;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class UriHelper {

	public static String fileNameFromUrlStr(String urlStr) {
		return Downloader.fileName(urlStr);
	}

	public static String canonize(String refBase, String urlStr) {
		if (refBase != null) {
			if (!urlStr.contains(":")) {
				if (urlStr.startsWith("//")) {
					refBase = new UriX(refBase).protocolEx();
				} else if (urlStr.startsWith("/")) {
					refBase = new UriX(refBase).root();
				} else {
					refBase = refBase.endsWith("/") ? refBase : refBase + "/";
				}
				urlStr = refBase + urlStr;
			}
		}
		return urlStr;
	}

	public static Set<String> canonizeUrlStrs(Set<String> urlStrs, String base) {
		Set<String> set = new LinkedHashSet<String>();
		for (String urlStr : urlStrs) {
			String link = canonize(base, urlStr);
			set.add(link);
		}
		return set;
	}
	

	private static final String UTF8 = "UTF-8";
	public static String encode(String str) {
		final String sp = "SPACEHOLDER";
		str = str.replaceAll(" ", sp);
		try {
			return URLEncoder.encode(str, UTF8).replaceAll(sp, "%20");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static String decode(String str) {
		try {
			return URLDecoder.decode(str, UTF8);
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}

	public static String segmentwiseEncode(String str) {
		return segmentwiseTransform(str, new Transform<String, String>() {
			@Override
			public String transform(String data) {
				return encode(data);
			}
		});
	}

	public static String segmentwiseDecode(String str) {
		return segmentwiseTransform(str, new Transform<String, String>() {
			@Override
			public String transform(String data) {
				return decode(data);
			}
		});
	}

	private static String segmentwiseTransform(String str, Transform<String, String> transform) {
		String[] a = str.split("/");
		StringBuilder sb = new StringBuilder();
		boolean touched = false;
		for (int i = 0; i < a.length; i++) {
			if (touched) {
				sb.append("/");
			}
			sb.append(transform.transform(a[i]));
			touched = true;
		}
		return sb.toString();
	}
	

	public static String compressPath(String str) {
		String[] a = str.split("/");
		ArrayList<String> l = new ArrayList<String>(Arrays.asList(a));
		for (int i = 0; i < l.size(); i++) {
			if (i < 0) {
				throw new RuntimeException(String.format("Malformed path: %s", str));
			}
			String s = l.get(i);
			if ("..".equals(s)) {
				l.remove(i);
				l.remove(i - 1);
				i = i - 2;
			} else if (".".equals(s)) {
				l.remove(i);
				i = i - 1;
			}
		}
		a = l.toArray(new String[l.size()]);
		return UtilX.joinString(a, "/");
	}
}
