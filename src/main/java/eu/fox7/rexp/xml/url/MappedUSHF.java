package eu.fox7.rexp.xml.url;

import eu.fox7.rexp.util.ReflectX;
import eu.fox7.rexp.util.StreamX;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

public class MappedUSHF implements URLStreamHandlerFactory {
	private static class SingletonHolder {
		static final MappedUSHF INSTANCE = new MappedUSHF();
	}

	public static MappedUSHF getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private Map<String, URLStreamHandler> registeredHandlers;
	private Map<String, URLStreamHandler> originalHandlers;

	private MappedUSHF() {
		registeredHandlers = new HashMap<String, URLStreamHandler>();
		originalHandlers = new HashMap<String, URLStreamHandler>();
	}

	public void register(String protocol, URLStreamHandler handler) {
		if (!originalHandlers.containsKey(protocol)) {
			URLStreamHandler oldHandler = getProtocolStreamHandler(protocol);
			originalHandlers.put(protocol, oldHandler);
		}
		registeredHandlers.put(protocol, handler);
	}

	public URLStreamHandler getHandler(String protocol) {
		return registeredHandlers.get(protocol);
	}

	public URLStreamHandler getOriginalHandler(String protocol) {
		return originalHandlers.get(protocol);
	}

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		return getHandler(protocol);
	}

	public static void register() {
		MultiUSHF.register();
		MultiUSHF.getInstance().add(getInstance());
	}
	

	public static void testUrlConnection(String surl) {
		try {
			System.out.println("<<<< " + surl + " ====");
			InputStream inputStream = new URL(surl).openConnection().getInputStream();
			StreamX.transfer(inputStream, System.out);
			inputStream.close();
			System.out.println();
			System.out.println(">>>>");
		} catch (IOException ex) {
			System.err.println("Failed URL test: " + surl);
			ex.printStackTrace(System.err);
		}
	}
	

	static URLConnection openHandlerConnection(URLStreamHandler handler, URL u) throws IOException {
		return (URLConnection) ReflectX.invoke(URLStreamHandler.class, "openConnection", handler, u);
	}

	static URLStreamHandler getProtocolStreamHandler(String protocol) {
		URLStreamHandler r = (URLStreamHandler) ReflectX.invoke(URL.class, "getURLStreamHandler", null, protocol);
		Map<?, ?> handlers = (Map) ReflectX.getField(URL.class, "handlers", null);
		handlers.remove(protocol);
		return r;
	}

	static URLConnection openOriginalConnection(URL u) {
		try {
			URLStreamHandler handler = getInstance().getOriginalHandler(u.getProtocol());
			if (handler != null) {
				return openHandlerConnection(handler, u);
			} else {
				return u.openConnection();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void resetUrlHandlerFactorySetup() {
		Map<?, ?> handlers = (Map) ReflectX.getField(URL.class, "handlers", null);
		handlers.clear();
		ReflectX.setField(URL.class, "factory", null, null);
	}
}
