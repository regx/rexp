package eu.fox7.rexp.isc.analysis.util;

import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.RefHolder;
import eu.fox7.rexp.util.StreamX;
import eu.fox7.rexp.xml.url.UriX;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Downloader {
	private static String USER_AGENT = "Mozilla/5.0";
	private static int TIMEOUT = 2000;
	private static final boolean SIMULATE = false;

	public static void main(String[] args) {
		String s = "D:/home/dev/data/.rexp/xsd/raw_/grepcode.com/file/repo1.maven.org/maven2/com.legsem.legstar/legstar-jaxbgen/1.5.5/cobxsd(10)(12)(13)(14)(15)(16)";
		System.out.println(findAlternativeFileForExisting(new File(s)));
	}

	static {
		System.getProperties().setProperty("http.keepAlive", "false");
	}

	public static String download(String urlStr, String directory) {
		return downloadWithUrlPath(urlStr, directory);
	}

	public static String downloadWithUrlPath(String urlStr, String directory) {
		return rawDownloadByUrlString(urlStr, directory, null, true, false, true);
	}

	public static String downloadDirectly(String urlStr, String directory) {
		return rawDownloadByUrlString(urlStr, directory, null, true, false, false);
	}

	public static String downloadWithName(String urlStr, String directory, String fileName) {
		return rawDownloadByUrlString(urlStr, directory, fileName, false, false, false);
	}

	public static String rawDownloadByUrlString(String urlStr, String directory, String fileName, boolean inferName, boolean overwrite, boolean pathSubDirectories) {
		try {
			return rawDownload(new URL(urlStr), directory, fileName, inferName, overwrite, pathSubDirectories);
		} catch (MalformedURLException ex) {
			Log.w("Malformed url: %s", urlStr);
			return null;
		}
	}

	static HttpURLConnection openHttpURLConnection(URL url) throws IOException {
		HttpURLConnection hc = (HttpURLConnection) url.openConnection();
		hc.setRequestProperty("User-agent", USER_AGENT);
		hc.setConnectTimeout(TIMEOUT);
		hc.setReadTimeout(TIMEOUT);
		return hc;
	}

	public static String rawDownload(URL url, String directory, String fileName, boolean inferName, boolean overwrite, boolean pathSubDirectories) {
		HttpURLConnection hc;
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			hc = openHttpURLConnection(url);
			RefHolder<URL> refUrl = new RefHolder<URL>(url);
			hc = resolveRedirects(hc, 0, refUrl);
			url = refUrl.get();
			if (fileName == null) {
				fileName = inferName ? fileName(hc, url) : generateFileName();
			}
			fileName = fileNameEscape(fileName);
			if (pathSubDirectories) {
				String basePath = new UriX(url.toString()).externalHostedSubPath();//UriHelper.getUrlHostedPathToFile(url.toString());
				directory = directory.endsWith("/") ? directory : directory + "/";
				directory = directory + basePath;
			}
			fileName = new UriX(fileName).externalFullHostedPath();//UriHelper.getNiceTargetName(fileName);
			File targetFile = FileX.newFile(directory, fileName);
			if (targetFile.exists()) {
				if (!overwrite) {
					targetFile = findAlternativeFileForExisting(targetFile);
				}
			}
			inStream = hc.getInputStream();
			if (hc.getHeaderField("Content-Encoding") != null) {
				if (hc.getHeaderField("Content-Encoding").contains("gzip")) {
					inStream = new java.util.zip.GZIPInputStream(hc.getInputStream());
				}
			}

			if (!SIMULATE) {
				FileX.prepareOutFile(targetFile);
				outStream = new FileOutputStream(targetFile);
				StreamX.transfer(inStream, outStream);
				outStream.close();
			}
			inStream.close();
			Log.i("Downloaded %s", url);
			fileName = UriX.concatPaths(directory, targetFile.getName());
			return fileName;
		} catch (IOException e) {
			Log.w("Failed to load %s", url);
			Log.w("Exception: %s", e.toString());
			return null;
		} finally {
			if (outStream != null) {
				try {
					outStream.flush();
				} catch (IOException ignored) {
				}
			}
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException ignored) {
				}
			}
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static HttpURLConnection resolveRedirects(HttpURLConnection hc, int d, RefHolder<URL> refUrl) {
		try {
			int status = hc.getResponseCode();
			if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM) {
				URL url = new URL(hc.getHeaderField("Location"));
				refUrl.set(url);
				String cookie = hc.getHeaderField("Set-Cookie");
				hc = openHttpURLConnection(url);
				if (cookie != null) {
					hc.setRequestProperty("Cookie", cookie);
				}
				hc = resolveRedirects(hc, d + 1, refUrl);
			}
		} catch (IOException ex) {
			Log.w("Failed to redirect %s", ex);
		} catch (StackOverflowError ex) {
			Log.w("Failed to redirect %s", ex);
		}
		return hc;
	}

	public static InputStream openHttpInputStream(String surl) {
		try {
			return openHttpInputStream(new URL(surl));
		} catch (MalformedURLException ex) {
			Log.w("Malformed URL: %s", ex);
			return null;
		}
	}

	public static InputStream openHttpInputStream(URL url) {
		InputStream inStream;
		try {
			HttpURLConnection hc = (HttpURLConnection) url.openConnection();
			hc.setRequestProperty("User-agent", USER_AGENT);
			hc.setConnectTimeout(TIMEOUT);
			inStream = hc.getInputStream();
			return inStream;
		} catch (IOException e) {
			Log.w("Exception: %s", e.toString());
			return null;
		}
	}

	private static String fileName(HttpURLConnection hc, URL url) {
		String fileName = fileName(hc);
		fileName = fileName != null ? fileName : fileName(url);
		fileName = fileName != null ? fileName : generateFileName();
		return fileName;
	}

	private static String generateFileName() {
		return UUID.randomUUID().toString();
	}

	private static String fileName(HttpURLConnection hc) {
		String fileName = hc.getHeaderField("Content-Disposition");
		if (fileName != null) {
			Pattern p = Pattern.compile(".*filename=(.*)[^;]*");
			Matcher m = p.matcher(fileName);
			if (m.matches()) {
				fileName = m.group(1);
				if (fileName != null) {
					fileName = fileName.replace("\"", "");
				}
			} else {
				return null;
			}
		}
		return fileName;
	}

	public static String fileName(URL url) {
		String link = url.getPath();
		return fileName(link);
	}

	public static String fileName(String surl) {
		String link = surl;
		String[] a = link.split("/");
		if (a.length > 0) {
			link = a[a.length - 1];
			link = link.replaceAll("%5B", "[");
			link = link.replaceAll("%5D", "]");
			link = link.replaceAll("%20", " ");
			link = link.replaceAll("%23", "#");
			link = link.replaceAll("\\?", "");
			return link.equals("") ? null : link;
		} else {
			return null;
		}
	}

	public static String fileNameEscape(String str) {
		str = str.replace("?", "%3F");
		str = str.replaceAll(":(\\d+)", "%3A$1");
		return str;
	}

	private static File findAlternativeFileForExisting(File file) {
		if (!file.exists()) {
			return file;
		} else {
			String nameWithMaybeExt = file.getName();
			int indexOfDot = nameWithMaybeExt.lastIndexOf(".");
			if (indexOfDot >= 0) {
				String ext = nameWithMaybeExt.substring(indexOfDot + 1, nameWithMaybeExt.length());
				String nameWithoutExt = nameWithMaybeExt.substring(0, indexOfDot);
				int n = getLastNumberInBrace(nameWithoutExt);
				nameWithoutExt = n > 0 ? stripTrailingBracedNum(nameWithoutExt, n) : nameWithoutExt;
				nameWithMaybeExt = String.format("%s(%s).%s", nameWithoutExt, n + 1, ext);
			} else {
				int n = getLastNumberInBrace(nameWithMaybeExt);
				nameWithMaybeExt = n > 0 ? stripTrailingBracedNum(nameWithMaybeExt, n) : nameWithMaybeExt;
				nameWithMaybeExt = String.format("%s(%s)", nameWithMaybeExt, n + 1);
			}
			return findAlternativeFileForExisting(FileX.newFile(file.getParent(), nameWithMaybeExt));
		}
	}

	private static String stripTrailingBracedNum(String name, int n) {
		String rev = new StringBuilder(name).reverse().toString();
		String rem = String.format("\\)%s\\(", new StringBuilder(String.valueOf(n)).reverse());
		return new StringBuilder(rev.replaceFirst(rem, "")).reverse().toString();
	}

	private static int getLastNumberInBrace(String str) {
		Pattern p = Pattern.compile(".*\\((\\d*)\\)");
		Matcher m = p.matcher(str);
		if (m.matches() && m.groupCount() > 0) {
			try {
				return Integer.parseInt(m.group(1));
			} catch (NumberFormatException ignored) {
			}
		}
		return 0;
	}
}
