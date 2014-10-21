package eu.fox7.rexp.xml.url;

import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.ReflectX;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.url.resolver.IdUrlResolver;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

public class MappedUSH extends URLStreamHandler {
	public static void main(String[] args) {
		UtilX.enableHttps();
		MappedUSH.getInstance().register(IdUrlResolver.class, "http", "https");
		String host = "tt1.ath.cx";
		MappedUSHF.testUrlConnection(String.format("http://%s/docs", host));
		MappedUSHF.testUrlConnection(String.format("https://%s/docs/pub", host));
		MappedUSH.getInstance().unregister("https");
		MappedUSHF.testUrlConnection(String.format("https://%s/docs", host));
		MappedUSHF.testUrlConnection(String.format("http://%s/docs", host));
	}

	private static class SingletonHolder {
		static final MappedUSH INSTANCE = new MappedUSH();
	}

	public static MappedUSH getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private Map<String, URLStreamHandler> registeredHandlers;

	private MappedUSH() {
		MappedUSHF.register();
		registeredHandlers = new HashMap<String, URLStreamHandler>();
	}

	public void registerHandler(URLStreamHandler handler, String... protocols) {
		for (String protocol : protocols) {
			registeredHandlers.put(protocol, handler);
			MappedUSHF.getInstance().register(protocol, getInstance());
		}
	}

	public <T extends URLConnection & UrlOpener> void register(Class<T> type, String... protocols) {
		URLStreamHandler handler = makeUrlHandler(type);
		registerHandler(handler, protocols);
	}

	public void register(UrlOpener opener, String... protocols) {
		URLStreamHandler handler = makeUrlHandler(opener);
		registerHandler(handler, protocols);
	}

	public void unregister(String... protocols) {
		registerHandler(null, protocols);
	}

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		Log.v("Opening url connection: %s", u);
		URLStreamHandler handler = registeredHandlers.get(u.getProtocol());
		if (handler != null) {
			URLConnection connection = MappedUSHF.openHandlerConnection(handler, u);
			if (connection != null) {
				return connection;
			} else {
				return MappedUSHF.openOriginalConnection(u);
			}
		} else {
			return MappedUSHF.openOriginalConnection(u);
		}
	}

	public static URLConnection cachedOpenConnection(URL u) {
		return MappedUSHF.openOriginalConnection(u);
	}

	private static <T extends URLConnection & UrlOpener> URLStreamHandler makeUrlHandler(final Class<T> type) {
		return new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL u) throws IOException {
				try {
					UrlOpener opener = (UrlOpener) ReflectX.construct(type, u);
					return opener.openConnection(u);
				} catch (InstantiationException ex) {
					throw new IOException(ex);
				} catch (IllegalAccessException ex) {
					throw new IOException(ex);
				}
			}
		};
	}

	private static URLStreamHandler makeUrlHandler(final UrlOpener opener) {
		return new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL u) throws IOException {
				return opener.openConnection(u);
			}
		};
	}
}
