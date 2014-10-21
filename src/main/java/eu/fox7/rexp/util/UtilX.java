package eu.fox7.rexp.util;

import eu.fox7.rexp.App;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.Manifest;

public class UtilX {
	public static void silentSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			Log.e("%s", ex);
		}
	}
	

	public static void enableHttps() {
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
			@Override
			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				return true;
			}
		});

		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				}
			}
		};

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception ignored) {
		}
	}
	

	public static void setCd(String directoryPath) {
		if (directoryPath != null) {
			System.setProperty("user.dir", new File(directoryPath).getAbsolutePath());
		}
	}

	public static String getCd() {
		return System.getProperty("user.dir");
	}
	public static void silentClose(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ignored) {
		}
	}
	

	public static <T> Iterable<T> iterate(final Enumeration<T> e) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {

					@Override
					public boolean hasNext() {
						return e.hasMoreElements();
					}

					@Override
					public T next() {
						return e.nextElement();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Not supported");
					}
				};
			}
		};
	}
	public static <T> Iterable<T> iterate(final Iterator<T> it) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return it;
			}
		};
	}

	public static <T> List<T> makeList(Iterable<T> iterable) {
		List<T> c = new LinkedList<T>();
		for (T o : iterable) {
			c.add(o);
		}
		return c;
	}

	public static <T> List<T> flatten(Iterable<? extends Iterable<T>> iterable) {
		List<T> list = new LinkedList<T>();
		for (Iterable<T> it : iterable) {
			for (T o : it) {
				list.add(o);
			}
		}
		return list;
	}

	public static <K, V> K reverseLookup(Map<K, V> map, V key) {
		for (Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue().equals(key)) {
				return entry.getKey();
			}
		}
		return null;
	}
	public static int iteratorSize(Iterator<?> it) {
		int count = 0;
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public static <K> void mapIncrement(Map<K, Integer> map, K key, int n) {
		if (map.containsKey(key)) {
			Integer v = map.get(key);
			map.put(key, v + n);
		} else {
			map.put(key, n);
		}
	}

	public static <K> void mapIncrement(Map<K, Integer> map, K key) {
		mapIncrement(map, key, 1);
	}
	public static <K, V> void putInMultiMap(Map<K, Set<V>> map, K key, V val) {
		Set<V> set = map.get(key);
		if (set != null) {
			set.add(val);
		} else {
			set = new LinkedHashSet<V>();
			set.add(val);
			map.put(key, set);
		}
	}
	

	public static String getAppName(Class<?> clazz) {
		try {
			return new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
		} catch (URISyntaxException ex) {
			return clazz.getSimpleName();
		}
	}

	public static String findAppVersion(Object obj) {
		URLClassLoader cl = (URLClassLoader) obj.getClass().getClassLoader();
		final String u = "Unknown version";
		try {
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			String s = manifest.getMainAttributes().getValue("Version");
			return s != null ? s : u;
		} catch (Exception e) {
			return u;
		}
	}
	

	public static boolean checkArg(String[] args, String arg) {
		return (args.length > 0 && arg.equals(args[0]));
	}

	public static String[] truncateArgs(String[] args) {
		ArrayList<String> al = new ArrayList<String>(Arrays.asList(args));
		args = al.subList(1, al.size()).toArray(args);
		return args;
	}
	
	public static String replaceLast(String s, String w, String r) {
		int i = s.lastIndexOf(w);
		if (i >= 0) {
			StringBuilder sb = new StringBuilder(s);
			sb.replace(i, i + w.length(), r);
			return sb.toString();
		} else {
			return s;
		}
	}

	public static String joinString(String[] a, String delim) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(a[i]);
		}
		return sb.toString();
	}

	public static String formatNanos(long nanos) {
		DecimalFormat df = new DecimalFormat("0.000000000");
		String s = df.format(nanos / 1000000000.d);
		String re = "(\\d+).(\\d{3})(\\d{3})(\\d{3})";
		return s.replaceAll(re, "$1.$2,$3,$4") + "s";
	}

	public static void browse(String surl) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = java.awt.Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				java.net.URI uri;
				try {
					uri = new java.net.URI(surl);
					desktop.browse(uri);
				} catch (URISyntaxException ex) {
					Log.w("%s", ex);
				} catch (IOException ex) {
					Log.w("%s", ex);
				}
			}
		}
	}
}
