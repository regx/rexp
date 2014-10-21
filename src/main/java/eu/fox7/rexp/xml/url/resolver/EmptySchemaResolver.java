package eu.fox7.rexp.xml.url.resolver;

import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import eu.fox7.rexp.xml.url.MappedUSH;
import eu.fox7.rexp.xml.url.UrlOpener;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class EmptySchemaResolver implements UrlOpener {
	private static final String EMPTY_SCHEMA_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" />";

	public static void main(String[] args) throws Exception {
		String fileName = args.length > 0 ? args[0] : ".files/xml/part/xsd0.xsd";

		Log.configureRootLogger(Level.FINEST);
		Xsd2XSchema.setVerbose(true);
		Xsd2XSchema processor = new Xsd2XSchema();

		File file = new File(fileName);
		String urlStr = file.toURI().toURL().toExternalForm();
		EmptySchemaResolver resolver = new EmptySchemaResolver();
		resolver.registerWithExclusions(urlStr);
		processor.process(new InputSource(urlStr));
		resolver.unregister();
		System.out.println(processor.getResult().toPrettyString());
	}

	private static class EmptySchemaUrlConnection extends URLConnection {
		public EmptySchemaUrlConnection(URL url) {
			super(url);
		}

		@Override
		public void connect() throws IOException {
		}

		@Override
		public InputStream getInputStream() throws IOException {
			byte[] b = EMPTY_SCHEMA_STRING.getBytes();
			ByteArrayInputStream bis = new ByteArrayInputStream(b);
			return bis;
		}
	}

	private Set<String> excludes;

	public EmptySchemaResolver() {
		this.excludes = new HashSet<String>();
		excludes.add(Xsd2XSchema.XSD_NAMESPACE);
	}

	public void addExclusion(String... urlStrs) {
		excludes.addAll(Arrays.asList(urlStrs));
	}

	public void register(String... protocols) {
		MappedUSH.getInstance().register(this, protocols);
	}

	public void registerWithExclusions(String... excludedUrlStrs) {
		MappedUSH.getInstance().register(this, "http", "https", "file");
		for (String excludedUrlStr : excludedUrlStrs) {
			addExclusion(excludedUrlStr);
		}
	}

	public void unregister() {
		MappedUSH.getInstance().unregister("http", "https", "file");
	}

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		if (excludes.contains(u.toExternalForm())) {
			return null;
		} else {
			return new EmptySchemaUrlConnection(u);
		}
	}

	public static final String[] HTTP_PROTOCOLS = {"http", "https"};
}
