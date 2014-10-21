package eu.fox7.rexp.isc.analysis.util;

import eu.fox7.rexp.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {
	private static final String PROPERTIES_RESOURCE = "application.properties";
	private static final String LOCAL_PROPERTIES_RESOURCE = "local.properties";

	private static class Holder {
		private static Properties properties = load();
	}

	private static Properties get() {
		return Holder.properties;
	}

	private static Properties load() {
		Properties sysProps = System.getProperties();
		Properties props = new Properties();
		loadFromResourceStream(props, PROPERTIES_RESOURCE);
		loadFromResourceStream(props, LOCAL_PROPERTIES_RESOURCE);
		props.putAll(sysProps);
		return props;
	}

	private static String lookupString(String s) {
		try {
			return get().getProperty(s);
		} catch (NullPointerException ex) {
			return null;
		}
	}

	public static String getProperty(String s) {
		return lookupString(s);
	}

	public static boolean getBoolean(String s) {
		s = lookupString(s);
		return s != null ? s.equals("true") : false;
	}

	public static int getInt(String s) {
		s = lookupString(s);
		return s != null ? parseInt(s, 0) : 0;
	}

	public static int parseInt(String s, int defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return defaultValue;
		}
	}

	static void loadFromResourceStream(Properties props, String resourceName) {
		InputStream rs = getResourceStream(resourceName);
		try {
			if (rs != null) {
				props.load(rs);
			}
		} catch (IOException e) {
			Log.w("Unable to load " + resourceName);
		}
	}

	public static InputStream getResourceStream(String resourceName) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream rs = cl.getResourceAsStream(resourceName);
		return rs;
	}
}
