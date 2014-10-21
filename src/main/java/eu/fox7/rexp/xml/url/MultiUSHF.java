package eu.fox7.rexp.xml.url;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MultiUSHF implements URLStreamHandlerFactory {
	private static class SingletonHolder {
		static final MultiUSHF INSTANCE = new MultiUSHF();
	}

	public static MultiUSHF getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private Map<Object, URLStreamHandlerFactory> map;

	private MultiUSHF() {
		map = new LinkedHashMap<Object, URLStreamHandlerFactory>();
	}

	public void add(URLStreamHandlerFactory factory) {
		add(factory, factory);
	}

	public void add(URLStreamHandlerFactory factory, Object tag) {
		map.put(tag, factory);
	}

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		for (Entry<Object, URLStreamHandlerFactory> entry : map.entrySet()) {
			URLStreamHandlerFactory factory = entry.getValue();
			URLStreamHandler handler = factory.createURLStreamHandler(protocol);
			if (handler != null) {
				return handler;
			}
		}
		return null;
	}

	public static void safeRegister(URLStreamHandlerFactory factory) {
		try {
			URL.setURLStreamHandlerFactory(factory);
		} catch (Error ignored) {
		}
	}

	public static void register() {
		safeRegister(getInstance());
	}
}
